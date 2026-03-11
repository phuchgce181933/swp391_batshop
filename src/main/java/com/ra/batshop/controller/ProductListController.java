package com.ra.batshop.controller;

import com.ra.batshop.model.Enum.RacketLevel;
import com.ra.batshop.model.Enum.RacketStyle;
import com.ra.batshop.model.Enum.RacketWeight;
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

            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) String keyword,

            @RequestParam(required = false) RacketLevel level,
            @RequestParam(required = false) RacketWeight weight,
            @RequestParam(required = false) RacketStyle style,

            @RequestParam(defaultValue = "default") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,

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

        Page<Product> productPage =
                productRepository.filterAllProducts(
                        categoryId,
                        brandId,
                        level,
                        weight,
                        style,
                        keyword,
                        pageable
                );

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());

        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("selectedBrand", brandId);
        model.addAttribute("selectedLevel", level);
        model.addAttribute("selectedWeight", weight);
        model.addAttribute("selectedStyle", style);
        model.addAttribute("racketLevels", RacketLevel.values());
        model.addAttribute("racketWeights", RacketWeight.values());
        model.addAttribute("racketStyles", RacketStyle.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedSort", sort);

        model.addAttribute("racketLevels", RacketLevel.values());

        return "user/product-list";
    }
}