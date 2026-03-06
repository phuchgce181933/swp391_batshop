package com.ra.batshop.controller;

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
    // 1. Khai báo thêm FlashSaleRepository
    private final FlashSaleRepository flashSaleRepository;

    // 2. Inject vào Constructor
    public ProductDetailController(ProductRepository productRepository,
                                   ProductVariantRepository variantRepository,
                                   ReviewRepository reviewRepository,
                                   FlashSaleRepository flashSaleRepository) {
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.reviewRepository = reviewRepository;
        this.flashSaleRepository = flashSaleRepository;
    }

    @GetMapping("/productvariant/list")
    public String ProductVariantList(Model model) {
        model.addAttribute("productvariant", variantRepository.findAll());
        return ("user/productvariant-list");
    }

    // VIEW PRODUCT DETAIL
    @GetMapping("/detail/{id}")
    public String productDetail(@PathVariable Integer id, Model model) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductVariant> variants = variantRepository.findByProduct_Id(id);

        List<Review> reviews =
                reviewRepository.findByProduct_IdOrderByCreatedAtDesc(id);

        model.addAttribute("product", product);
        model.addAttribute("variants", variants);
        model.addAttribute("reviews", reviews);

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

    // ADD REVIEW
    @PostMapping("/review")
    public String addReview(@RequestParam Integer productId,
                            @RequestParam String title,
                            @RequestParam String content,
                            HttpSession session) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            // quay lại trang detail + báo lỗi
            return "redirect:/product/detail/" + productId + "?error=true";
        }

        Product product =
                productRepository.findById(productId).orElseThrow();

        Review review = new Review();
        review.setTitle(title);
        review.setContent(content);
        review.setCreatedAt(LocalDateTime.now());
        review.setUser(user);
        review.setProduct(product);

        reviewRepository.save(review);

        return "redirect:/product/detail/" + productId;
    }
}