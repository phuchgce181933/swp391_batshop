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

    @ModelAttribute
    public void addActiveMenu(Model model) {
        model.addAttribute("activeMenu", "categories");
    }

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // LIST + SEARCH
    @GetMapping
    public String listCategories(@RequestParam(required = false) String keyword,
                                 Model model,
                                 HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        List<Category> categories;

        if (keyword != null && !keyword.trim().isEmpty()) {
            categories = categoryRepository.findByNameContainingIgnoreCase(keyword.trim());
        } else {
            categories = categoryRepository.findAll();
        }

        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("content", "admin/category/list");

        return "admin/layout";
    }

    // FORM ADD
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        model.addAttribute("category", new Category());
        model.addAttribute("content", "admin/category/add");
        return "admin/layout";
    }

    // ADD CATEGORY
    @PostMapping("/add")
    public String save(@ModelAttribute Category category,
                       @RequestParam("file") MultipartFile file,
                       Model model,
                       HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        // Validate name
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            model.addAttribute("error", "Tên category không được để trống");
            model.addAttribute("category", category);
            model.addAttribute("content", "admin/category/add");
            return "admin/layout";
        }

        String name = category.getName().trim();

        // Check trùng name
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            model.addAttribute("error", "Tên category đã tồn tại");
            model.addAttribute("category", category);
            model.addAttribute("content", "admin/category/add");
            return "admin/layout";
        }

        category.setName(name);

        // Upload ảnh
        if (!file.isEmpty()) {
            try {
                String fileName = file.getOriginalFilename();
                Path uploadDir = Paths.get("uploads/category");

                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                Files.copy(file.getInputStream(),
                        uploadDir.resolve(fileName),
                        StandardCopyOption.REPLACE_EXISTING);

                category.setImage(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            model.addAttribute("error", "Vui lòng chọn ảnh");
            model.addAttribute("category", category);
            model.addAttribute("content", "admin/category/add");
            return "admin/layout";
        }

        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    // FORM EDIT
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id,
                           Model model,
                           HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        Category category = categoryRepository.findById(id).orElseThrow();

        model.addAttribute("category", category);
        model.addAttribute("content", "admin/category/edit");

        return "admin/layout";
    }

    // UPDATE CATEGORY
    @PostMapping("/edit")
    public String update(@ModelAttribute Category category,
                         @RequestParam("file") MultipartFile file,
                         Model model,
                         HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        Category old = categoryRepository.findById(category.getId()).orElseThrow();

        // Validate name
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            model.addAttribute("error", "Tên category không được để trống");
            model.addAttribute("category", category);
            model.addAttribute("content", "admin/category/edit");
            return "admin/layout";
        }

        String name = category.getName().trim();

        Category existed = categoryRepository.findByNameIgnoreCase(name);

        if (existed != null && !existed.getId().equals(category.getId())) {
            model.addAttribute("error", "Tên category đã tồn tại");
            model.addAttribute("category", category);
            model.addAttribute("content", "admin/category/edit");
            return "admin/layout";
        }

        category.setName(name);

        // Upload ảnh mới
        if (!file.isEmpty()) {
            try {
                String fileName = file.getOriginalFilename();
                Path uploadDir = Paths.get("uploads/category");

                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                Files.copy(file.getInputStream(),
                        uploadDir.resolve(fileName),
                        StandardCopyOption.REPLACE_EXISTING);

                category.setImage(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            category.setImage(old.getImage());
        }

        categoryRepository.save(category);
        return "redirect:/admin/categories";
    }

    // DELETE CATEGORY
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id,
                         Model model,
                         HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        Category category = categoryRepository.findById(id).orElseThrow();

        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            model.addAttribute("error", "Không thể xóa category vì còn sản phẩm");
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("content", "admin/category/list");
            return "admin/layout";
        }

        categoryRepository.deleteById(id);
        return "redirect:/admin/categories";
    }
}