package com.ra.batshop.controller;

import com.ra.batshop.model.User;
import com.ra.batshop.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("content", "admin/dashboard-content");
        return "admin/layout";
    }

//    @GetMapping("")
//    public String dashboard(Model model) {
//        model.addAttribute("content", "admin/dashboard");
//        return "admin/layout";
//    }

    @GetMapping("/manageruser")
    public String userPage(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("content", "admin/user/list");
        return "admin/layout";
    }

}