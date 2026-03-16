package com.ra.batshop.controller;

import com.ra.batshop.model.Size;
import com.ra.batshop.repository.SizeRepository;
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
    public String listSizes(Model model) {
        model.addAttribute("sizes", sizeRepository.findAll());
        return "admin/size/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("size", new Size());
        return "admin/size/add";
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
        return "admin/size/add";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        sizeRepository.deleteById(id);
        return "redirect:/admin/sizes";
    }
}