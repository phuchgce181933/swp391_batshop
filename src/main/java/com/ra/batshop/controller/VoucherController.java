//package com.ra.batshop.controller;
//
//import com.ra.batshop.model.Voucher;
//import com.ra.batshop.repository.VoucherRepository;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//
//@Controller
//@RequestMapping("/admin/vouchers")
//public class VoucherController {
//
//    private final VoucherRepository voucherRepository;
//
//    public VoucherController(VoucherRepository voucherRepository) {
//        this.voucherRepository = voucherRepository;
//    }
//
//    // LIST
//    @GetMapping
//    public String list(Model model) {
//        model.addAttribute("vouchers", voucherRepository.findAll());
//        model.addAttribute("content", "admin/voucher/list");
//        return "admin/layout";
//    }
//
//    // ADD FORM
//    @GetMapping("/add")
//    public String addForm(Model model) {
//        model.addAttribute("voucher", new Voucher());
//        model.addAttribute("content", "admin/voucher/form");
//        return "admin/layout";
//    }
//
//    // EDIT FORM
//    @GetMapping("/edit/{id}")
//    public String editForm(@PathVariable Integer id, Model model) {
//        model.addAttribute("voucher", voucherRepository.findById(id).orElseThrow());
//        model.addAttribute("content", "admin/voucher/form");
//        return "admin/layout";
//    }
//
//    // SAVE (ADD + EDIT)
//    @PostMapping("/save")
//    public String save(@ModelAttribute Voucher voucher) {
//
//        if (voucher.getId() != null) {
//            Voucher old = voucherRepository.findById(voucher.getId()).orElseThrow();
//            voucher.setActive(old.getActive()); // giữ trạng thái cũ
//        } else {
//            voucher.setActive(true);
//        }
//
//        voucherRepository.save(voucher);
//        return "redirect:/admin/vouchers";
//    }
//
//    // TOGGLE STATUS
//    @GetMapping("/toggle/{id}")
//    public String toggle(@PathVariable Integer id) {
//        Voucher v = voucherRepository.findById(id).orElseThrow();
//        v.setActive(!Boolean.TRUE.equals(v.getActive()));
//        voucherRepository.save(v);
//        return "redirect:/admin/vouchers";
//    }
//}

package com.ra.batshop.controller;

import com.ra.batshop.model.Voucher;
import com.ra.batshop.repository.VoucherRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/vouchers")
public class VoucherController {

    private final VoucherRepository voucherRepository;

    public VoucherController(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    // LIST
    @GetMapping
    public String listVouchers(
            @RequestParam(required = false) String keyword,
            Model model) {

        List<Voucher> vouchers;

        if (keyword != null && !keyword.isEmpty()) {
            vouchers = voucherRepository.findByCodeContainingIgnoreCase(keyword);
        } else {
            vouchers = voucherRepository.findAll();
        }

        model.addAttribute("vouchers", vouchers);
        model.addAttribute("keyword", keyword);
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
        if (voucher.getValidFrom() != null &&
                voucher.getValidTo() != null &&
                voucher.getValidTo().isBefore(voucher.getValidFrom())) {

            model.addAttribute("error", "Valid To must be after Valid From");
            model.addAttribute("voucher", voucher);
            model.addAttribute("content", "admin/voucher/form");
            return "admin/layout";
        }

        // ===== 2. CHECK DUPLICATE CODE =====
        Optional<Voucher> existing = voucherRepository.findByCode(voucher.getCode());
        if (existing.isPresent() &&
                (voucher.getId() == null || !existing.get().getId().equals(voucher.getId()))) {

            model.addAttribute("error", "Voucher code already exists");
            model.addAttribute("voucher", voucher);
            model.addAttribute("content", "admin/voucher/form");
            return "admin/layout";
        }

        // ===== 3. SET STATUS =====
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
