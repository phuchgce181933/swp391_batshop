package com.ra.batshop.controller;

import com.ra.batshop.model.Product;
import com.ra.batshop.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product")
public class ProductDetailController {

    private final ProductRepository productRepository;

    public ProductDetailController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // View product detail
    @GetMapping("/detail/{id}")
    public String viewProductDetail(@PathVariable Integer id, Model model) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        model.addAttribute("product", product);
        return "user/product-detail";
    }
}
