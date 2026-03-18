package com.ra.batshop.controller;

import com.ra.batshop.model.FlashSale;
import com.ra.batshop.model.FlashSaleProduct;
import com.ra.batshop.model.Product;
import com.ra.batshop.repository.FlashSaleProductRepository;
import com.ra.batshop.repository.FlashSaleRepository;
import com.ra.batshop.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Controller
public class FlashSaleController {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleProductRepository flashSaleProductRepository;
    private final ProductRepository productRepository;

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
        LocalDateTime now = LocalDateTime.now();

        // 1. Tìm đợt Sale đang diễn ra
        Optional<FlashSale> activeSale = flashSaleRepository.findActiveFlashSales(now).stream().findFirst();

        if (activeSale.isPresent()) {
            model.addAttribute("activeFlashSale", activeSale.get());
        } else {
            // 2. Nếu không có đợt nào đang chạy, tìm đợt sắp diễn ra
            Optional<FlashSale> upcomingSale = flashSaleRepository.findFirstByStartDateAfterOrderByStartDateAsc(now);
            if (upcomingSale.isPresent()) {
                model.addAttribute("upcomingFlashSale", upcomingSale.get());
            }
        }

        return "user/flash-sale/flash-sale";
    }

    // =======================================================
    // 2. KHU VỰC DÀNH CHO ADMIN (QUẢN LÝ FLASH SALE)
    // =======================================================

    // DANH SÁCH FLASH SALE
    @GetMapping("/admin/flash-sales")
    public String list(Model model) {
        model.addAttribute("flashSales", flashSaleRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));        model.addAttribute("content", "admin/flash-sale/list");
        return "admin/layout";
    }

    // FORM THÊM MỚI
    @GetMapping("/admin/flash-sales/add")
    public String addForm(Model model) {
        model.addAttribute("flashSale", new FlashSale());
        model.addAttribute("content", "admin/flash-sale/add");
        return "admin/layout";
    }

    // LƯU FLASH SALE MỚI (Tách Ngày và Giờ)
    @PostMapping("/admin/flash-sales/add")
    public String save(@ModelAttribute FlashSale flashSale,
                       @RequestParam("saleDate") LocalDate saleDate,
                       @RequestParam("startTime") LocalTime startTime,
                       @RequestParam("endTime") LocalTime endTime,
                       RedirectAttributes redirectAttributes) {

        // 1. Gộp Ngày và Giờ lại thành LocalDateTime
        LocalDateTime start = LocalDateTime.of(saleDate, startTime);
        LocalDateTime end = LocalDateTime.of(saleDate, endTime);

        // 2. Validate: Giờ kết thúc phải sau Giờ bắt đầu
        if (!start.isBefore(end)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Giờ kết thúc phải sau giờ bắt đầu!");
            return "redirect:/admin/flash-sales/add";
        }

        // ==================================================
        // 3. SỬA ĐOẠN NÀY: Validate chống trùng lịch Flash Sale
        // ==================================================
        if (flashSaleRepository.isFlashSaleTimeOverlapping(start, end, null)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Khoảng thời gian này đã bị trùng lịch với một đợt Flash Sale khác!");
            return "redirect:/admin/flash-sales/add";
        }

        flashSale.setStartDate(start);
        flashSale.setEndDate(end);
        flashSaleRepository.save(flashSale);

        return "redirect:/admin/flash-sales";
    }

    // CHI TIẾT & FORM SỬA, THÊM SẢN PHẨM VÀO SALE
    @GetMapping("/admin/flash-sales/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        FlashSale flashSale = flashSaleRepository.findById(id).orElseThrow();

        // Lấy danh sách ID của các sản phẩm ĐÃ CÓ trong đợt Flash Sale này
        List<Integer> addedProductIds = flashSale.getProducts().stream()
                .map(fsp -> fsp.getProduct().getId())
                .toList();

        model.addAttribute("flashSale", flashSale);
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("addedProductIds", addedProductIds); // Truyền list ID sang View
        model.addAttribute("content", "admin/flash-sale/edit");

        return "admin/layout";
    }

    // CẬP NHẬT THÔNG TIN CƠ BẢN CỦA ĐỢT SALE
    @PostMapping("/admin/flash-sales/edit")
    public String update(@ModelAttribute FlashSale flashSale,
                         @RequestParam("saleDate") LocalDate saleDate,
                         @RequestParam("startTime") LocalTime startTime,
                         @RequestParam("endTime") LocalTime endTime,
                         RedirectAttributes redirectAttributes) {

        FlashSale existing = flashSaleRepository.findById(flashSale.getId()).orElseThrow();

        // 1. Gộp Ngày và Giờ
        LocalDateTime start = LocalDateTime.of(saleDate, startTime);
        LocalDateTime end = LocalDateTime.of(saleDate, endTime);

        // 2. Validate thời gian
        if (!start.isBefore(end)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Giờ kết thúc phải diễn ra sau giờ bắt đầu!");
            return "redirect:/admin/flash-sales/edit/" + flashSale.getId();
        }

        // ==================================================
        // 3. SỬA ĐOẠN NÀY: Validate chống trùng lịch (Bỏ qua ID của chính nó đang sửa)
        // ==================================================
        if (flashSaleRepository.isFlashSaleTimeOverlapping(start, end, flashSale.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Khoảng thời gian này đã bị trùng lịch với một đợt Flash Sale khác!");
            return "redirect:/admin/flash-sales/edit/" + flashSale.getId();
        }

        // 4. Kiểm tra sự thay đổi của % giảm giá
        boolean isDiscountChanged = !existing.getDiscountPercent().equals(flashSale.getDiscountPercent());

        existing.setDiscountPercent(flashSale.getDiscountPercent());
        existing.setStartDate(start);
        existing.setEndDate(end);
        flashSaleRepository.save(existing);

        // 5. Logic tính lại giá cho toàn bộ sản phẩm nếu Admin đổi % giảm
        if (isDiscountChanged && existing.getProducts() != null) {
            for (FlashSaleProduct fsp : existing.getProducts()) {
                Product p = fsp.getProduct();

                BigDecimal discount = new BigDecimal(existing.getDiscountPercent()).divide(new BigDecimal(100));
                BigDecimal discountAmount = p.getPrice().multiply(discount);
                BigDecimal newSalePrice = p.getPrice().subtract(discountAmount);

                fsp.setSalePrice(newSalePrice);
                flashSaleProductRepository.save(fsp);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật thông số và tính toán lại giá toàn bộ sản phẩm!");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông số thành công!");
        }

        return "redirect:/admin/flash-sales/edit/" + flashSale.getId();
    }

    // THÊM NHIỀU SẢN PHẨM VÀO FLASH SALE CÙNG LÚC
    @PostMapping("/admin/flash-sales/{id}/add-product")
    public String addProductsToFlashSale(@PathVariable("id") Integer flashSaleId,
                                         @RequestParam(value = "productIds", required = false) List<Integer> productIds,
                                         RedirectAttributes redirectAttributes) {

        if (productIds == null || productIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng tick chọn ít nhất một sản phẩm!");
            return "redirect:/admin/flash-sales/edit/" + flashSaleId;
        }

        FlashSale flashSale = flashSaleRepository.findById(flashSaleId).orElseThrow();

        int successCount = 0;
        int failCount = 0;

        for (Integer productId : productIds) {
            Product product = productRepository.findById(productId).orElseThrow();

            // 1. Bỏ qua nếu sản phẩm ĐÃ CÓ trong Flash Sale NÀY
            boolean existsInCurrent = flashSale.getProducts().stream()
                    .anyMatch(fsp -> fsp.getProduct().getId().equals(productId));
            if (existsInCurrent) continue;

            // 2. LOGIC KHẮT KHE: Kiểm tra trùng thời gian với đợt sale KHÁC
            boolean isOverlapping = flashSaleRepository.isProductInOverlappingSale(
                    productId, flashSaleId, flashSale.getStartDate(), flashSale.getEndDate()
            );

            if (isOverlapping) {
                failCount++;
                continue; // Bỏ qua sản phẩm bị trùng, đi tới sản phẩm tiếp theo
            }

            // 3. Nếu an toàn, tiến hành thêm vào db
            FlashSaleProduct fsp = new FlashSaleProduct();
            fsp.setFlashSale(flashSale);
            fsp.setProduct(product);

            BigDecimal discount = new BigDecimal(flashSale.getDiscountPercent()).divide(new BigDecimal(100));
            BigDecimal discountAmount = product.getPrice().multiply(discount);
            BigDecimal salePrice = product.getPrice().subtract(discountAmount);

            fsp.setSalePrice(salePrice);
            flashSaleProductRepository.save(fsp);
            successCount++;
        }

        // Thông báo kết quả
        if (failCount > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã thêm " + successCount + " sản phẩm. Bỏ qua " + failCount + " sản phẩm do bị trùng thời gian với đợt Sale khác!");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm thành công " + successCount + " sản phẩm vào Flash Sale!");
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