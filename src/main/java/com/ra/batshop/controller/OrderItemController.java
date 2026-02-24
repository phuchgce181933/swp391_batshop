package com.ra.batshop.controller;

import com.ra.batshop.model.OrderItem;
import com.ra.batshop.repository.OrderItemRepository;
import com.ra.batshop.repository.UserAddressRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Controller
@RequestMapping("/orders_item")
public class OrderItemController {
    private OrderItemRepository orderItemRepository;
    public OrderItemController(OrderItemRepository orderItemRepository
                               ) {
        this.orderItemRepository = orderItemRepository;
    }
    @GetMapping("/admin/{id}")
    public String getOrderItems(@PathVariable Integer id, Model model) {
        model.addAttribute("orderItems", orderItemRepository.findByOrderId(id));
        model.addAttribute("content", "admin/orderitem/list");
        return "admin/layout";
    }

}
