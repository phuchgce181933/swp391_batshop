package com.ra.batshop.controller;

import com.ra.batshop.model.Banner;
import com.ra.batshop.model.FlashSale;
import com.ra.batshop.model.ProductVariant;
import com.ra.batshop.repository.*;
import com.ra.batshop.model.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequestMapping("/home")
@Controller
public class HomeController {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BlogRepository blogRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BannerRepository bannerRepository;
    // 1. Thêm FlashSaleRepository
    private final FlashSaleRepository flashSaleRepository;

    // 2. Inject vào Constructor
    public HomeController(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          BlogRepository blogRepository,
                          ProductVariantRepository productVariantRepository,
                          BannerRepository bannerRepository,
                          FlashSaleRepository flashSaleRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.blogRepository = blogRepository;
        this.productVariantRepository = productVariantRepository;
        this.bannerRepository = bannerRepository;
        this.flashSaleRepository = flashSaleRepository;
    }
    // ==========================================
    // HÀM TRỢ GIÚP KIỂM TRA FLASH SALE
    // ==========================================
    private void checkAndAddFlashSale(Model model) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Tìm xem có đợt nào ĐANG diễn ra không
        Optional<FlashSale> activeSale = flashSaleRepository.findActiveFlashSales(now).stream().findFirst();        if (activeSale.isPresent()) {
            model.addAttribute("activeFlashSale", activeSale.get());
        } else {
            // 2. Nếu không có đợt nào đang chạy, tìm đợt SẮP diễn ra gần nhất
            Optional<FlashSale> upcomingSale = flashSaleRepository.findFirstByStartDateAfterOrderByStartDateAsc(now);
            if (upcomingSale.isPresent()) {
                model.addAttribute("upcomingFlashSale", upcomingSale.get());
            }
        }
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

        // ===== CHỈ LẤY PRODUCT KHÔNG TRÙNG =====
        List<Product> products = variants.stream()
                .map(ProductVariant::getProduct)
                .distinct()
                .toList();

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("banners", bannerRepository.findAllByStatusTrue());
        model.addAttribute("blogs",
                blogRepository.findTop4ByStatusIsTrueOrderByCreatedAtDesc());

        // 3. Gọi hàm kiểm tra Flash Sale
        checkAndAddFlashSale(model);

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

        // 3. Gọi hàm kiểm tra Flash Sale
        checkAndAddFlashSale(model);

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