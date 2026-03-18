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
    public CheckoutController(CartItemRepository cartItemRepository,
                             // UserAddressRepository userAddressRepository,
                              OrderRepository orderRepository,
                              OrderItemRepository orderItemRepository,
                              AddressRepository addressRepository,
                              VoucherRepository voucherRepository) {
        this.cartItemRepository = cartItemRepository;
       // this.userAddressRepository = userAddressRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
        this.voucherRepository = voucherRepository;
    }
    @GetMapping("/list")
    public String checkout(HttpSession httpSession, Model model) {
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<CartItem> cartitem = cartItemRepository.findByUserId(user.getId());
        Double total = cartItemRepository.calculateTotalByUserId(user.getId()) + 30000;
        Voucher voucher = (Voucher) httpSession.getAttribute("voucher");

        Integer discount = 0;

        if(voucher != null){

            if(total >= voucher.getMinOrderAmount()){

                discount = total.intValue() * voucher.getDiscountPercent() / 100;

                if(discount > voucher.getMaxDiscountAmount()){
                    discount = voucher.getMaxDiscountAmount();
                }

                total = total - discount;
            }
        }

        Address defaultAddress = addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElse(null);
        List<Address> addresses =
                addressRepository.findByUserId(user.getId());
        model.addAttribute("cartItems", cartitem);
        model.addAttribute("totalCart", total);
        model.addAttribute("discount", discount);
        model.addAttribute("defaultAddress", defaultAddress );
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
