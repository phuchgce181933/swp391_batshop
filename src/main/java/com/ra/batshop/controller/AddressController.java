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
        return "profile/address";
    }

    @PostMapping("/save")
    public String saveAddress(@ModelAttribute Address address,
                              @RequestParam(value = "isDefault", required = false) String isDefaultStr,
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        boolean isDefault = "on".equals(isDefaultStr) || "true".equals(isDefaultStr);
        address.setDefault(isDefault);
        address.setUser(user);

        List<Address> list = addressRepository.findByUser(user);

        if (address.isDefault()) {
            for (Address a : list) {
                // Kiểm tra tránh lỗi Null khi ID của address mới chưa có
                if (a.isDefault() && (address.getId() == null || !a.getId().equals(address.getId()))) {
                    a.setDefault(false);
                    addressRepository.save(a);
                }
            }
        } else if (list.isEmpty()) {
            address.setDefault(true);
        }

        addressRepository.save(address);
        return "redirect:/address/list";
    }

    // --- ĐÂY LÀ CHỖ QUAN TRỌNG NHẤT ĐỂ NÚT XÓA CHẠY ---
    @GetMapping("/delete/{id}")
    public String deleteAddress(@PathVariable("id") Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            // Chỉ cho phép xóa nếu địa chỉ đó tồn tại
            addressRepository.deleteById(id);
        } catch (Exception e) {
            // In lỗi ra console nếu không xóa được (ví dụ do khóa ngoại)
            e.printStackTrace();
        }

        return "redirect:/address/list";
    }

    @GetMapping("/get/{id}")
    @ResponseBody
    public Address getAddress(@PathVariable("id") Long id) {
        return addressRepository.findById(id).orElse(null);
    }
}