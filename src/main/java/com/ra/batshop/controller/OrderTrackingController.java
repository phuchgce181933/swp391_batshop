package com.ra.batshop.controller;


import com.ra.batshop.model.Enum.OrderStatus;
import com.ra.batshop.model.Order;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user/orders")
@RequiredArgsConstructor
public class OrderTrackingController {
    private final OrderRepository orderRepository;

    // Danh sách đơn hàng
    @GetMapping
    public String listOrders(@RequestParam(required = false) String status,
                             @RequestParam(required = false, defaultValue = "desc") String sort,
                             HttpSession session,
                             Model model) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/login";
        }

        // Xử lý sort
        Sort sortObj = sort.equals("asc") ?
                Sort.by("createdAt").ascending() :
                Sort.by("createdAt").descending();

        List<Order> orders;

        // Filter theo status nếu có
        if (status != null && !status.isEmpty()) {
            orders = orderRepository.findByUserAndStatus(user, OrderStatus.valueOf(status), sortObj);
        } else {
            orders = orderRepository.findByUser(user, sortObj);
        }

        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("sort", sort);
        model.addAttribute("OrderStatus", OrderStatus.values());

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
        model.addAttribute("OrderStatus", OrderStatus.class);
        return "user/order/order-tracking";
    }
    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable Integer id,
                              @RequestParam("reason") String reason,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Chỉ cho hủy nếu chưa hoàn tất hoặc chưa hủy
        if (order.getStatus() == null || order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.PROCESSING) {
            order.setStatus(OrderStatus.PROCESSING);
            order.setCancelReason(reason); // Lưu lý do hủy
            orderRepository.save(order);

            redirectAttributes.addFlashAttribute("message", "Đơn hàng đã được hủy thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy đơn hàng này!");
        }

        return "redirect:/user/orders";
    }
}
