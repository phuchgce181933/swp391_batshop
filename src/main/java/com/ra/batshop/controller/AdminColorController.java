package com.ra.batshop.controller;

import com.ra.batshop.model.Color;
import com.ra.batshop.repository.ColorRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // Cần thiết để gửi thông báo

import java.util.Optional;

@Controller
@RequestMapping("/admin/colors")
public class AdminColorController {

    @ModelAttribute
    public void addActiveMenu(Model model) {
        model.addAttribute("activeMenu", "colors");
    }

    @Autowired
    private ColorRepository colorRepository;

    @GetMapping
    public String listColors(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        model.addAttribute("colors", colorRepository.findAll());
        model.addAttribute("content", "admin/color/list");
        return "admin/layout";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        if (!model.containsAttribute("color")) {
            model.addAttribute("color", new Color());
        }
        model.addAttribute("content", "admin/color/add");
        return "admin/layout";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute("color") Color color, RedirectAttributes redirectAttributes) {
        // 1. Check duplicate name
        Optional<Color> existing = colorRepository.findByName(color.getName());
        if (existing.isPresent()) {
            if (color.getId() == null || !color.getId().equals(existing.get().getId())) {
                redirectAttributes.addFlashAttribute("error", "Color name '" + color.getName() + "' already exists!");
                return color.getId() == null ? "redirect:/admin/colors/add" : "redirect:/admin/colors/edit/" + color.getId();
            }
        }
        colorRepository.save(color);
        redirectAttributes.addFlashAttribute("success", "Color saved successfully!");
        return "redirect:/admin/colors";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            colorRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Color deleted successfully!");
        } catch (Exception e) {
            // ĐÂY LÀ CHỖ BẮT LỖI KHI CÓ NGƯỜI ĐẶT HÀNG
            redirectAttributes.addFlashAttribute("error", "Cannot delete! This color is already linked to products or orders.");
        }
        return "redirect:/admin/colors";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Color color = colorRepository.findById(id).orElseThrow();
        model.addAttribute("color", color);
        model.addAttribute("content", "admin/color/add");
        return "admin/layout";
    }
}