package com.ra.batshop.controller;

import com.ra.batshop.model.*;
import com.ra.batshop.model.Enum.OrderStatus;
import com.ra.batshop.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private CartItemRepository cartItemRepository;
    //private UserAddressRepository userAddressRepository;
    private AddressRepository addressRepository;
    private VoucherRepository voucherRepository;
    private  FlashSaleProductRepository flashSaleProductRepository;
    public CheckoutController(CartItemRepository cartItemRepository,
                              AddressRepository addressRepository,
                              VoucherRepository voucherRepository,
                              FlashSaleProductRepository flashSaleProductRepository) {
        this.cartItemRepository = cartItemRepository;
        this.flashSaleProductRepository = flashSaleProductRepository;
        this.addressRepository = addressRepository;
        this.voucherRepository = voucherRepository;
    }
    @GetMapping("/list")
    public String checkout(HttpSession httpSession, Model model) {
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        if (cartItems.isEmpty()) {
            model.addAttribute("totalCart", 0);
            model.addAttribute("discount", 0);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("defaultAddress", null);
            model.addAttribute("addresses", List.of());
            return "user/checkout/list";
        }


        BigDecimal total = BigDecimal.ZERO;

        // Tính tổng từng sản phẩm, nếu có Flash Sale thì lấy giá giảm
        Map<Integer, BigDecimal> flashSalePrices =
                (Map<Integer, BigDecimal>) httpSession.getAttribute("flashSalePrices");

        for (CartItem item : cartItems) {

            BigDecimal price;

            //  nếu có giá flash sale đã lưu → dùng lại
            if (flashSalePrices != null && flashSalePrices.containsKey(item.getId())) {
                price = flashSalePrices.get(item.getId());
            } else {
                // fallback nếu không có (trường hợp cũ)
                price = item.getProductVariant().getProduct().getPrice()
                        .add(item.getProductVariant().getAdditionalPrice());
            }

            total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        // Phí vận chuyển
        total = total.add(BigDecimal.valueOf(30000));


        // Áp dụng voucher
        Voucher voucher = (Voucher) httpSession.getAttribute("voucher");
        BigDecimal discount = BigDecimal.ZERO;
        if (voucher != null && total.compareTo(BigDecimal.valueOf(voucher.getMinOrderAmount())) >= 0) {
            discount = total.multiply(BigDecimal.valueOf(voucher.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100));

            if (discount.compareTo(BigDecimal.valueOf(voucher.getMaxDiscountAmount())) > 0) {
                discount = BigDecimal.valueOf(voucher.getMaxDiscountAmount());
            }
            total = total.subtract(discount);
        }

        // Lấy địa chỉ mặc định và tất cả địa chỉ
        Address defaultAddress = addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElse(null);
        List<Address> addresses = addressRepository.findByUserId(user.getId());

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalCart", total);
        model.addAttribute("discount", discount);
        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("addresses", addresses);

        return "user/checkout/list";
    }
    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam String code,
                               HttpSession session,
                               RedirectAttributes ra) {

        Optional<Voucher> voucherOpt = voucherRepository.findByCode(code);

        if(voucherOpt.isEmpty()){
            ra.addFlashAttribute("error","Voucher không tồn tại");
            return "redirect:/cart/checkout";
        }

        Voucher voucher = voucherOpt.get();

        if(!voucher.getActive()){
            ra.addFlashAttribute("error","Voucher không khả dụng");
            return "redirect:/cart/checkout";
        }

        if(voucher.getValidTo().isBefore(LocalDateTime.now())){
            ra.addFlashAttribute("error","Voucher đã hết hạn");
            return "redirect:/cart/checkout";
        }

        session.setAttribute("voucher", voucher);

        ra.addFlashAttribute("success","Áp dụng voucher thành công");

        return "redirect:/checkout/list";
    }
}
