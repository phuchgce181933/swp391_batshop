package com.ra.batshop.controller;

import com.ra.batshop.model.Category;
import com.ra.batshop.repository.CategoryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // LIST
    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("content", "admin/category/list");
        return "admin/layout";
    }

    // ADD FORM
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("content", "admin/category/add");
        return "admin/layout";
    }

    // SAVE
    @PostMapping("/add")
    public String save(@ModelAttribute Category category) {
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("category", categoryRepository.findById(id).orElseThrow());
        model.addAttribute("content", "admin/category/edit");
        return "admin/layout";
    }

    // UPDATE
    @PostMapping("/edit")
    public String update(@ModelAttribute Category category) {
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    // DELETE (CÓ CHECK PRODUCT)
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Model model) {
        Category category = categoryRepository.findById(id).orElseThrow();

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            model.addAttribute("error", "Cannot delete category with products");
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("content", "admin/category/list");
            return "admin/layout";
        }

        categoryRepository.deleteById(id);
        return "redirect:/admin/categories";
    }
}