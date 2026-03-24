package com.ra.batshop.controller;

import com.ra.batshop.model.Enum.Role;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.UserRepository;
import com.ra.batshop.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Random;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute("user") User user,
            Model model
    ) {
        // --- PHẦN THÊM MỚI: KIỂM TRA ĐỊNH DẠNG SỐ ĐIỆN THOẠI ---
        String phone = user.getPhone();
        if (phone == null || !phone.matches("^\\d{10}$")) {
            model.addAttribute("errorPhone", "Số điện thoại phải bao gồm đúng 10 chữ số và không chứa chữ cái");
            return "auth/register";
        }
        // ---------------------------------------------------

        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("errorEmail", "Email đã tồn tại");
            return "auth/register";
        }

        if (userRepository.existsByPhone(user.getPhone())) {
            model.addAttribute("errorPhone", "Số điện thoại đã tồn tại");
            return "auth/register";
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setRole(Role.USER);
        user.setStatus(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        return "redirect:/login";
    }


    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          Model model,
                          HttpSession session) {

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            model.addAttribute("error", "User not found");
            return "auth/login";
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            model.addAttribute("error", "Wrong password");
            return "auth/login";
        }
        session.setAttribute("user", user);
        if (user.getRole() == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        } else {
            return "redirect:/home";
        }
    }

    // ========================================================
    // QUẢN LÝ HỒ SƠ (PROFILE) - PHẦN ĐÃ SỬA THEO YÊU CẦU
    // ========================================================

    // 1. Trang xem thông tin tổng quan (Thêm mới để làm trang đệm)
    @GetMapping("/profile")
    public String showProfileInfo(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) return "redirect:/login";

        // Luôn lấy dữ liệu mới nhất từ DB
        User dbUser = userRepository.findById(currentUser.getId()).orElse(currentUser);
        model.addAttribute("user", dbUser);
        return "profile/info"; // Đây là file HTML hiện thông tin và 2 nút bấm
    }

    // 2. Trang chỉnh sửa thông tin (Giữ nguyên Mapping nhưng đổi Redirect)
    @GetMapping("/profile/edit")
    public String editProfileForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);
        return "profile/edit-profile";
    }

    @PostMapping("/profile/edit")
    public String editProfileSubmit(
            @ModelAttribute("user") User formUser,
            HttpSession session,
            Model model
    ) {
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                return "redirect:/login";
            }

            User dbUser = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            // --- PHẦN KIỂM TRA SDT KHI EDIT PROFILE ---
            if (formUser.getPhone() == null || !formUser.getPhone().matches("^\\d{10}$")) {
                model.addAttribute("error", "Số điện thoại phải có đúng 10 chữ số");
                model.addAttribute("user", dbUser);
                return "profile/edit-profile";
            }

            if (!dbUser.getEmail().equals(formUser.getEmail())
                    && userRepository.existsByEmail(formUser.getEmail())) {
                model.addAttribute("error", "Email đã tồn tại");
                model.addAttribute("user", dbUser);
                return "profile/edit-profile";
            }

            dbUser.setEmail(formUser.getEmail());
            dbUser.setFullName(formUser.getFullName());
            dbUser.setPhone(formUser.getPhone());
            dbUser.setUpdatedAt(LocalDateTime.now());

            userRepository.save(dbUser);
            session.setAttribute("user", dbUser);

            // Lưu xong redirect về trang xem thông tin cho hợp lý
            return "redirect:/profile?success=true";

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi cập nhật: " + e.getMessage());
            return "profile/edit-profile";
        }
    }

    // =========================
    // CHANGE PASSWORD
    // =========================
    @GetMapping("/change-password")
    public String showChangePassword() {
        return "profile/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String oldPass,
            @RequestParam String newPass,
            @RequestParam String confirmPass,
            HttpSession session,
            Model model
    ) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (!passwordEncoder.matches(oldPass, user.getPasswordHash())) {
            model.addAttribute("error", "Mật khẩu cũ không đúng");
            return "profile/change-password";
        }

        if (!newPass.equals(confirmPass)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "profile/change-password";
        }

        user.setPasswordHash(passwordEncoder.encode(newPass));
        userRepository.save(user);
        session.invalidate();

        return "redirect:/login?changed=true";
    }

    // =========================
    // FORGOT PASSWORD (GIỮ NGUYÊN)
    // =========================
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email, Model model) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            model.addAttribute("error", "Email không tồn tại trong hệ thống");
            return "auth/forgot-password";
        }

        String code = String.valueOf((int)((Math.random() * 900000) + 100000));

        user.setResetCode(code);
        user.setResetCodeExpiredAt(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Mã đặt lại mật khẩu - BatShop",
                "Mã xác nhận của bạn là: " + code + ". Mã có hiệu lực trong 5 phút."
        );

        model.addAttribute("email", email);
        model.addAttribute("message", "Đã gửi mã xác nhận về hòm thư của bạn.");

        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String newPassword,
            Model model
    ) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            model.addAttribute("error", "Email không tồn tại");
            model.addAttribute("email", email);
            return "auth/reset-password";
        }

        if (!code.equals(user.getResetCode())) {
            model.addAttribute("error", "Mã xác nhận không đúng");
            model.addAttribute("email", email);
            return "auth/reset-password";
        }
        if (user.getResetCodeExpiredAt() == null ||
                user.getResetCodeExpiredAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Mã đã hết hạn, vui lòng yêu cầu lại");
            model.addAttribute("email", email);
            return "auth/reset-password";
        }

        if (newPassword.length() < 6) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự");
            model.addAttribute("email", email);
            return "auth/reset-password";
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetCode(null);
        user.setResetCodeExpiredAt(null);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return "redirect:/login?resetSuccess=true";
    }
}