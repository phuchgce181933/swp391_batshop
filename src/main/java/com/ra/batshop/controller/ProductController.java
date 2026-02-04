package com.ra.batshop.controller;

import com.ra.batshop.model.Product;
//import com.ra.batshop.repository.BrandRepository;
import com.ra.batshop.repository.CategoryRepository;
import com.ra.batshop.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
//    private final BrandRepository brandRepository;

    public ProductController(ProductRepository productRepository,
                                  CategoryRepository categoryRepository) {
//                                  BrandRepository brandRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
//        this.brandRepository = brandRepository;
    }

    // LIST
    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("content", "admin/product/list");
        return "admin/layout";
    }

    // ADD FORM
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
//        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("content", "admin/product/add");
        return "admin/layout";
    }

    // SAVE
    @PostMapping("/add")
    public String save(@ModelAttribute Product product) {
        product.setCreatedAt(LocalDateTime.now());
        product.setStatus(true);
        productRepository.save(product);
        return "redirect:/admin/products";
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("product", productRepository.findById(id).orElseThrow());
        model.addAttribute("categories", categoryRepository.findAll());
//        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("content", "admin/product/edit");
        return "admin/layout";
    }

    // UPDATE
    @PostMapping("/edit")
    public String update(@ModelAttribute Product product) {
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
        return "redirect:/admin/products";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        productRepository.deleteById(id);
        return "redirect:/admin/products";
    }
}