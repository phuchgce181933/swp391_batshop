package com.ra.batshop.controller;

import com.ra.batshop.model.Product;
import com.ra.batshop.repository.BrandRepository;
import com.ra.batshop.repository.CategoryRepository;
import com.ra.batshop.repository.ProductRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
public class ProductListController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public ProductListController(ProductRepository productRepository,
                                 CategoryRepository categoryRepository,
                                 BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    @GetMapping
    public String listProducts(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "brandId", required = false) Integer brandId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", defaultValue = "default") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "8") int size,
            Model model) {

        Sort sortObj;

        if (sort.equals("priceAsc")) {
            sortObj = Sort.by("price").ascending();
        } else if (sort.equals("priceDesc")) {
            sortObj = Sort.by("price").descending();
        } else {
            sortObj = Sort.by("id").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<Product> productPage;

        if (keyword != null && !keyword.isEmpty()) {
            productPage = productRepository
                    .findByNameContainingIgnoreCaseAndStatusTrue(keyword, pageable);
        }
        else if (categoryId != null && brandId != null) {
            productPage = productRepository
                    .findByCategory_IdAndBrand_IdAndStatusTrue(categoryId, brandId, pageable);
        }
        else if (categoryId != null) {
            productPage = productRepository
                    .findByCategory_IdAndStatusTrue(categoryId, pageable);
        }
        else if (brandId != null) {
            productPage = productRepository
                    .findByBrand_IdAndStatusTrue(brandId, pageable);
        }
        else {
            productPage = productRepository
                    .findByStatusTrue(pageable);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());

        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedBrand", brandId);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("keyword", keyword);

        return "user/product-list";
    }
}