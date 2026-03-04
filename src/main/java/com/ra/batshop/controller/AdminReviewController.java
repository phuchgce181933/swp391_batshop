package com.ra.batshop.controller;

import com.ra.batshop.model.Review;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewRepository reviewRepository;

    public AdminReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // =========================
    // LIST REVIEW
    // =========================
    @GetMapping
    public String list(Model model, HttpSession session) {

        User admin = (User) session.getAttribute("user");

        // Kiểm tra đăng nhập admin
        if (admin == null) {
            return "redirect:/login";
        }

        List<Review> reviews = reviewRepository.findAll();

        model.addAttribute("reviews", reviews);

        // truyền path view trực tiếp (không dùng fragment)
        model.addAttribute("content", "admin/review/list");

        return "admin/layout";
    }

    // =========================
    // REPLY REVIEW
    // =========================
    @PostMapping("/reply")
    public String reply(@RequestParam Integer reviewId,
                        @RequestParam String content,
                        HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        Review original = reviewRepository.findById(reviewId)
                .orElseThrow();

        Review reply = new Review();
        reply.setTitle("ADMIN_REPLY");
        reply.setContent(content);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUser(admin);
        reply.setProduct(original.getProduct());

        reviewRepository.save(reply);

        return "redirect:/admin/reviews";
    }
}