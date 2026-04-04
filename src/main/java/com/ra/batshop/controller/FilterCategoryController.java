package com.ra.batshop.controller;

import com.ra.batshop.model.Brand;
import com.ra.batshop.model.Category;
import com.ra.batshop.model.Product;
import com.ra.batshop.repository.BrandRepository;
import com.ra.batshop.repository.CategoryRepository;
import com.ra.batshop.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class FilterCategoryController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public FilterCategoryController(ProductRepository productRepository,
                                    CategoryRepository categoryRepository,
                                    BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    @GetMapping("/category")
    public String showCategoryPage(
            @RequestParam(name = "id", required = false) Integer categoryId,
            @RequestParam(name = "price", required = false) List<String> prices,
            @RequestParam(name = "brand", required = false) List<Long> brandIds,
            @RequestParam(name = "sort", defaultValue = "default") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            Model model) {

        // 1. Xử lý Sắp xếp (Sort) cho các trường hợp cơ bản
        Sort sortOrder = Sort.unsorted();
        switch (sort) {
            case "price_asc":
                sortOrder = Sort.by(Sort.Direction.ASC, "price");
                break;
            case "price_desc":
                sortOrder = Sort.by(Sort.Direction.DESC, "price");
                break;
            case "newest":
                sortOrder = Sort.by(Sort.Direction.DESC, "id");
                break;
            // Không xử lý "best_selling" ở đây vì nó sẽ được xử lý riêng bằng Query Custom
        }

        Pageable pageable = PageRequest.of(page, 12, sortOrder);

// 2. Xử lý Khoảng giá (Min / Max) hỗ trợ chọn nhiều checkbox cùng lúc
        BigDecimal minPrice = new BigDecimal("999999999999"); // Khởi tạo min rất lớn
        BigDecimal maxPrice = BigDecimal.ZERO; // Khởi tạo max rất nhỏ

        if (prices != null && !prices.isEmpty()) {
            if (prices.contains("under-500")) {
                minPrice = minPrice.min(BigDecimal.ZERO);
                maxPrice = maxPrice.max(new BigDecimal("500000"));
            }
            if (prices.contains("500-1000")) {
                minPrice = minPrice.min(new BigDecimal("500000"));
                maxPrice = maxPrice.max(new BigDecimal("1000000"));
            }
            if (prices.contains("1000-2000")) {
                minPrice = minPrice.min(new BigDecimal("1000000"));
                maxPrice = maxPrice.max(new BigDecimal("2000000"));
            }
            if (prices.contains("2000-3000")) {
                minPrice = minPrice.min(new BigDecimal("2000000"));
                maxPrice = maxPrice.max(new BigDecimal("3000000"));
            }
            if (prices.contains("over-3000")) {
                minPrice = minPrice.min(new BigDecimal("3000000"));
                maxPrice = maxPrice.max(new BigDecimal("999999999999"));
            }
        } else {
            // Nếu không chọn gì thì lấy tất cả
            minPrice = BigDecimal.ZERO;
            maxPrice = new BigDecimal("999999999999");
        }

        // 3. Truy vấn DB lấy Product (XỬ LÝ LUỒNG RIÊNG CHO BEST_SELLING)
        Page<Product> productPage;

        if ("best_selling".equals(sort)) {
            // Nếu chọn Bán chạy nhất -> Gọi hàm chứa Query GROUP BY SUM()
            productPage = productRepository.filterAndSortBestSellingProducts(categoryId, brandIds, minPrice, maxPrice, pageable);
        } else {
            // Các trường hợp khác (Mặc định, Giá tăng, Giá giảm, Mới nhất) -> Gọi hàm cũ
            productPage = productRepository.filterProducts(categoryId, brandIds, minPrice, maxPrice, pageable);
        }

        // 4. Lấy Category để làm Breadcrumb
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            model.addAttribute("category", category);
        }

        // 5. Lấy danh sách Brand có trạng thái hoạt động
        List<Brand> activeBrands = brandRepository.findByStatusTrue();
        model.addAttribute("brands", activeBrands);

        // THÊM ĐOẠN NÀY ĐỂ LẤY TẤT CẢ DANH MỤC CHO SIDEBAR:
        List<Category> allCategories = categoryRepository.findAll();
        model.addAttribute("categories", allCategories);

        // 6. Đẩy dữ liệu ra View
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("currentSort", sort);

        model.addAttribute("selectedPrices", prices);
        model.addAttribute("selectedBrands", brandIds);

        return "user/category/category";
    }
    @GetMapping("/about")
    public String showAboutPage() {
        return "about";
    }
}