package com.ra.batshop.controller;

import com.ra.batshop.model.Voucher;
import com.ra.batshop.repository.OrderRepository;
import com.ra.batshop.repository.VoucherRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/vouchers")
public class VoucherController {

    @ModelAttribute
    public void addActiveMenu(Model model) {
        model.addAttribute("activeMenu", "vouchers");
    }

    private final VoucherRepository voucherRepository;
    private final OrderRepository orderRepository;


    public VoucherController(VoucherRepository voucherRepository,
                                OrderRepository orderRepository) {
        this.voucherRepository = voucherRepository;
        this.orderRepository = orderRepository;
    }

    // LIST
    @GetMapping
    public String listVouchers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Integer discount,
            Model model, HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        List<Voucher> vouchers;

        // FILTER CODE
        if (keyword != null && !keyword.isEmpty()) {
            vouchers = voucherRepository.findByCodeContainingIgnoreCase(keyword);
        } else {
            vouchers = voucherRepository.findAll();
        }

        // FILTER STATUS
        if (status != null) {
            vouchers = vouchers.stream()
                    .filter(v -> Boolean.TRUE.equals(v.getActive()) == status)
                    .toList();
        }

        // FILTER DISCOUNT
        if (discount != null) {
            vouchers = vouchers.stream()
                    .filter(v -> v.getDiscountPercent() >= discount)
                    .toList();
        }

        //  FIX: SET totalUsed chuẩn từ DB
        vouchers.forEach(v -> {
            int used = orderRepository.countVoucherUsed(v.getId());
            v.setTotalUsed(used);
        });

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("discount", discount);
        model.addAttribute("content", "admin/voucher/list");

        return "admin/layout";
    }

    // ADD FORM
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        model.addAttribute("content", "admin/voucher/form");
        return "admin/layout";
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("voucher", voucherRepository.findById(id).orElseThrow());
        model.addAttribute("content", "admin/voucher/form");
        return "admin/layout";
    }

    // SAVE
    @PostMapping("/save")
    public String save(@ModelAttribute Voucher voucher, Model model) {

        // ===== 1. CHECK DATE =====
        if (voucher.getValidFrom() != null && voucher.getValidTo() != null &&
                voucher.getValidTo().isBefore(voucher.getValidFrom())) {
            model.addAttribute("error", "Valid To must be after Valid From");
            model.addAttribute("voucher", voucher);
            model.addAttribute("content", "admin/voucher/form");
            return "admin/layout";
        }

        // ===== 2. CHECK DISCOUNT PERCENT =====
        if (voucher.getDiscountPercent() == null || voucher.getDiscountPercent() <= 0 || voucher.getDiscountPercent() > 100) {
            model.addAttribute("error", "Discount percent must be between 1 and 100");
            model.addAttribute("voucher", voucher);
            model.addAttribute("content", "admin/voucher/form");
            return "admin/layout";
        }

        // ===== 3. CHECK MAX/MIN MONEY =====
        if (voucher.getMaxDiscountAmount() != null && voucher.getMaxDiscountAmount() < 0) {
            model.addAttribute("error", "Max discount must be >= 0");
            model.addAttribute("voucher", voucher);
            model.addAttribute("content", "admin/voucher/form");
            return "admin/layout";
        }
        if (voucher.getMinOrderAmount() != null && voucher.getMinOrderAmount() < 0) {
            model.addAttribute("error", "Min order amount must be >= 0");
            model.addAttribute("voucher", voucher);
            model.addAttribute("content", "admin/voucher/form");
            return "admin/layout";
        }

        // NEW LOGIC: max discount cannot exceed min order
        if (voucher.getMaxDiscountAmount() != null && voucher.getMinOrderAmount() != null
                && voucher.getMaxDiscountAmount() > voucher.getMinOrderAmount()) {
            model.addAttribute("error", "Max discount cannot exceed Min order amount");
            model.addAttribute("voucher", voucher);
            model.addAttribute("content", "admin/voucher/form");
            return "admin/layout";
        }

        // ===== 4. CHECK DUPLICATE CODE =====
        Optional<Voucher> existing = voucherRepository.findByCode(voucher.getCode());
        if (existing.isPresent() &&
                (voucher.getId() == null || !existing.get().getId().equals(voucher.getId()))) {
            model.addAttribute("error", "Voucher code already exists");
            model.addAttribute("voucher", voucher);
            model.addAttribute("content", "admin/voucher/form");
            return "admin/layout";
        }

        // ===== 5. SET ACTIVE =====
        if (voucher.getId() != null) {
            Voucher old = voucherRepository.findById(voucher.getId()).orElseThrow();
            voucher.setActive(old.getActive());
        } else {
            voucher.setActive(true);
        }

        voucherRepository.save(voucher);
        return "redirect:/admin/vouchers";
    }

    // TOGGLE STATUS
    @GetMapping("/toggle/{id}")
    public String toggle(@PathVariable Integer id) {

        Voucher v = voucherRepository.findById(id).orElseThrow();

        // Không cho activate nếu hết hạn
        if (!Boolean.TRUE.equals(v.getActive())) {
            if (v.getValidTo() != null &&
                    v.getValidTo().isBefore(LocalDateTime.now())) {
                return "redirect:/admin/vouchers";
            }
        }

        v.setActive(!Boolean.TRUE.equals(v.getActive()));
        voucherRepository.save(v);

        return "redirect:/admin/vouchers";
    }
}
