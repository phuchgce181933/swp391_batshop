package com.ra.batshop.controller;

import com.ra.batshop.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/home")
@Controller
public class HomeController {
    private final ProductRepository productRepository;
    public HomeController(ProductRepository productRepository) {

        this.productRepository = productRepository;
    }
    @GetMapping()
    public String home(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        return "home";
    }

    @GetMapping("/product")
    public String product(Model model) {
            model.addAttribute("products", productRepository.findAll());
            return "user/product";
        }
    }

