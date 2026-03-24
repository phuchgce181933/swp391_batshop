package com.ra.batshop.controller;

import com.ra.batshop.model.Address;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.AddressRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        // Gọi đúng hàm findByUser đã thêm ở Repo
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

        address.setUser(user);
        address.setDefault("on".equals(isDefaultStr) || "true".equals(isDefaultStr));

        if (address.isDefault()) {
            List<Address> list = addressRepository.findByUser(user);
            for (Address a : list) {
                if (a.isDefault() && (address.getId() == null || !a.getId().equals(address.getId()))) {
                    a.setDefault(false);
                    addressRepository.save(a);
                }
            }
        }

        addressRepository.save(address);
        return "redirect:/address/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            Address address = addressRepository.findById(id).orElse(null);
            if (address != null && address.getUser().getId().equals(user.getId())) {
                // Gọi lệnh SQL xóa vĩnh viễn
                addressRepository.hardDeleteAddressById(id);
                ra.addFlashAttribute("success", "Đã xóa vĩnh viễn địa chỉ!");
            } else {
                ra.addFlashAttribute("error", "Không tìm thấy địa chỉ!");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: Không thể xóa do ràng buộc đơn hàng!");
        }
        return "redirect:/address/list";
    }

    @GetMapping("/get/{id}")
    @ResponseBody
    public Address getAddress(@PathVariable Long id) {
        return addressRepository.findById(id).orElse(null);
    }
}