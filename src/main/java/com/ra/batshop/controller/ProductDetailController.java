package com.ra.batshop.controller;


import com.ra.batshop.model.*;
import com.ra.batshop.repository.CommentRepository;
import com.ra.batshop.model.FlashSale;
import com.ra.batshop.model.Product;
import com.ra.batshop.model.ProductVariant;
import com.ra.batshop.model.Review;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.FlashSaleRepository;
import com.ra.batshop.repository.ProductRepository;
import com.ra.batshop.repository.ProductVariantRepository;
import com.ra.batshop.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest; // ⭐ NEW
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/product")
public class ProductDetailController {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ReviewRepository reviewRepository;

    private final CommentRepository commentRepository;
    // 1. Khai báo thêm FlashSaleRepository
    private final FlashSaleRepository flashSaleRepository;

    // 2. Inject vào Constructor
    public ProductDetailController(ProductRepository productRepository,
                                   ProductVariantRepository variantRepository,
                                   ReviewRepository reviewRepository,
                                   FlashSaleRepository flashSaleRepository,
                                   CommentRepository commentRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.reviewRepository = reviewRepository;
        this.commentRepository = commentRepository;
        this.flashSaleRepository = flashSaleRepository;
    }


    @GetMapping("/productvariant/list")
    public String ProductVariantList(Model model) {
        model.addAttribute("productvariant", variantRepository.findAll());
        return "user/productvariant-list";
    }


    // =========================
    // PRODUCT DETAIL
    // =========================
    // VIEW PRODUCT DETAIL

    @GetMapping("/detail/{id}")
    public String productDetail(@PathVariable Integer id,
                                @RequestParam(defaultValue = "0") int page, // ⭐ NEW
                                Model model) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductVariant> variants =
                variantRepository.findByProduct_Id(id);
        List<Comment> comments =
                commentRepository
                        .findByProduct_IdAndParentIsNullOrderByCreatedAtDesc(id);
        Page<Review> reviewPage =
                reviewRepository.findByProduct_IdAndParentIsNull(
                        id,
                        PageRequest.of(page,5)
                );

        Double avgRating =
                reviewRepository.getAverageRating(id);
        Long reviewCount = reviewRepository.countByProduct_Id(id);

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("comments",comments);
        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviews", reviewPage.getContent());

        // ==========================================
        // 3. KIỂM TRA LOGIC FLASH SALE TẠI ĐÂY
        // ==========================================
        Optional<FlashSale> activeSale = flashSaleRepository.findActiveFlashSales(LocalDateTime.now()).stream().findFirst();

        if (activeSale.isPresent()) {
            FlashSale sale = activeSale.get();
            // Kiểm tra xem sản phẩm này có nằm trong danh sách Flash Sale không
            boolean isInSale = sale.getProducts().stream()
                    .anyMatch(fsp -> fsp.getProduct().getId().equals(product.getId()));

            if (isInSale) {
                // Nếu có, đẩy object flash sale sang View để Thymeleaf tự động tính giá giảm
                model.addAttribute("activeFlashSale", sale);
            }
        }

        return "user/product-detail";
    }

    // =========================
    // ADD REVIEW
    // =========================
    @PostMapping("/review")
    public String addReview(
            @RequestParam Integer productId,
            @RequestParam String name,
            @RequestParam String phone,
            @RequestParam String message,
            @RequestParam Integer rating
    ) {

        Product product = productRepository
                .findById(productId)
                .orElseThrow();

        Review review = new Review();

        review.setName(name);
        review.setPhone(phone);
        review.setMessage(message);
        review.setRating(rating);
        review.setCreatedAt(LocalDateTime.now());
        review.setProduct(product);

        reviewRepository.save(review);

        return "redirect:/product/detail/" + productId;
    }
}