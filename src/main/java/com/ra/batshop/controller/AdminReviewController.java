package com.ra.batshop.controller;

import com.ra.batshop.model.Enum.Role;
import com.ra.batshop.model.Review;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewRepository reviewRepository;

    public AdminReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    // =========================
    // LIST REVIEW + PAGINATION
    // =========================
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            HttpSession session) {

        User admin = (User) session.getAttribute("user");

        if (admin == null ||
                (admin.getRole() != Role.ADMIN && admin.getRole() != Role.STAFF)) {
            return "redirect:/login";
        }

        Page<Review> reviewPage =
                reviewRepository.findByParentIsNull(
                        PageRequest.of(page,5));

        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());

        model.addAttribute("content", "admin/review/list.html");

        return "admin/layout";
    }

    // =========================
    // REPLY REVIEW
    // =========================
    @PostMapping("/reply")
    public String reply(@RequestParam Integer reviewId,
                        @RequestParam String message,
                        HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null ||
                (user.getRole() != Role.ADMIN && user.getRole() != Role.STAFF)) {
            return "redirect:/login";
        }

        Review original = reviewRepository
                .findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        Review reply = new Review();

        if (user.getRole() == Role.ADMIN) {
            reply.setName("ADMIN");
        } else {
            reply.setName("STAFF");
        }

        reply.setMessage(message);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setRating(null);

        reply.setProduct(original.getProduct());
        reply.setParent(original);

        reviewRepository.save(reply);

        return "redirect:/admin/reviews";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Integer reviewId,
                         HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (!isAdmin(user)) {
            return "redirect:/admin/reviews";
        }

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        reviewRepository.delete(review);

        return "redirect:/admin/reviews";
    }
    private boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    private boolean isStaff(User user) {
        return user != null && user.getRole() == Role.STAFF;
    }
}