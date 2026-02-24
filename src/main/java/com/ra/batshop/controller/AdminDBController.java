package com.ra.batshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

@Controller
public class AdminDBController {

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("content", "admin/dashboard-content");
        return "admin/layout";
    }
}