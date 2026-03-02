package com.ra.batshop.controller;

import com.ra.batshop.model.Banner;
import com.ra.batshop.model.ProductVariant;
import com.ra.batshop.repository.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/home")
@Controller
public class HomeController {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BlogRepository blogRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BannerRepository bannerRepository; // 1. Khai báo

    // 2. Inject vào Constructor
    public HomeController(ProductRepository productRepository, CategoryRepository categoryRepository, BlogRepository blogRepository,
                          ProductVariantRepository productVariantRepository,
                          BannerRepository bannerRepository
                          ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.blogRepository = blogRepository;
        this.productVariantRepository = productVariantRepository;
        this.bannerRepository = bannerRepository;

    }

    @GetMapping()
    public String home(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            Model model) {

        List<ProductVariant> variants;

        if (categoryId != null) {
            variants = productVariantRepository
                    .findByProduct_Category_IdAndProduct_StatusTrue(categoryId);
        } else {
            variants = productVariantRepository
                    .findByProduct_StatusTrue();
        }

        model.addAttribute("productvariant", variants);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("banners", bannerRepository.findAllByStatusTrue());
        model.addAttribute("blogs",
                blogRepository.findTop4ByStatusIsTrueOrderByCreatedAtDesc());

        return "home";
    }

    @GetMapping("/product")
    public String product(
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            Model model) {

        if (categoryId != null) {
            model.addAttribute("products",
                    productRepository.findByCategory_Id(categoryId));
        } else {
            model.addAttribute("products",
                    productRepository.findAll());
        }

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategory", categoryId);

        return "user/product";
    }
    // LIST
    @GetMapping("/category")
    public String listCategory(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "user/category/list";
    }

    @GetMapping("/blog")
    public String listBlog(Model model) {
        model.addAttribute("blogs", blogRepository.findAll());

       return "user/blog/list";

    }
}