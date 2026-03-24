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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private CartItemRepository cartItemRepository;
    //private UserAddressRepository userAddressRepository;
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private AddressRepository addressRepository;
    private VoucherRepository voucherRepository;
    private FlashSaleProductRepository flashSaleProductRepository;
    public CheckoutController(CartItemRepository cartItemRepository,
                              // UserAddressRepository userAddressRepository,
                              OrderRepository orderRepository,
                              OrderItemRepository orderItemRepository,
                              AddressRepository addressRepository,
                              VoucherRepository voucherRepository,
                              FlashSaleProductRepository flashSaleProductRepository) {
        this.cartItemRepository = cartItemRepository;
        // this.userAddressRepository = userAddressRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
        this.voucherRepository = voucherRepository;
        this.flashSaleProductRepository = flashSaleProductRepository;
    }
    @GetMapping("/list")
    public String checkout(HttpSession httpSession, Model model) {
        User user = (User) httpSession.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal shippingFee = BigDecimal.valueOf(30000);

        System.out.println("===== DEBUG CHECKOUT =====");
        Map<Integer, BigDecimal> flashSalePrices = new HashMap<>();

        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();

            Optional<FlashSaleProduct> flashSaleOpt =
                    flashSaleProductRepository.findActiveByProductId(
                            variant.getProduct().getId(), LocalDateTime.now()
                    );

            BigDecimal price;
            if (flashSaleOpt.isPresent()) {
                price = flashSaleOpt.get().getSalePrice();
            } else {
                price = variant.getAdditionalPrice() != null ? variant.getAdditionalPrice() : BigDecimal.ZERO;
            }

            item.setDisplayPrice(price);

            flashSalePrices.put(variant.getId(), price); // lưu giá theo variantId
            total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        httpSession.setAttribute("flashSalePrices", flashSalePrices); // save vào session

        total = total.add(shippingFee);

        // Áp dụng voucher
        Voucher voucher = (Voucher) httpSession.getAttribute("voucher");
        BigDecimal discount = BigDecimal.ZERO;
        if (voucher != null && total.compareTo(BigDecimal.valueOf(voucher.getMinOrderAmount())) >= 0) {
            discount = total.multiply(BigDecimal.valueOf(voucher.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100));
            if (discount.compareTo(BigDecimal.valueOf(voucher.getMaxDiscountAmount())) > 0)
                discount = BigDecimal.valueOf(voucher.getMaxDiscountAmount());
            total = total.subtract(discount);
        }
        System.out.println("TOTAL CART: " + total);
        System.out.println("===== END DEBUG =====");

        Address defaultAddress = addressRepository.findByUserIdAndIsDefaultTrue(user.getId()).orElse(null);
        List<Address> addresses = addressRepository.findByUserId(user.getId());

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalCart", total);
        model.addAttribute("discount", discount);
        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("addresses", addresses);
        model.addAttribute("voucher", voucher);
        return "user/checkout/list";
    }
    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam String code,
                               HttpSession session,
                               RedirectAttributes ra) {

        if (code == null || code.trim().isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng nhập mã voucher");
            return "redirect:/checkout/list";
        }

        Optional<Voucher> voucherOpt = voucherRepository.findByCode(code.trim());

        if (voucherOpt.isEmpty()) {
            ra.addFlashAttribute("error","Voucher không tồn tại");
            return "redirect:/checkout/list";
        }

        Voucher voucher = voucherOpt.get();

        if(!Boolean.TRUE.equals(voucher.getActive())){
            ra.addFlashAttribute("error","Voucher không khả dụng");
            return "redirect:/checkout/list";
        }

        if(voucher.getValidTo() != null && voucher.getValidTo().isBefore(LocalDateTime.now())){
            ra.addFlashAttribute("error","Voucher đã hết hạn");
            return "redirect:/checkout/list";
        }

        // Kiểm tra giá trị đơn hàng
        List<CartItem> cartItems = cartItemRepository.findByUserId(((User)session.getAttribute("user")).getId());
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            BigDecimal price = item.getDisplayPrice();
            if (price == null) {
                // fallback nếu displayPrice chưa set
                price = item.getProductVariant().getAdditionalPrice();
                if (price == null) price = BigDecimal.ZERO;
                item.setDisplayPrice(price);
            }
            total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        if (voucher.getMinOrderAmount() != null &&
                total.compareTo(BigDecimal.valueOf(voucher.getMinOrderAmount())) < 0) {
            ra.addFlashAttribute("error", "Đơn hàng chưa đủ giá trị tối thiểu để áp dụng voucher");
            return "redirect:/checkout/list";
        }

        session.setAttribute("voucher", voucher);
        ra.addFlashAttribute("success","Áp dụng voucher thành công");

        return "redirect:/checkout/list";
    }
    // xóa voucher khỏi session
    @GetMapping("/cancel-voucher")
    public String cancelVoucher(HttpSession session, RedirectAttributes ra) {
        session.removeAttribute("voucher"); // Xóa voucher
        ra.addFlashAttribute("success", "Đã hủy áp dụng voucher");
        return "redirect:/checkout/list";
    }
}