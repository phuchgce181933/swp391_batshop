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
    private UserVoucherRepository userVoucherRepository;
    private FlashSaleProductRepository flashSaleProductRepository;
    public CheckoutController(CartItemRepository cartItemRepository,
                              // UserAddressRepository userAddressRepository,
                              OrderRepository orderRepository,
                              OrderItemRepository orderItemRepository,
                              AddressRepository addressRepository,
                              VoucherRepository voucherRepository,
                              UserVoucherRepository userVoucherRepository,
                              FlashSaleProductRepository flashSaleProductRepository) {
        this.cartItemRepository = cartItemRepository;
        // this.userAddressRepository = userAddressRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
        this.voucherRepository = voucherRepository;
        this.flashSaleProductRepository = flashSaleProductRepository;
        this.userVoucherRepository = userVoucherRepository;
    }
    @GetMapping("/list")
    public String checkout(HttpSession httpSession, Model model, RedirectAttributes ra) {
        User user = (User) httpSession.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal shippingFee = BigDecimal.valueOf(30000);

        System.out.println("===== DEBUG CHECKOUT =====");
        Map<Integer, BigDecimal> flashSalePrices = new HashMap<>();

        // Tính giá từng item, áp dụng flash sale nếu có
        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();

            Optional<FlashSaleProduct> flashSaleOpt =
                    flashSaleProductRepository.findActiveByProductId(
                            variant.getProduct().getId(), LocalDateTime.now()
                    );

            BigDecimal basePrice = variant.getAdditionalPrice() != null ? variant.getAdditionalPrice() : BigDecimal.ZERO;

            BigDecimal price;
            if (flashSaleOpt.isPresent()) {
                BigDecimal discountPercent = BigDecimal.valueOf(flashSaleOpt.get().getDiscountPercent() != null ? flashSaleOpt.get().getDiscountPercent() : 0);
                price = basePrice.multiply(BigDecimal.valueOf(100).subtract(discountPercent))
                        .divide(BigDecimal.valueOf(100));
            } else {
                price = basePrice;
            }

            item.setDisplayPrice(price);
            flashSalePrices.put(variant.getId(), price); // lưu giá theo variantId
            total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        httpSession.setAttribute("flashSalePrices", flashSalePrices); // lưu vào session

        BigDecimal subtotal = total; // tổng giá sản phẩm
        Voucher voucher = (Voucher) httpSession.getAttribute("voucher");
        BigDecimal discount = BigDecimal.ZERO;

        // Áp dụng voucher (chỉ trên subtotal)
        if (voucher != null && subtotal.compareTo(BigDecimal.valueOf(voucher.getMinOrderAmount())) >= 0) {
            discount = subtotal.multiply(BigDecimal.valueOf(voucher.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100));
            if (discount.compareTo(BigDecimal.valueOf(voucher.getMaxDiscountAmount())) > 0)
                discount = BigDecimal.valueOf(voucher.getMaxDiscountAmount());
            total = subtotal.subtract(discount);
        }

        BigDecimal finalTotal = total.add(shippingFee);
        System.out.println("TOTAL" + finalTotal);// cộng phí ship
        System.out.println("TOTAL CART: " + total);
        System.out.println("===== END DEBUG =====");

        // Chặn giỏ rỗng
        if (cartItems == null || cartItems.isEmpty()) {
            return "redirect:/cart/list?error=empty_cart";
        }

        Address defaultAddress = addressRepository.findByUserIdAndIsDefaultTrue(user.getId()).orElse(null);
        List<Address> addresses = addressRepository.findByUserId(user.getId());
        if (addresses == null || addresses.isEmpty()) {
            ra.addFlashAttribute("error", "Bạn chưa có địa chỉ nào! Vui lòng thêm địa chỉ.");
            return "redirect:/address/list?error=no_address";
        } else if (defaultAddress == null) {
            ra.addFlashAttribute("error", "Bạn chưa có địa chỉ mặc định! Vui lòng chọn địa chỉ mặc định.");
            return "redirect:/address/list?error=no_default_address";
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("shippingFee", shippingFee);
        model.addAttribute("totalCart", finalTotal);
        model.addAttribute("defaultAddress", defaultAddress);
        model.addAttribute("addresses", addresses);
        model.addAttribute("voucher", voucher);

        return "user/checkout/list";
    }
    @PostMapping("/apply-voucher")
    public String applyVoucher(@RequestParam String code,
                               HttpSession session,
                               RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            ra.addFlashAttribute("error", "Vui lòng đăng nhập để áp dụng voucher");
            return "redirect:/login";
        }

        Optional<Voucher> voucherOpt = voucherRepository.findByCode(code.trim());
        if (voucherOpt.isEmpty()) {
            ra.addFlashAttribute("error","Voucher không tồn tại");
            return "redirect:/checkout/list";
        }

        Voucher voucher = voucherOpt.get();

        // Kiểm tra voucher đã hết hạn / không active
        if (!Boolean.TRUE.equals(voucher.getActive()) ||
                (voucher.getValidTo() != null && voucher.getValidTo().isBefore(LocalDateTime.now()))) {
            ra.addFlashAttribute("error","Voucher không khả dụng");
            return "redirect:/checkout/list";
        }

        // Kiểm tra user đã dùng voucher chưa
        Optional<UserVoucher> userVoucherOpt = userVoucherRepository.findByUserAndVoucher(user, voucher);
        if (userVoucherOpt.isPresent() && userVoucherOpt.get().getUsedCount() != null && userVoucherOpt.get().getUsedCount() > 0) {
            ra.addFlashAttribute("error","Voucher này đã được sử dụng rồi");
            return "redirect:/checkout/list";
        }

        // Kiểm tra giá trị đơn hàng
        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            BigDecimal price = item.getDisplayPrice();
            if (price == null) price = item.getProductVariant().getAdditionalPrice();
            total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        if (voucher.getMinOrderAmount() != null &&
                total.compareTo(BigDecimal.valueOf(voucher.getMinOrderAmount())) < 0) {
            ra.addFlashAttribute("error", "Đơn hàng chưa đủ giá trị tối thiểu để áp dụng voucher");
            return "redirect:/checkout/list";
        }

        // Áp dụng voucher
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