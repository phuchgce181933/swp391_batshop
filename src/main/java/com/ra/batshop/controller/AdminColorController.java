package com.ra.batshop.controller;

import com.ra.batshop.model.Color;
import com.ra.batshop.repository.ColorRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // PHẢI dùng import này
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/colors") // Thêm chữ 's' cho chuẩn REST
public class AdminColorController {

    @Autowired
    private ColorRepository colorRepository;

    @GetMapping
    public String listColors(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        model.addAttribute("colors", colorRepository.findAll());
        model.addAttribute("content", "admin/color/list");
        return "admin/layout"; // Đường dẫn tới file HTML
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("color", new Color());
        model.addAttribute("content", "admin/color/add");
        return "admin/layout";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("color") Color color) {
        colorRepository.save(color);
        return "redirect:/admin/colors";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) { // Dùng Integer cho khớp Repo
        colorRepository.deleteById(id);
        return "redirect:/admin/colors";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Color color = colorRepository.findById(id).orElse(null);
        model.addAttribute("color", color);
        model.addAttribute("content", "admin/color/add");
        return "admin/layout";
    }
}