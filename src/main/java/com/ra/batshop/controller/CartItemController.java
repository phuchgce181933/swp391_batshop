package com.ra.batshop.controller;

import com.ra.batshop.model.CartItem;
import com.ra.batshop.model.FlashSaleProduct;
import com.ra.batshop.model.ProductVariant;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.CartItemRepository;
import com.ra.batshop.repository.FlashSaleProductRepository;
import com.ra.batshop.repository.ProductVariantRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/cart")
public class CartItemController {
    private CartItemRepository cartItemRepository;
    private ProductVariantRepository productVariantRepository;
    private FlashSaleProductRepository flashSaleProductRepository;
    public CartItemController(CartItemRepository cartItemRepository,
                              ProductVariantRepository productVariantRepository,
                              FlashSaleProductRepository flashSaleProductRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.flashSaleProductRepository = flashSaleProductRepository;
    }

    @PostMapping("/add")
    public String addCartItem(@RequestParam("productVariantId") Integer productvariantId,
                              HttpSession httpSession,
                              @RequestParam("quantity") Integer quantity) {

        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        ProductVariant variant = productVariantRepository
                .findById(productvariantId)
                .orElseThrow();

        Integer stock = variant.getStock();
        if (quantity == null || quantity <= 0) {
            return "redirect:/product/detail/" + productvariantId + "?error=invalid_quantity";
        }
        if(quantity > stock){
            return "redirect:/product/detail/" + variant.getProduct().getId() + "?error=stock&stock=" + stock;
        }

        Optional<CartItem> cartItemOption =
                cartItemRepository.findByUserIdAndProductVariantId(user.getId(), productvariantId);
        int currentQuantity = cartItemOption.map(CartItem::getQuantity).orElse(0);
        if (currentQuantity + quantity > stock) {
            return "redirect:/product/detail/" + variant.getProduct().getId()
                    + "?error=stock&stock=" + stock
                    + "&current=" + currentQuantity;
        }

        CartItem cartItem;

        if (cartItemOption.isPresent()) {
            cartItem = cartItemOption.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProductVariant(variant);
            cartItem.setQuantity(quantity);
        }

        cartItemRepository.save(cartItem);


       //(LƯU GIÁ FLASH SALE)

        Map<Integer, BigDecimal> flashSalePrices =
                (Map<Integer, BigDecimal>) httpSession.getAttribute("flashSalePrices");

        if (flashSalePrices == null) {
            flashSalePrices = new HashMap<>();
        }

        BigDecimal finalPrice;

        BigDecimal basePrice = variant.getAdditionalPrice() != null ? variant.getAdditionalPrice() : BigDecimal.ZERO;

        Optional<FlashSaleProduct> fspOpt =
                flashSaleProductRepository.findActiveByProductId(
                        variant.getProduct().getId(),
                        LocalDateTime.now()
                );

        if (fspOpt.isPresent()) {
            BigDecimal discountPercent = BigDecimal.valueOf(fspOpt.get().getDiscountPercent() != null ? fspOpt.get().getDiscountPercent() : 0);
            finalPrice = basePrice.multiply(BigDecimal.valueOf(100).subtract(discountPercent))
                    .divide(BigDecimal.valueOf(100));
        } else {
            finalPrice = basePrice;
        }

        flashSalePrices.put(cartItem.getId(), finalPrice);

        httpSession.setAttribute("flashSalePrices", flashSalePrices);

        return "redirect:/cart/list";
    }

    @GetMapping("/list")
    public String findAll(Model model, HttpSession httpSession) {

        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        if (cartItems == null) {
            cartItems = List.of();
        }

        // Map lưu giá flash sale theo itemId
        Map<Integer, BigDecimal> flashSalePrices = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            ProductVariant variant = item.getProductVariant();

            Optional<FlashSaleProduct> flashSaleOpt =
                    flashSaleProductRepository.findActiveByProductId(
                            variant.getProduct().getId(),
                            LocalDateTime.now()
                    );

            BigDecimal basePrice = variant.getAdditionalPrice() != null ? variant.getAdditionalPrice() : BigDecimal.ZERO;

            BigDecimal price;
            if (flashSaleOpt.isPresent()) {
                BigDecimal discountPercent = BigDecimal.valueOf(
                        flashSaleOpt.get().getDiscountPercent() != null ? flashSaleOpt.get().getDiscountPercent() : 0
                );
                price = basePrice.multiply(BigDecimal.valueOf(100).subtract(discountPercent))
                        .divide(BigDecimal.valueOf(100));
            } else {
                price = basePrice;
            }

            flashSalePrices.put(item.getId(), price); // lưu theo itemId
            total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        httpSession.setAttribute("flashSalePrices", flashSalePrices);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalCart", total.doubleValue());

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
                                 @RequestParam("productVariantId") Integer productvariantId,
                                 HttpSession httpSession) {
        User user = (User) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        ProductVariant variant = productVariantRepository
                .findById(productvariantId)
                .orElseThrow();
        Integer stock = variant.getStock();
        if (quantity == null || quantity <= 0) {
            return "redirect:/cart/list?error=invalid_quantity";
        }
        if(quantity > stock){
            return "redirect:/cart/list?error=stock&stock=" + stock;
        }
        CartItem cartItem = cartItemRepository.findById(id).get();
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        return "redirect:/cart/list";
    }
}