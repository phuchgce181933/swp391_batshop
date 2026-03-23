package com.ra.batshop.controller;

import com.ra.batshop.model.Size;
import com.ra.batshop.repository.SizeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/sizes")
public class AdminSizeController {

    @Autowired
    private SizeRepository sizeRepository;

    @GetMapping
    public String listSizes(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("content", "admin/size/list");
        return "admin/layout";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("size", new Size());
        model.addAttribute("content", "admin/size/add");
        return "admin/layout";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("size") Size size) {
        sizeRepository.save(size);
        return "redirect:/admin/sizes";
    }

    // Dùng Integer để khớp với toàn bộ hệ thống cũ của ông
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Size size = sizeRepository.findById(id).orElseThrow();
        model.addAttribute("size", size);
        model.addAttribute("content", "admin/size/add");
        return "admin/layout";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        sizeRepository.deleteById(id);
        return "redirect:/admin/sizes";
    }
}