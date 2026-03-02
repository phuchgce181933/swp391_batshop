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
        // Kiểm tra trùng Email
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("errorEmail", "Email đã tồn tại");
            return "auth/register";
        }

        // --- PHẦN THÊM MỚI: Kiểm tra trùng số điện thoại ---
        if (userRepository.existsByPhone(user.getPhone())) {
            model.addAttribute("errorPhone", "Số điện thoại đã tồn tại");
            return "auth/register";
        }
        // ------------------------------------------------

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setRole(Role.USER);
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
        return "redirect:/home";
    }

    // =========================
    // CHANGE PASSWORD
    // =========================
    @GetMapping("/change-password")
    public String showChangePassword() {
        return "auth/change-password";
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
            return "auth/change-password";
        }

        if (!newPass.equals(confirmPass)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "auth/change-password";
        }

        user.setPasswordHash(passwordEncoder.encode(newPass));
        userRepository.save(user);
        session.invalidate();

        return "redirect:/login?changed=true";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        return "user/edit-profile";
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

            if (!dbUser.getEmail().equals(formUser.getEmail())
                    && userRepository.existsByEmail(formUser.getEmail())) {
                model.addAttribute("error", "Email đã tồn tại");
                model.addAttribute("user", dbUser);
                return "user/edit-profile";
            }

            dbUser.setEmail(formUser.getEmail());
            dbUser.setFullName(formUser.getFullName());
            dbUser.setPhone(formUser.getPhone());
            dbUser.setUpdatedAt(LocalDateTime.now());

            userRepository.save(dbUser);
            session.setAttribute("user", dbUser);

            model.addAttribute("user", dbUser);
            model.addAttribute("success", "Cập nhật thành công");
            return "user/edit-profile";

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi cập nhật: " + e.getMessage());
            return "user/edit-profile";
        }
    }

    // =========================================================================
    // FORGOT PASSWORD - ĐÃ SỬA: Gửi đúng cho mọi người, dùng 1 mail hệ thống
    // =========================================================================
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

    // =========================================================================
    // RESET PASSWORD - ĐÃ SỬA: Thêm bảo mật cơ bản
    // =========================================================================

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