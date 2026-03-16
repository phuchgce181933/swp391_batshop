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

    // 1. SỬA: Chỉ lấy danh sách địa chỉ CHƯA BỊ XÓA MỀM
    @GetMapping("/list")
    public String showAddressList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // Sử dụng hàm mới trong Repository để lọc isDeleted = false
        List<Address> addresses = addressRepository.findByUserAndIsDeletedFalse(user);

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
        address.setDeleted(false); // Đảm bảo khi lưu mới/cập nhật thì isDeleted là false

        // Lấy danh sách chưa xóa để xử lý logic mặc định
        List<Address> list = addressRepository.findByUserAndIsDeletedFalse(user);

        if (address.isDefault()) {
            for (Address a : list) {
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

    // 2. SỬA: Logic Xóa mềm (Soft Delete)
    @GetMapping("/delete/{id}")
    public String deleteAddress(@PathVariable("id") Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            // Tìm địa chỉ trong DB
            Address address = addressRepository.findById(id).orElse(null);

            // Kiểm tra: địa chỉ tồn tại và đúng là của user đang đăng nhập
            if (address != null && address.getUser().getId().equals(user.getId())) {
                // THỰC HIỆN XÓA MỀM
                address.setDeleted(true);
                addressRepository.save(address);
                System.out.println("--- ĐÃ XÓA MỀM ĐỊA CHỈ ID: " + id + " ---");
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa mềm địa chỉ:");
            e.printStackTrace();
        }

        return "redirect:/address/list";
    }

    @GetMapping("/get/{id}")
    @ResponseBody
    public Address getAddress(@PathVariable("id") Long id) {
        // Chỉ cho phép lấy dữ liệu nếu địa chỉ đó chưa bị xóa mềm
        return addressRepository.findById(id)
                .filter(a -> !a.isDeleted())
                .orElse(null);
    }
}