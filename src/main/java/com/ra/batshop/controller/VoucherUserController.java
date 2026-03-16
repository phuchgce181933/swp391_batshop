package com.ra.batshop.controller;

import com.ra.batshop.model.Voucher;

import com.ra.batshop.repository.VoucherRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/vouchers")
public class VoucherUserController {
    private final VoucherRepository voucherRepository;

    public VoucherUserController(VoucherRepository voucherRepository) {

        this.voucherRepository = voucherRepository;
    }
    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam String code,
                               HttpSession session,
                               RedirectAttributes ra) {

        Optional<Voucher> voucherOpt = voucherRepository.findByCode(code);

        if(voucherOpt.isEmpty()){
            ra.addFlashAttribute("error","Voucher không tồn tại");
            return "redirect:/checkout";
        }

        Voucher voucher = voucherOpt.get();

        if(!voucher.getActive()){
            ra.addFlashAttribute("error","Voucher không khả dụng");
            return "redirect:/checkout";
        }

        if(voucher.getValidTo().isBefore(LocalDateTime.now())){
            ra.addFlashAttribute("error","Voucher đã hết hạn");
            return "redirect:/checkout";
        }

        session.setAttribute("voucher", voucher);

        ra.addFlashAttribute("success","Áp dụng voucher thành công");

        return "redirect:/checkout";
    }
}
