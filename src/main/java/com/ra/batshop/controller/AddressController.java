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
        return "user/address"; // Đảm bảo file HTML của con nằm đúng thư mục này
    }

    @PostMapping("/save")
    public String saveAddress(@ModelAttribute Address address,
                              @RequestParam(value = "isDefault", required = false) String isDefaultStr,
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 1. Ép kiểu checkbox: nếu tick thì là true
        boolean isDefault = "on".equals(isDefaultStr) || "true".equals(isDefaultStr);
        address.setDefault(isDefault);

        // 2. Lấy danh sách địa chỉ hiện tại của user để xử lý logic mặc định
        List<Address> list = addressRepository.findByUser(user);

        // --- ĐÃ XÓA ĐOẠN CODE KHỐNG CHẾ 3 ĐỊA CHỈ TẠI ĐÂY ---

        // 3. Logic xử lý địa chỉ mặc định
        if (address.isDefault()) {
            // Nếu lưu cái này là mặc định, gỡ mặc định của tất cả cái cũ trong DB
            for (Address a : list) {
                if (a.isDefault()) {
                    a.setDefault(false);
                    addressRepository.save(a);
                }
            }
        } else if (list.isEmpty()) {
            // Nếu là địa chỉ đầu tiên của user, bắt buộc phải là mặc định
            address.setDefault(true);
        }

        // 4. Thiết lập user sở hữu và lưu vào database
        address.setUser(user);
        addressRepository.save(address);

        return "redirect:/address/list";
    }

    @GetMapping("/get/{id}")
    @ResponseBody
    public Address getAddress(@PathVariable Long id) {
        // Trả về dữ liệu JSON để hàm JavaScript editAddress() điền vào form
        return addressRepository.findById(id).orElse(null);
    }

    // Ba tặng thêm cho con hàm Xóa địa chỉ để quản lý cho chuyên nghiệp
    @GetMapping("/delete/{id}")
    public String deleteAddress(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        addressRepository.deleteById(id);
        return "redirect:/address/list";
    }
}