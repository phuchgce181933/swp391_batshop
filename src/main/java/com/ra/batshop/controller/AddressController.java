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

        // Lấy danh sách địa chỉ theo user
        List<Address> addresses = addressRepository.findByUser(user);

        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        return "profile/address";
    }

    @PostMapping("/save")
    public String saveAddress(@ModelAttribute Address address,
                              @RequestParam(value = "isDefault", required = false) String isDefaultStr,
                              HttpSession session,
                              RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 1. Kiểm tra Số điện thoại: Phải đúng 10 chữ số
        String phone = address.getReceiverPhone();
        if (phone == null || !phone.matches("^\\d{10}$")) {
            ra.addFlashAttribute("error", "Số điện thoại không hợp lệ! Vui lòng nhập đúng 10 chữ số.");
            return "redirect:/address/list";
        }

        // 2. Kiểm tra Địa chỉ: Không cho ký tự đặc biệt, trừ dấu "/" cho số nhà
        // 2. Kiểm tra Địa chỉ: Cho phép chữ, số, khoảng trắng, dấu "/" và dấu ","
        String detail = address.getDetail();
// Thêm dấu "," vào ngay sau dấu "/" trong Regex
        if (detail == null || !detail.matches("^[a-zA-Z0-9\\s,/À-ỹ]*$")) {
            ra.addFlashAttribute("error", "Địa chỉ không được chứa ký tự đặc biệt (ngoại trừ dấu / và ,)!");
            return "redirect:/address/list";
        }

        // Thiết lập thông tin user và trạng thái mặc định
        address.setUser(user);
        address.setDefault("on".equals(isDefaultStr) || "true".equals(isDefaultStr));

        // Logic xử lý địa chỉ mặc định duy nhất
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
        ra.addFlashAttribute("success", "Lưu địa chỉ thành công!");
        return "redirect:/address/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        try {
            Address address = addressRepository.findById(id).orElse(null);
            // Kiểm tra quyền sở hữu trước khi xóa
            if (address != null && address.getUser().getId().equals(user.getId())) {
                // Gọi hàm xóa vĩnh viễn cũ của bạn
                addressRepository.hardDeleteAddressById(id);
                ra.addFlashAttribute("success", "Đã xóa vĩnh viễn địa chỉ!");
            } else {
                ra.addFlashAttribute("error", "Không tìm thấy địa chỉ hoặc bạn không có quyền xóa!");
            }
        } catch (Exception e) {
            // Xử lý lỗi ràng buộc dữ liệu (ví dụ địa chỉ đã có trong đơn hàng)
            ra.addFlashAttribute("error", "Lỗi: Không thể xóa do địa chỉ này đang được sử dụng trong đơn hàng!");
        }
        return "redirect:/address/list";
    }

    @GetMapping("/get/{id}")
    @ResponseBody
    public Address getAddress(@PathVariable Long id) {
        return addressRepository.findById(id).orElse(null);
    }
}