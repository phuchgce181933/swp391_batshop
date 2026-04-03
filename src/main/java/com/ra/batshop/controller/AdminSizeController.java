package com.ra.batshop.controller;

import com.ra.batshop.model.Size;
import com.ra.batshop.repository.SizeRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/sizes")
public class AdminSizeController {

    @ModelAttribute
    public void addActiveMenu(Model model) {
        model.addAttribute("activeMenu", "sizes");
    }

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
        if (!model.containsAttribute("size")) {
            model.addAttribute("size", new Size());
        }
        model.addAttribute("content", "admin/size/add");
        return "admin/layout";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Size size, RedirectAttributes redirectAttributes) {
        // EXCEPTION: Validation for numbers only (integers or decimals)
        if (size.getName() == null || !size.getName().matches("^\\d+(\\.\\d+)?$")) {
            redirectAttributes.addFlashAttribute("error", "Invalid size! Please enter a number (e.g., 42, 43.5).");
            return size.getId() == null ? "redirect:/admin/sizes/add" : "redirect:/admin/sizes/edit/" + size.getId();
        }

        Optional<Size> existing = sizeRepository.findByName(size.getName());
        if (existing.isPresent()) {
            if (size.getId() == null || !size.getId().equals(existing.get().getId())) {
                redirectAttributes.addFlashAttribute("error", "Size '" + size.getName() + "' already exists!");
                return size.getId() == null ? "redirect:/admin/sizes/add" : "redirect:/admin/sizes/edit/" + size.getId();
            }
        }

        sizeRepository.save(size);
        redirectAttributes.addFlashAttribute("success", "Size saved successfully!");
        return "redirect:/admin/sizes";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            sizeRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Size deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete! This size is linked to existing products.");
        }
        return "redirect:/admin/sizes";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Size size = sizeRepository.findById(id).orElseThrow();
        model.addAttribute("size", size);
        model.addAttribute("content", "admin/size/add");
        return "admin/layout";
    }
}