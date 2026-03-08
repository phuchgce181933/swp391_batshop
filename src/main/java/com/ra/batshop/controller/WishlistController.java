package com.ra.batshop.controller;

import com.ra.batshop.model.Product;
import com.ra.batshop.model.User;
import com.ra.batshop.model.Wishlist;
import com.ra.batshop.repository.ProductRepository;
import com.ra.batshop.repository.WishlistRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    // Đổi productId từ Long thành Integer để khớp với Product.java
    @GetMapping("/add/{productId}")
    public String addToWishlist(@PathVariable Integer productId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // Bây giờ findById sẽ nhận Integer và không còn báo lỗi đỏ nữa
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
            if (wishlistRepository.findByUserAndProduct(user, product).isEmpty()) {
                Wishlist wish = new Wishlist();
                wish.setUser(user);
                wish.setProduct(product);
                wishlistRepository.save(wish);
            }
        }
        return "redirect:/wishlist/list";
    }

    @GetMapping("/list")
    public String showWishlist(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<Wishlist> items = wishlistRepository.findByUser(user);
        model.addAttribute("wishlistItems", items);
        return "profile/wishlist";
    }

    @GetMapping("/delete/{id}")
    public String deleteFromWishlist(@PathVariable("id") Long id) {
        wishlistRepository.deleteById(id);
        return "redirect:/wishlist/list";
    }
}