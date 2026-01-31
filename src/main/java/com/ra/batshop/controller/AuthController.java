package com.ra.batshop.controller;


import com.ra.batshop.model.Enum.Role;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("errorEmail", "Email đã tồn tại");
            return "auth/register";
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash())
        );

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
                          @RequestParam String password, Model model, HttpSession session) {
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

        // check mật khẩu cũ
        if (!passwordEncoder.matches(oldPass, user.getPasswordHash())) {
            model.addAttribute("error", "Mật khẩu cũ không đúng");
            return "auth/change-password";
        }

        // check xác nhận
        if (!newPass.equals(confirmPass)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "auth/change-password";
        }

        // cập nhật mật khẩu
        user.setPasswordHash(passwordEncoder.encode(newPass));
        userRepository.save(user);

        // ❗ logout user
        session.invalidate();

        // ✅ redirect về login + flag success
        return "redirect:/login?changed=true";
    }
}