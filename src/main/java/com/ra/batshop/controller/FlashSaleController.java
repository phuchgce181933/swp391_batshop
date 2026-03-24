package com.ra.batshop.controller;

import com.ra.batshop.model.FlashSale;
import com.ra.batshop.model.FlashSaleProduct;
import com.ra.batshop.model.Product;
import com.ra.batshop.repository.CategoryRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class FlashSaleController {

    private final FlashSaleRepository flashSaleRepository;
    private final FlashSaleProductRepository flashSaleProductRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository; // Đã thêm CategoryRepository

    public FlashSaleController(FlashSaleRepository flashSaleRepository,
                               FlashSaleProductRepository flashSaleProductRepository,
                               ProductRepository productRepository,
                               CategoryRepository categoryRepository) { // Thêm vào tham số Constructor
        this.flashSaleRepository = flashSaleRepository;
        this.flashSaleProductRepository = flashSaleProductRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository; // Gán giá trị
    }

    // =======================================================
    // 0. API LẤY DANH SÁCH KHUNG GIỜ ĐÃ CÓ ĐỂ VÔ HIỆU HÓA BÊN GIAO DIỆN
    // =======================================================
    @GetMapping("/admin/flash-sales/api/booked-slots")
    @ResponseBody
    public List<Integer> getBookedSlots(@RequestParam("date") LocalDate date,
                                        @RequestParam(value = "excludeId", required = false) Integer excludeId) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime nextDay = date.plusDays(1).atStartOfDay();

        List<FlashSale> sales = flashSaleRepository.findFlashSalesByDate(startOfDay, nextDay, excludeId);

        // Trả về danh sách các "giờ bắt đầu" đã được đặt (VD: [0, 8, 14])
        return sales.stream()
                .map(f -> f.getStartDate().getHour())
                .collect(Collectors.toList());
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

        // =================================================================
        // THÊM: Truyền danh sách Category xuống View để làm bộ lọc Danh Mục
        // =================================================================
        model.addAttribute("categories", categoryRepository.findAll());

        return "user/flash-sale/flash-sale";
    }

    // =======================================================
    // 2. KHU VỰC DÀNH CHO ADMIN (QUẢN LÝ FLASH SALE)
    // =======================================================

    // DANH SÁCH FLASH SALE
    @GetMapping("/admin/flash-sales")
    public String list(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        model.addAttribute("flashSales", flashSaleRepository.findAll(Sort.by(Sort.Direction.DESC, "startDate")));
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

    // LƯU FLASH SALE MỚI (Sử dụng timeSlot)
    @PostMapping("/admin/flash-sales/add")
    public String save(@ModelAttribute FlashSale flashSale,
                       @RequestParam("saleDate") LocalDate saleDate,
                       @RequestParam("timeSlot") Integer timeSlot,
                       RedirectAttributes redirectAttributes) {

        // 1. Tính toán Giờ bắt đầu và Giờ kết thúc từ Khung giờ (timeSlot)
        LocalDateTime start = saleDate.atTime(timeSlot, 0);
        // Nếu khung giờ là 22h, giờ kết thúc là 0h của ngày hôm sau
        LocalDateTime end = (timeSlot == 22) ? saleDate.plusDays(1).atStartOfDay() : saleDate.atTime(timeSlot + 2, 0);

        // 2. Validate: Chỉ báo lỗi khi KHUNG GIỜ ĐÃ KẾT THÚC hoàn toàn
        if (!end.isAfter(LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Khung giờ đã chọn đã trôi qua hoàn toàn!");
            return "redirect:/admin/flash-sales/add";
        }

        // 3. Validate chống trùng lịch Flash Sale
        if (flashSaleRepository.isFlashSaleTimeOverlapping(start, end, null)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Khung giờ này đã bị trùng lịch với một đợt Flash Sale khác!");
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

    // CẬP NHẬT THÔNG TIN CƠ BẢN CỦA ĐỢT SALE (Chỉ cập nhật thời gian)
    @PostMapping("/admin/flash-sales/edit")
    public String update(@ModelAttribute FlashSale flashSale,
                         @RequestParam("saleDate") LocalDate saleDate,
                         @RequestParam("timeSlot") Integer timeSlot,
                         RedirectAttributes redirectAttributes) {

        FlashSale existing = flashSaleRepository.findById(flashSale.getId()).orElseThrow();

        // 1. Tính toán Giờ bắt đầu và Giờ kết thúc từ Khung giờ (timeSlot)
        LocalDateTime start = saleDate.atTime(timeSlot, 0);
        LocalDateTime end = (timeSlot == 22) ? saleDate.plusDays(1).atStartOfDay() : saleDate.atTime(timeSlot + 2, 0);

        // 2. Validate chống trùng lịch (Bỏ qua ID của chính nó đang sửa)
        if (flashSaleRepository.isFlashSaleTimeOverlapping(start, end, flashSale.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Khoảng thời gian này đã bị trùng lịch với một đợt Flash Sale khác!");
            return "redirect:/admin/flash-sales/edit/" + flashSale.getId();
        }

        existing.setStartDate(start);
        existing.setEndDate(end);
        flashSaleRepository.save(existing);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật khung giờ thành công!");
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

            // 3. Nếu an toàn, tiến hành thêm vào db với mức giảm mặc định là 1%
            FlashSaleProduct fsp = new FlashSaleProduct();
            fsp.setFlashSale(flashSale);
            fsp.setProduct(product);

            fsp.setDiscountPercent(1); // Mặc định giảm 1% khi mới thêm vào

            // Tự động tính luôn giá Sale cho mức 1% (Giá Gốc * 99 / 100)
            BigDecimal salePrice = product.getPrice()
                    .multiply(BigDecimal.valueOf(99))
                    .divide(BigDecimal.valueOf(100));

            fsp.setSalePrice(salePrice);
            flashSaleProductRepository.save(fsp);
            successCount++;
        }

        // Thông báo kết quả
        if (failCount > 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã thêm " + successCount + " sản phẩm. Bỏ qua " + failCount + " sản phẩm do bị trùng thời gian với đợt Sale khác!");
        } else {
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm thành công " + successCount + " sản phẩm vào Flash Sale! Vui lòng cập nhật % giảm giá cho từng sản phẩm.");
        }

        return "redirect:/admin/flash-sales/edit/" + flashSaleId;
    }

    // CẬP NHẬT % GIẢM GIÁ CHO TỪNG SẢN PHẨM RIÊNG LẺ
    @PostMapping("/admin/flash-sales/{fsId}/update-discount/{fspId}")
    public String updateProductDiscount(@PathVariable Integer fsId,
                                        @PathVariable Integer fspId,
                                        @RequestParam("discountPercent") Integer discountPercent,
                                        RedirectAttributes redirectAttributes) {

        FlashSaleProduct fsp = flashSaleProductRepository.findById(fspId).orElseThrow();

        // Cập nhật %
        fsp.setDiscountPercent(discountPercent);

        // Công thức tính giá mới: Giá Sale = Giá Gốc * (100 - % Giảm) / 100
        BigDecimal salePrice = fsp.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(100 - discountPercent))
                .divide(BigDecimal.valueOf(100));

        fsp.setSalePrice(salePrice);
        flashSaleProductRepository.save(fsp);

        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật mức giảm " + discountPercent + "% cho sản phẩm: " + fsp.getProduct().getName());
        return "redirect:/admin/flash-sales/edit/" + fsId;
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
    // =======================================================
    // LƯU TẤT CẢ % GIẢM GIÁ CÙNG LÚC
    // =======================================================
    @PostMapping("/admin/flash-sales/{id}/update-all-discounts")
    public String updateAllDiscounts(@PathVariable("id") Integer flashSaleId,
                                     @RequestParam(value = "fspIds", required = false) List<Integer> fspIds,
                                     @RequestParam(value = "discountPercents", required = false) List<Integer> discountPercents,
                                     RedirectAttributes redirectAttributes) {

        // Kiểm tra dữ liệu đầu vào
        if (fspIds == null || discountPercents == null || fspIds.isEmpty() || fspIds.size() != discountPercents.size()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: Không có dữ liệu hợp lệ để cập nhật.");
            return "redirect:/admin/flash-sales/edit/" + flashSaleId;
        }

        // Lặp qua từng sản phẩm để cập nhật
        int count = 0;
        for (int i = 0; i < fspIds.size(); i++) {
            Integer fspId = fspIds.get(i);
            Integer discountPercent = discountPercents.get(i);

            FlashSaleProduct fsp = flashSaleProductRepository.findById(fspId).orElse(null);
            if (fsp != null) {
                fsp.setDiscountPercent(discountPercent);

                // Tính toán lại giá Flash Sale
                BigDecimal salePrice = fsp.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(100 - discountPercent))
                        .divide(BigDecimal.valueOf(100));

                fsp.setSalePrice(salePrice);
                flashSaleProductRepository.save(fsp);
                count++;
            }
        }

        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật thành công mức giảm giá cho " + count + " sản phẩm!");
        return "redirect:/admin/flash-sales/edit/" + flashSaleId;
    }
}