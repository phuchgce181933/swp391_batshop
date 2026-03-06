package com.ra.batshop.controller;

import com.ra.batshop.model.FlashSale;
import com.ra.batshop.model.FlashSaleProduct;
import com.ra.batshop.model.Product;
import com.ra.batshop.repository.FlashSaleProductRepository;
import com.ra.batshop.repository.FlashSaleRepository;
import com.ra.batshop.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class FlashSaleController {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleProductRepository flashSaleProductRepository;
    private final ProductRepository productRepository;

    // Cập nhật constructor để Inject cả 3 Repository
    public FlashSaleController(FlashSaleRepository flashSaleRepository,
                               FlashSaleProductRepository flashSaleProductRepository,
                               ProductRepository productRepository) {
        this.flashSaleRepository = flashSaleRepository;
        this.flashSaleProductRepository = flashSaleProductRepository;
        this.productRepository = productRepository;
    }

    // =======================================================
    // 1. KHU VỰC DÀNH CHO NGƯỜI DÙNG (END-USER)
    // =======================================================
    @GetMapping("/flash-sale")
    public String flashSalePage(Model model) {
        Optional<FlashSale> activeSale = flashSaleRepository.findActiveFlashSale(LocalDateTime.now());

        if (activeSale.isPresent()) {
            model.addAttribute("flashSale", activeSale.get());
            model.addAttribute("flashSaleProducts", activeSale.get().getProducts());
        } else {
            model.addAttribute("flashSale", null);
        }

        return "user/flash-sale";
    }


    // =======================================================
    // 2. KHU VỰC DÀNH CHO ADMIN (QUẢN LÝ FLASH SALE)
    // =======================================================

    // DANH SÁCH FLASH SALE
    @GetMapping("/admin/flash-sales")
    public String list(Model model) {
        model.addAttribute("flashSales", flashSaleRepository.findAll());
        model.addAttribute("content", "admin/flash-sale/list");
        return "admin/layout";
    }

    // FORM THÊM MỚI
    @GetMapping("/admin/flash-sales/add")
    public String addForm(Model model) {
        model.addAttribute("flashSale", new FlashSale());
        model.addAttribute("content", "admin/flash-sale/add");
        return "admin/layout";
    }

    // LƯU FLASH SALE MỚI
    @PostMapping("/admin/flash-sales/add")
    public String save(@ModelAttribute FlashSale flashSale) {
        flashSaleRepository.save(flashSale);
        return "redirect:/admin/flash-sales";
    }

    // CHI TIẾT & FORM SỬA, THÊM SẢN PHẨM VÀO SALE
    @GetMapping("/admin/flash-sales/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        FlashSale flashSale = flashSaleRepository.findById(id).orElseThrow();

        model.addAttribute("flashSale", flashSale);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("content", "admin/flash-sale/edit");

        return "admin/layout";
    }

    // CẬP NHẬT THÔNG TIN CƠ BẢN CỦA ĐỢT SALE (Thời gian, %)
    @PostMapping("/admin/flash-sales/edit")
    public String update(@ModelAttribute FlashSale flashSale) {
        FlashSale existing = flashSaleRepository.findById(flashSale.getId()).orElseThrow();
        existing.setDiscountPercent(flashSale.getDiscountPercent());
        existing.setStartDate(flashSale.getStartDate());
        existing.setEndDate(flashSale.getEndDate());
        flashSaleRepository.save(existing);

        return "redirect:/admin/flash-sales/edit/" + flashSale.getId();
    }

    // THÊM SẢN PHẨM VÀO FLASH SALE VÀ TỰ ĐỘNG TÍNH GIÁ
    @PostMapping("/admin/flash-sales/{id}/add-product")
    public String addProductToFlashSale(@PathVariable("id") Integer flashSaleId,
                                        @RequestParam("productId") Integer productId) {
        FlashSale flashSale = flashSaleRepository.findById(flashSaleId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        // Kiểm tra xem sản phẩm đã có trong Flash Sale này chưa để tránh trùng lặp
        boolean exists = flashSale.getProducts().stream()
                .anyMatch(fsp -> fsp.getProduct().getId().equals(productId));

        if (!exists) {
            FlashSaleProduct fsp = new FlashSaleProduct();
            fsp.setFlashSale(flashSale);
            fsp.setProduct(product);

            // Tự động tính giá Sale = Giá gốc - (Giá gốc * % giảm / 100)
            BigDecimal discount = new BigDecimal(flashSale.getDiscountPercent()).divide(new BigDecimal(100));
            BigDecimal discountAmount = product.getPrice().multiply(discount);
            BigDecimal salePrice = product.getPrice().subtract(discountAmount);

            fsp.setSalePrice(salePrice);
            flashSaleProductRepository.save(fsp);
        }

        return "redirect:/admin/flash-sales/edit/" + flashSaleId;
    }

    // XÓA SẢN PHẨM KHỎI FLASH SALE
    @GetMapping("/admin/flash-sales/{flashSaleId}/remove-product/{fspId}")
    public String removeProduct(@PathVariable Integer flashSaleId, @PathVariable Integer fspId) {
        flashSaleProductRepository.deleteById(fspId);
        return "redirect:/admin/flash-sales/edit/" + flashSaleId;
    }

    // XÓA ĐỢT FLASH SALE
    @GetMapping("/admin/flash-sales/delete/{id}")
    public String delete(@PathVariable Integer id) {
        flashSaleRepository.deleteById(id);
        return "redirect:/admin/flash-sales";
    }
}