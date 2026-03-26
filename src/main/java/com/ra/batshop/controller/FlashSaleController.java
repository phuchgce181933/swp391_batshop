package com.ra.batshop.controller;

import com.ra.batshop.model.FlashSale;
import com.ra.batshop.model.FlashSaleProduct;
import com.ra.batshop.model.Product;
import com.ra.batshop.repository.FlashSaleProductRepository;
import com.ra.batshop.repository.FlashSaleRepository;
import com.ra.batshop.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class FlashSaleController {

    @ModelAttribute
    public void addActiveMenu(Model model) {
        model.addAttribute("activeMenu", "flash-sales");
    }

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
    // HÀM HỖ TRỢ: Lấy danh sách khung giờ đã được đặt
    // =======================================================
    private List<String> getBookedSlots(Integer excludeId) {
        return flashSaleRepository.findAll().stream()
                .filter(fs -> excludeId == null || !fs.getId().equals(excludeId))
                .map(fs -> {
                    String date = fs.getStartDate().toLocalDate().toString();
                    String startHour = String.format("%02d:00", fs.getStartDate().getHour());
                    String endHour = fs.getEndDate().getHour() == 23 ? "24:00" : String.format("%02d:00", fs.getEndDate().getHour());
                    return date + "|" + startHour + "-" + endHour; // Format: 2026-03-25|10:00-12:00
                })
                .collect(Collectors.toList());
    }

    // =======================================================
    // 1. KHU VỰC DÀNH CHO NGƯỜI DÙNG (END-USER)
    // =======================================================
    @GetMapping("/flash-sale")
    public String flashSalePage(Model model) {
        LocalDateTime now = LocalDateTime.now();
        Optional<FlashSale> activeSale = flashSaleRepository.findActiveFlashSales(now).stream().findFirst();

        if (activeSale.isPresent()) {
            model.addAttribute("activeFlashSale", activeSale.get());
        } else {
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

    @GetMapping("/admin/flash-sales")
    public String list(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) return "redirect:/login";
        model.addAttribute("flashSales", flashSaleRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
        model.addAttribute("content", "admin/flash-sale/list");
        return "admin/layout";
    }

    @GetMapping("/admin/flash-sales/add")
    public String addForm(Model model) {
        model.addAttribute("flashSale", new FlashSale());
        model.addAttribute("bookedSlots", getBookedSlots(null)); // Gửi danh sách giờ đã chiếm sang JS
        model.addAttribute("content", "admin/flash-sale/add");
        return "admin/layout";
    }

    @PostMapping("/admin/flash-sales/add")
    public String save(@ModelAttribute FlashSale flashSale,
                       @RequestParam("saleDate") LocalDate saleDate,
                       @RequestParam("timeSlot") String timeSlot,
                       RedirectAttributes redirectAttributes) {

        String[] times = timeSlot.split("-");
        LocalTime startTime = LocalTime.parse(times[0]);
        LocalTime endTime = "24:00".equals(times[1]) ? LocalTime.of(23, 59, 59) : LocalTime.parse(times[1]);

        LocalDateTime start = LocalDateTime.of(saleDate, startTime);
        LocalDateTime end = LocalDateTime.of(saleDate, endTime);

        if (flashSaleRepository.isFlashSaleTimeOverlapping(start, end, null)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khoảng thời gian này đã bị trùng lịch!");
            return "redirect:/admin/flash-sales/add";
        }

        flashSale.setStartDate(start);
        flashSale.setEndDate(end);
        flashSaleRepository.save(flashSale);
        return "redirect:/admin/flash-sales";
    }

    @GetMapping("/admin/flash-sales/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        FlashSale flashSale = flashSaleRepository.findById(id).orElseThrow();
        List<Integer> addedProductIds = flashSale.getProducts().stream()
                .map(fsp -> fsp.getProduct().getId())
                .toList();

        // Lấy khung giờ hiện tại của đợt Sale
        String currentSlot = String.format("%02d:00-%s",
                flashSale.getStartDate().getHour(),
                flashSale.getEndDate().getHour() == 23 ? "24:00" : String.format("%02d:00", flashSale.getEndDate().getHour())
        );

        model.addAttribute("flashSale", flashSale);
        model.addAttribute("currentTimeSlot", currentSlot);
        model.addAttribute("bookedSlots", getBookedSlots(id)); // Bỏ qua id hiện tại khi check trùng
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("addedProductIds", addedProductIds);
        model.addAttribute("content", "admin/flash-sale/edit");

        return "admin/layout";
    }

    @PostMapping("/admin/flash-sales/edit")
    public String update(@ModelAttribute FlashSale flashSale,
                         @RequestParam("saleDate") LocalDate saleDate,
                         @RequestParam("timeSlot") String timeSlot,
                         RedirectAttributes redirectAttributes) {

        FlashSale existing = flashSaleRepository.findById(flashSale.getId()).orElseThrow();
        String[] times = timeSlot.split("-");
        LocalTime startTime = LocalTime.parse(times[0]);
        LocalTime endTime = "24:00".equals(times[1]) ? LocalTime.of(23, 59, 59) : LocalTime.parse(times[1]);

        LocalDateTime start = LocalDateTime.of(saleDate, startTime);
        LocalDateTime end = LocalDateTime.of(saleDate, endTime);

        if (flashSaleRepository.isFlashSaleTimeOverlapping(start, end, flashSale.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khoảng thời gian này đã bị trùng lịch!");
            return "redirect:/admin/flash-sales/edit/" + flashSale.getId();
        }

        existing.setStartDate(start);
        existing.setEndDate(end);
        flashSaleRepository.save(existing);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thời gian thành công!");

        return "redirect:/admin/flash-sales/edit/" + flashSale.getId();
    }

    // =======================================================
    // LƯU % GIẢM GIÁ CHO 1 SẢN PHẨM
    // =======================================================
    @PostMapping("/admin/flash-sales/{fsId}/update-discount/{fspId}")
    public String updateSingleDiscount(@PathVariable Integer fsId,
                                       @PathVariable Integer fspId,
                                       @RequestParam("discounts") List<Integer> discounts,
                                       @RequestParam("fspIds") List<Integer> fspIds,
                                       RedirectAttributes redirectAttributes) {
        int index = fspIds.indexOf(fspId);
        if (index != -1) {
            Integer discount = discounts.get(index);
            FlashSaleProduct fsp = flashSaleProductRepository.findById(fspId).orElseThrow();
            fsp.setDiscountPercent(discount);
            BigDecimal discAmt = fsp.getProduct().getPrice().multiply(new BigDecimal(discount)).divide(new BigDecimal(100));
            fsp.setSalePrice(fsp.getProduct().getPrice().subtract(discAmt));
            flashSaleProductRepository.save(fsp);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật mức giảm cho " + fsp.getProduct().getName());
        }
        return "redirect:/admin/flash-sales/edit/" + fsId;
    }

    // =======================================================
    // LƯU TẤT CẢ % GIẢM GIÁ
    // =======================================================
    @PostMapping("/admin/flash-sales/{id}/update-all-discounts")
    public String updateAllDiscounts(@PathVariable Integer id,
                                     @RequestParam(value = "fspIds", required = false) List<Integer> fspIds,
                                     @RequestParam(value = "discounts", required = false) List<Integer> discounts,
                                     RedirectAttributes redirectAttributes) {
        if (fspIds != null && discounts != null) {
            for (int i = 0; i < fspIds.size(); i++) {
                FlashSaleProduct fsp = flashSaleProductRepository.findById(fspIds.get(i)).orElseThrow();
                Integer discount = discounts.get(i);
                fsp.setDiscountPercent(discount);
                BigDecimal discAmt = fsp.getProduct().getPrice().multiply(new BigDecimal(discount)).divide(new BigDecimal(100));
                fsp.setSalePrice(fsp.getProduct().getPrice().subtract(discAmt));
                flashSaleProductRepository.save(fsp);
            }
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu mức giảm cho toàn bộ sản phẩm!");
        }
        return "redirect:/admin/flash-sales/edit/" + id;
    }

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

            boolean existsInCurrent = flashSale.getProducts().stream()
                    .anyMatch(fsp -> fsp.getProduct().getId().equals(productId));
            if (existsInCurrent) continue;

            boolean isOverlapping = flashSaleRepository.isProductInOverlappingSale(
                    productId, flashSaleId, flashSale.getStartDate(), flashSale.getEndDate()
            );

            if (isOverlapping) {
                failCount++;
                continue;
            }

            FlashSaleProduct fsp = new FlashSaleProduct();
            fsp.setFlashSale(flashSale);
            fsp.setProduct(product);

            // Cài đặt mặc định 1% khi mới thêm
            fsp.setDiscountPercent(1);
            BigDecimal discountAmount = product.getPrice().multiply(new BigDecimal("0.01"));
            BigDecimal salePrice = product.getPrice().subtract(discountAmount);

            fsp.setSalePrice(salePrice);
            flashSaleProductRepository.save(fsp);
            successCount++;
        }

        if (failCount > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã thêm " + successCount + " SP. Bỏ qua " + failCount + " SP do đang ở đợt Sale khác!");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm thành công " + successCount + " sản phẩm!");
        }
        return "redirect:/admin/flash-sales/edit/" + flashSaleId;
    }

    @GetMapping("/admin/flash-sales/{flashSaleId}/remove-product/{fspId}")
    public String removeProduct(@PathVariable Integer flashSaleId, @PathVariable Integer fspId) {
        flashSaleProductRepository.deleteById(fspId);
        return "redirect:/admin/flash-sales/edit/" + flashSaleId;
    }

    @GetMapping("/admin/flash-sales/delete/{id}")
    public String delete(@PathVariable Integer id) {
        flashSaleRepository.deleteById(id);
        return "redirect:/admin/flash-sales";
    }
}