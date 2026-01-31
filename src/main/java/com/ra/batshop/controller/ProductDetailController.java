package com.ra.batshop.controller;

import com.ra.batshop.model.Product;
import com.ra.batshop.model.ProductVariant;
import com.ra.batshop.repository.ProductRepository;
import com.ra.batshop.repository.ProductVariantRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductDetailController {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    public ProductDetailController(ProductRepository productRepository,
                                   ProductVariantRepository variantRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }
    @GetMapping("/productvariant/list")
    public String ProductVariantList(Model model) {
        model.addAttribute("productvariant", variantRepository.findAll());
        return ("user/productvariant-list");
    }
    // VIEW PRODUCT DETAIL
    @GetMapping("/detail/{id}")
    public String productDetail(@PathVariable Integer id, Model model) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductVariant> variants = variantRepository.findByProduct_Id(id);

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);

        return "user/product-detail";
    }
}
