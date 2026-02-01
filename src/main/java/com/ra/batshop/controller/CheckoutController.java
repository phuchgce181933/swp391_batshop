package com.ra.batshop.controller;

import com.ra.batshop.model.CartItem;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.CartItemRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private CartItemRepository cartItemRepository;
    public CheckoutController(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }
    @GetMapping("/list")
    public String checkout(HttpSession httpSession, Model model) {
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<CartItem> cartitem = cartItemRepository.findByUserId(user.getId());
        Double total = cartItemRepository.calculateTotalByUserId(user.getId()) + 30000;
        model.addAttribute("cartItems", cartitem);
        model.addAttribute("totalCart", total);
        return "user/checkout/list";
    }
}
