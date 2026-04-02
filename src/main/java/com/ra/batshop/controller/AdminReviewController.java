package com.ra.batshop.controller;

import com.ra.batshop.model.Review;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    @ModelAttribute
    public void addActiveMenu(Model model) {
        model.addAttribute("activeMenu", "reviews");
    }

    private final ReviewRepository reviewRepository;

    public AdminReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer rating,
            Model model,
            HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        // SORT: newest first
        PageRequest pageable = PageRequest.of(
                page,
                5,
                Sort.by("createdAt").descending()
        );

        Page<Review> reviewPage;

        // FILTER + SORT
        if (rating != null) {
            reviewPage = reviewRepository
                    .findByParentIsNullAndRating(rating, pageable);
        } else {
            reviewPage = reviewRepository
                    .findByParentIsNull(pageable);
        }

        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("selectedRating", rating); // FIX UI

        model.addAttribute("content", "admin/review/list");

        return "admin/layout";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Integer reviewId,
                         HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        // Xóa cả reply (nếu có)
        reviewRepository.delete(review);

        return "redirect:/admin/reviews";
    }

    @PostMapping("/reply")
    public String reply(@RequestParam Integer reviewId,
                        @RequestParam String message,
                        HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null) {
            return "redirect:/login";
        }

        Review original = reviewRepository
                .findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        Review reply = new Review();

        reply.setName("ADMIN");
        reply.setMessage(message);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setRating(null);

        reply.setProduct(original.getProduct());
        reply.setParent(original);

        reviewRepository.save(reply);

        return "redirect:/admin/reviews";
    }
}