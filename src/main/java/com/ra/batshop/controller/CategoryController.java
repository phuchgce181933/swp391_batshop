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
        return "admin/category/list";
    }

    // ADD FORM
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category/add";
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
        model.addAttribute("category",
                categoryRepository.findById(id).orElseThrow());
        return "admin/category/edit";
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
            return "admin/category/list";
        }

        categoryRepository.deleteById(id);
        return "redirect:/admin/categories";
    }
}