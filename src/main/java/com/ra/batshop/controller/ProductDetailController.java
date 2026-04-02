package com.ra.batshop.controller;


import com.ra.batshop.model.*;
import com.ra.batshop.model.Enum.OrderStatus;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ra.batshop.repository.OrderItemRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/product")
public class ProductDetailController {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final CommentRepository commentRepository;
    private final FlashSaleRepository flashSaleRepository;

    public ProductDetailController(ProductRepository productRepository,
                                   ProductVariantRepository variantRepository,
                                   ReviewRepository reviewRepository,
                                   FlashSaleRepository flashSaleRepository,
                                   CommentRepository commentRepository,
                                   OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.reviewRepository = reviewRepository;
        this.commentRepository = commentRepository;
        this.flashSaleRepository = flashSaleRepository;
        this.orderItemRepository = orderItemRepository;
    }


    @GetMapping("/productvariant/list")
    public String ProductVariantList(Model model) {
        model.addAttribute("productvariant", variantRepository.findAll());
        return "user/productvariant-list";
    }



    @GetMapping("/detail/{id}")
    public String productDetail(@PathVariable Integer id,
                                @RequestParam(defaultValue = "0") int page, // ⭐ NEW
                                Model model) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductVariant> variants =
                variantRepository.findByProduct_Id(id);
        // Tạo một map variantId -> racketDetail để dễ hiển thị
        Map<Integer, RacketDetail> racketDetails = variants.stream()
                .filter(v -> v.getRacketDetail() != null)
                .collect(Collectors.toMap(ProductVariant::getId, ProductVariant::getRacketDetail));

        model.addAttribute("racketDetails", racketDetails);
        List<Comment> comments =
                commentRepository
                        .findByProduct_IdAndParentIsNullOrderByCreatedAtDesc(id);
        Page<Review> reviewPage =
                reviewRepository.findByProduct_IdAndParentIsNull(
                        id,
                        PageRequest.of(page, 5)
                );

        Double avgRating = reviewRepository.getAverageRating(id);
        Long reviewCount = reviewRepository.countRootReview(id);

        if (reviewCount == null) {
            reviewCount = 0L;
        }

        if (avgRating == null) {
            avgRating = 0.0;
        }

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("comments", comments);
        model.addAttribute("reviews", reviewPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("avgRating", avgRating);
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("racketDetails", racketDetails);
        Optional<FlashSale> activeSale = flashSaleRepository.findActiveFlashSales(LocalDateTime.now()).stream().findFirst();

        if (activeSale.isPresent()) {
            FlashSale sale = activeSale.get();
            boolean isInSale = sale.getProducts().stream()
                    .anyMatch(fsp -> fsp.getProduct().getId().equals(product.getId()));

            if (isInSale) {
                model.addAttribute("activeFlashSale", sale);
            }
        }

        return "user/product/product-detail";
    }

    @PostMapping("/review")
    public String addReview(
            @RequestParam Integer productId,
            @RequestParam String message,
            @RequestParam Integer rating,
            HttpSession session
    ) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Product product = productRepository
                .findById(productId)
                .orElseThrow();

        Long purchased = orderItemRepository
                .checkUserPurchased(user.getId(), productId, OrderStatus.COMPLETED);

        if (purchased == 0) {
            return "redirect:/product/detail/" + productId + "?error=notPurchased";
        }

        boolean existed = reviewRepository
                .existsByUser_IdAndProduct_Id(user.getId(), productId);

        if (existed) {
            return "redirect:/product/detail/" + productId + "?error=reviewed";
        }

        Review review = new Review();

        review.setUser(user);
        review.setProduct(product);
        review.setMessage(message);
        review.setRating(rating);
        review.setCreatedAt(LocalDateTime.now());

        review.setName(user.getFullName());
        review.setPhone(user.getPhone());

        review.setVerifiedPurchase(true);

        reviewRepository.save(review);

        return "redirect:/product/detail/" + productId;
    }
}