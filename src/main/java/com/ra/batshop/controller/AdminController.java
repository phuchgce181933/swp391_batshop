package com.ra.batshop.controller;

import com.ra.batshop.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {
    private final UserRepository userRepository;
    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/admin")
    public String dashboard() {
        return "admin/layout";
    }

    @GetMapping("/admin/manageruser")
    public String userPage(Model model) {
        model.addAttribute("user", userRepository.findAll());
        return "Account-Management";
    }


}