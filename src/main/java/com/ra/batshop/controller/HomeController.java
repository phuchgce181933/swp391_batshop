package com.ra.batshop.controller;

import com.ra.batshop.repository.ProductRepository;
import com.ra.batshop.repository.ProductVariantRepository;
import com.ra.batshop.repository.BannerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/home")
@Controller
public class HomeController {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BannerRepository bannerRepository;

    public HomeController(ProductRepository productRepository, ProductVariantRepository productVariantRepository, BannerRepository bannerRepository) {

        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.bannerRepository = bannerRepository;
    }
    @GetMapping()
    public String home(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        model.addAttribute("banners", bannerRepository.findAllByStatusTrue());
        model.addAttribute("productvariant", productVariantRepository.findAll());
        return "home";
    }

    @GetMapping("/product")
    public String product(Model model) {
            model.addAttribute("products", productRepository.findAll());
            return "user/product";
        }
    }

