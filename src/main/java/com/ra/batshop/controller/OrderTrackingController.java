package com.ra.batshop.controller;


import com.ra.batshop.model.Order;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/user/orders")
@RequiredArgsConstructor
public class OrderTrackingController {
    private final OrderRepository orderRepository;

    // Danh sách đơn hàng
    @GetMapping
    public String listOrders(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderRepository.findByUser(user);
        model.addAttribute("orders", orders);

        return "user/order/order-list";
    }

    // Tracking chi tiết đơn
    @GetMapping("/{id}")
    public String orderTracking(@PathVariable Integer id,
                                HttpSession session,
                                Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("order", order);

        return "user/order/order-tracking";
    }
}
