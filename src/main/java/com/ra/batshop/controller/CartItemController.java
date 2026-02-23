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
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartItemController {
    private CartItemRepository cartItemRepository;
    private ProductVariantRepository productVariantRepository;
    public CartItemController(CartItemRepository cartItemRepository,
                              ProductVariantRepository productVariantRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @PostMapping("/add")
    public String addCartItem(@RequestParam("productVariantId") Integer productvariantId,
                               HttpSession httpSession) {
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        Optional<CartItem> cartItemOption = cartItemRepository.findByUserIdAndProductVariantId(user.getId(), productvariantId);
        if (cartItemOption.isPresent()) {
            CartItem cartItem = cartItemOption.get();
            cartItem.setQuantity(cartItem.getQuantity() + 1);
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProductVariant(productVariantRepository.findById(productvariantId).get());
            cartItem.setQuantity(1);
            cartItemRepository.save(cartItem);
        }
        return "redirect:/cart/list";
    }

    @GetMapping("/list")
    public String findAll(Model model, HttpSession httpSession) {

        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<CartItem> cartitem = cartItemRepository.findByUserId(user.getId());
        if (cartitem == null) {
            cartitem = List.of();
        }

        Double total = cartItemRepository.calculateTotalByUserId(user.getId());
        if (total == null) {
            total = 0.0;
        }
        model.addAttribute("cartItems", cartitem);
        model.addAttribute("totalCart", total );
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
