package com.ra.batshop.controller;

import com.ra.batshop.model.Category;
import com.ra.batshop.model.Product;
import com.ra.batshop.model.User;
import com.ra.batshop.model.Wishlist;
import com.ra.batshop.repository.CategoryRepository;
import com.ra.batshop.repository.ProductRepository;
import com.ra.batshop.repository.WishlistRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/add/{productId}")
    public String addToWishlist(@PathVariable Integer productId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

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
    public String showWishlist(HttpSession session,
                               Model model,
                               @RequestParam(name = "categoryId", required = false) Integer categoryId) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        // 1. Lấy tất cả danh sách yêu thích của user
        List<Wishlist> items = wishlistRepository.findByUser(user);

        // 2. Nếu có chọn categoryId, tiến hành lọc danh sách
        if (categoryId != null) {
            items = items.stream()
                    .filter(item -> item.getProduct().getCategory() != null &&
                            item.getProduct().getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        // 3. Gửi dữ liệu ra view
        model.addAttribute("wishlistItems", items);
        model.addAttribute("categories", categoryRepository.findAll()); // Để đổ vào Dropdown
        model.addAttribute("selectedCategory", categoryId); // Để giữ trạng thái đã chọn

        return "profile/wishlist";
    }

    @GetMapping("/delete/{id}")
    public String deleteFromWishlist(@PathVariable("id") Long id) {
        wishlistRepository.deleteById(id);
        return "redirect:/wishlist/list";
    }
}