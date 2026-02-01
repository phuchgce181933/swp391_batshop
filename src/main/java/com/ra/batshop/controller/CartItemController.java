package com.ra.batshop.controller;

import com.ra.batshop.model.CartItem;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.CartItemRepository;
import com.ra.batshop.repository.ProductVariantRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartItemController {
    private CartItemRepository cartItemRepository;
    public CartItemController(CartItemRepository cartItemRepository
                             ) {
        this.cartItemRepository = cartItemRepository;

    }

    @GetMapping("/list")
    public String findAll(Model model, HttpSession httpSession) {

        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<CartItem> cartitem = cartItemRepository.findByUserId(user.getId());
        model.addAttribute("cartItems", cartitem);
        return "user/cart/list";
    }
    @PostMapping("/delete")
    public String delete(@RequestParam("id") Integer id,
            HttpSession httpSession) {
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        cartItemRepository.deleteById(id);
        return "redirect:/cart/list";
    }
    @PostMapping("/update")
    public String updateCartItem(@RequestParam("id") Integer id,
                                 @RequestParam("quantity") Integer quantity,
                                 HttpSession httpSession) {
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        CartItem cartItem = cartItemRepository.findById(id).get();
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        return "redirect:/cart/list";
    }
}
