package com.ra.batshop.controller;

import com.ra.batshop.model.Address;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.AddressRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/address")
public class AddressController {

    private final AddressRepository addressRepository;

    public AddressController(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @GetMapping("/list")
    public String showAddressList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Address> addresses = addressRepository.findByUser(user);
        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        return "user/address";
    }

    @PostMapping("/save")
    public String saveAddress(@ModelAttribute Address address,
                              @RequestParam(value = "isDefault", required = false) String isDefaultStr,
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // Ép kiểu checkbox: nếu có gửi lên (on) thì là true, không thì false
        boolean isDefault = "on".equals(isDefaultStr) || "true".equals(isDefaultStr);
        address.setDefault(isDefault);

        List<Address> list = addressRepository.findByUser(user);

        // Khống chế 3 địa chỉ
        if (address.getId() == null && list.size() >= 3) {
            return "redirect:/address/list?error=limit";
        }

        // Logic xử lý địa chỉ mặc định
        if (address.isDefault()) {
            // Nếu lưu cái này là mặc định, gỡ mặc định của tất cả cái còn lại trong DB
            for (Address a : list) {
                if (a.isDefault()) {
                    a.setDefault(false);
                    addressRepository.save(a);
                }
            }
        } else if (list.isEmpty()) {
            // Nếu là cái đầu tiên, bắt buộc phải là mặc định
            address.setDefault(true);
        }

        address.setUser(user);
        addressRepository.save(address);

        return "redirect:/address/list";
    }

    @GetMapping("/get/{id}")
    @ResponseBody
    public Address getAddress(@PathVariable Long id) {
        return addressRepository.findById(id).orElse(null);
    }
}