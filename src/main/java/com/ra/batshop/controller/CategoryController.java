package com.ra.batshop.controller;

import com.ra.batshop.model.Category;
import com.ra.batshop.repository.CategoryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String listCategories(
            @RequestParam(required = false) String keyword,
            Model model,
            HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        List<Category> categories;

        if (keyword != null && !keyword.isEmpty()) {
            categories = categoryRepository.findByNameContainingIgnoreCase(keyword);
        } else {
            categories = categoryRepository.findAll();
        }

        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);

        model.addAttribute("content", "admin/category/list");
        return "admin/layout";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("content", "admin/category/add");
        return "admin/layout";
    }

    // CẬP NHẬT: Xử lý upload ảnh khi thêm mới
    @PostMapping("/add")
    public String save(@ModelAttribute Category category, @RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            try {
                Path uploadDir = Paths.get("uploads/category");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
                Files.copy(file.getInputStream(), uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                category.setImage(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("category", categoryRepository.findById(id).orElseThrow());
        model.addAttribute("content", "admin/category/edit");
        return "admin/layout";
    }

    // CẬP NHẬT: Xử lý upload ảnh khi sửa (Giữ ảnh cũ nếu không up ảnh mới)
    @PostMapping("/edit")
    public String update(@ModelAttribute Category category, @RequestParam("file") MultipartFile file) {
        Category old = categoryRepository.findById(category.getId()).orElseThrow();

        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            try {
                Path uploadDir = Paths.get("uploads/category");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }
                Files.copy(file.getInputStream(), uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                category.setImage(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Nếu không chọn file mới, giữ nguyên file cũ
            category.setImage(old.getImage());
        }

        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

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