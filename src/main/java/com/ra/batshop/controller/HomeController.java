package com.ra.batshop.controller;

import com.ra.batshop.model.Banner;
import com.ra.batshop.repository.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
    public String home(Model model, HttpSession session) {
//        if (session.getAttribute("user") == null) {
//            return "redirect:/login";
//        }

        // 3. Lấy danh sách banner active
        List<Banner> banners = bannerRepository.findAllByStatusTrue();
        model.addAttribute("banners", banners);
        model.addAttribute("productvariant", productVariantRepository.findAll());
        model.addAttribute("blogs", blogRepository.findTop4ByStatusIsTrueOrderByCreatedAtDesc());
        model.addAttribute("categories", categoryRepository.findAll());
        return "home";
    }

    @GetMapping("/product")
    public String product(Model model) {
        model.addAttribute("products", productRepository.findAll());
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