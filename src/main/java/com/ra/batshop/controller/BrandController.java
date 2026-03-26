package com.ra.batshop.controller;

import com.ra.batshop.model.Brand;
import com.ra.batshop.repository.BrandRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/brands")
public class BrandController {

    @ModelAttribute
    public void addActiveMenu(Model model) {
        model.addAttribute("activeMenu", "brands");
    }

    private final BrandRepository brandRepository;

    public BrandController(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    // LIST
    @GetMapping
    public String listBrands(@RequestParam(required = false) String keyword,
                             Model model,
                             HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        List<Brand> brands;

        if (keyword != null && !keyword.trim().isEmpty()) {
            brands = brandRepository.findByNameContainingIgnoreCase(keyword.trim());
        } else {
            brands = brandRepository.findAll();
        }

        model.addAttribute("brands", brands);
        model.addAttribute("keyword", keyword);
        model.addAttribute("content", "admin/brand/list");

        return "admin/layout";
    }

    // ADD FORM
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        model.addAttribute("brand", new Brand());
        model.addAttribute("content", "admin/brand/add");
        return "admin/layout";
    }

    // ADD
    @PostMapping("/add")
    public String add(@ModelAttribute Brand brand,
                      Model model,
                      HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        if (brand.getName() == null || brand.getName().trim().isEmpty()) {
            model.addAttribute("error", "Brand name cannot be empty");
            model.addAttribute("brand", brand);
            model.addAttribute("content", "admin/brand/add");
            return "admin/layout";
        }

        String name = brand.getName().trim();

        if (brandRepository.existsByNameIgnoreCase(name)) {
            model.addAttribute("error", "Brand name already exists");
            model.addAttribute("brand", brand);
            model.addAttribute("content", "admin/brand/add");
            return "admin/layout";
        }

        brand.setName(name);

        if (brand.getStatus() == null) {
            brand.setStatus(true);
        }

        brandRepository.save(brand);
        return "redirect:/admin/brands";
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        model.addAttribute("brand", brandRepository.findById(id).orElseThrow());
        model.addAttribute("content", "admin/brand/edit");
        return "admin/layout";
    }

    // UPDATE
    @PostMapping("/edit")
    public String edit(@ModelAttribute Brand brand,
                       Model model,
                       HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        if (brand.getName() == null || brand.getName().trim().isEmpty()) {
            model.addAttribute("error", "Brand name cannot be empty");
            model.addAttribute("brand", brand);
            model.addAttribute("content", "admin/brand/edit");
            return "admin/layout";
        }

        String name = brand.getName().trim();

        Brand existed = brandRepository.findByNameIgnoreCase(name);

        if (existed != null && !existed.getId().equals(brand.getId())) {
            model.addAttribute("error", "Brand name already exists");
            model.addAttribute("brand", brand);
            model.addAttribute("content", "admin/brand/edit");
            return "admin/layout";
        }

        brand.setName(name);

        brandRepository.save(brand);
        return "redirect:/admin/brands";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        brandRepository.deleteById(id);
        return "redirect:/admin/brands";
    }
}