package com.ra.batshop.controller;

import com.ra.batshop.model.Voucher;
import com.ra.batshop.repository.VoucherRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/vouchers")
public class VoucherController {

    private final VoucherRepository voucherRepository;

    public VoucherController(VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    // LIST
    @GetMapping
    public String list(Model model) {
        model.addAttribute("vouchers", voucherRepository.findAll());
        return "admin/voucher/list";
    }

    // ADD FORM
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        return "admin/voucher/form";
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("voucher",
                voucherRepository.findById(id).orElseThrow());
        return "admin/voucher/form";
    }

    // SAVE (ADD + EDIT)
    @PostMapping("/save")
    public String save(@ModelAttribute Voucher voucher) {

        if (voucher.getId() == null) {
            voucher.setActive(true);
        }

        voucherRepository.save(voucher);
        return "redirect:/admin/vouchers";
    }

    // TOGGLE STATUS
    @GetMapping("/toggle/{id}")
    public String toggle(@PathVariable Integer id) {
        Voucher v = voucherRepository.findById(id).orElseThrow();
        v.setActive(!Boolean.TRUE.equals(v.getActive()));
        voucherRepository.save(v);
        return "redirect:/admin/vouchers";
    }
}
