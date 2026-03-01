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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private CartItemRepository cartItemRepository;
    private UserAddressRepository userAddressRepository;
    private OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private AddressRepository addressRepository;
    public CheckoutController(CartItemRepository cartItemRepository,
                              UserAddressRepository userAddressRepository,
                              OrderRepository orderRepository,
                              OrderItemRepository orderItemRepository,
                              AddressRepository addressRepository) {
        this.cartItemRepository = cartItemRepository;
        this.userAddressRepository = userAddressRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.addressRepository = addressRepository;
    }
    @GetMapping("/list")
    public String checkout(HttpSession httpSession, Model model) {
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<CartItem> cartitem = cartItemRepository.findByUserId(user.getId());
        Double total = cartItemRepository.calculateTotalByUserId(user.getId()) + 30000;
//        UserAddress defaultAddress  = userAddressRepository.findByUserIdAndIsDefaultTrue(user.getId())
//                        .orElse(null);
        Address defaultAddress = addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .orElse(null);
        List<Address> addresses =
                addressRepository.findByUserId(user.getId());
        model.addAttribute("cartItems", cartitem);
        model.addAttribute("totalCart", total);
        model.addAttribute("defaultAddress", defaultAddress );
        model.addAttribute("addresses", addresses);
        return "user/checkout/list";
    }
}
