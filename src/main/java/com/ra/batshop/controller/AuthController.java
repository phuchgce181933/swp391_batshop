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

    // --- GIỮ NGUYÊN CODE CŨ CỦA ÔNG ---
    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user, Model model) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("error", "Email đã tồn tại");
            return "auth/register";
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session, Model model) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && passwordEncoder.matches(password, user.getPasswordHash())) {
            session.setAttribute("user", user);
            return "redirect:/home";
        }
        model.addAttribute("error", "Email hoặc mật khẩu không đúng");
        return "auth/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Email không tồn tại");
            return "auth/forgot-password";
        }
        String code = String.format("%06d", new Random().nextInt(999999));
        user.setResetCode(code);
        user.setResetCodeExpiredAt(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);
        emailService.sendEmail(email, "Mã xác nhận đặt lại mật khẩu", "Mã của bạn là: " + code);
        model.addAttribute("email", email);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email, @RequestParam String code, @RequestParam String newPassword, Model model) {
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
        if (user.getResetCodeExpiredAt() == null || user.getResetCodeExpiredAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("error", "Mã đã hết hạn");
            model.addAttribute("email", email);
            return "auth/reset-password";
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetCode(null);
        user.setResetCodeExpiredAt(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return "redirect:/login";
    }

    // ============================================================
    // PROFILE
    // ============================================================

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "profile/info";
    }

    @GetMapping("/profile/edit")
    public String showEditProfile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        return "profile/edit-profile";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@ModelAttribute("user") User userForm, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        userRepository.findByEmail(userForm.getEmail()).ifPresent(u -> {
            if (!u.getId().equals(sessionUser.getId())) {
                model.addAttribute("error", "Email này đã được sử dụng!");
            }
        });

        if (model.containsAttribute("error")) return "profile/edit-profile";

        User userInDb = userRepository.findById(sessionUser.getId()).get();
        userInDb.setFullName(userForm.getFullName());
        userInDb.setEmail(userForm.getEmail());
        userInDb.setPhone(userForm.getPhone());
        userInDb.setUpdatedAt(LocalDateTime.now());
        userRepository.save(userInDb);

        session.setAttribute("user", userInDb);
        return "redirect:/profile/edit?success";
    }

    // ✅ THÊM CÁI NÀY ĐỂ FIX 405
    @GetMapping("/change-password")
    public String showChangePassword(HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        return "profile/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String oldPass,
            @RequestParam String newPass,
            @RequestParam String confirmPass,
            HttpSession session,
            Model model) {

        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        if (!passwordEncoder.matches(oldPass, sessionUser.getPasswordHash())) {
            model.addAttribute("error", "Mật khẩu hiện tại không đúng!");
            return "profile/change-password";
        }

        if (!newPass.equals(confirmPass)) {
            model.addAttribute("error", "Xác nhận mật khẩu không khớp!");
            return "profile/change-password";
        }

        User userInDb = userRepository.findById(sessionUser.getId()).get();
        userInDb.setPasswordHash(passwordEncoder.encode(newPass));
        userInDb.setUpdatedAt(LocalDateTime.now());
        userRepository.save(userInDb);

        // 🔥 logout
        session.invalidate();

        // 🔥 về login + thông báo
        return "redirect:/login?changed";
    }
}