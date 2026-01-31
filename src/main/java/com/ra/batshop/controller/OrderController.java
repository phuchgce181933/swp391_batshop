package com.ra.batshop.controller;

import com.ra.batshop.model.Enum.OrderStatus;
import com.ra.batshop.model.Order;
import com.ra.batshop.repository.OrderRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // LIST
    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order/list";
    }

    // DETAIL
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("order",
                orderRepository.findById(id).orElseThrow());
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/order/detail";
    }

    // UPDATE STATUS
    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Integer id,
                               @RequestParam OrderStatus status) {
        Order order = orderRepository.findById(id).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
        return "redirect:/admin/orders/" + id;
    }
}
