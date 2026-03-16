package com.ra.batshop.controller;

import com.ra.batshop.model.User;
import com.ra.batshop.repository.UserRepository;
import com.ra.batshop.repository.ProductRepository;
import com.ra.batshop.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public AdminController(UserRepository userRepository,
                           ProductRepository productRepository,
                           OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        Double totalRevenue = orderRepository.getTotalRevenue();

        if (totalRevenue == null) totalRevenue = 0.0;

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);

         // ===== Revenue theo tháng =====
        List<Object[]> monthlyData = orderRepository.getMonthlyRevenue();

        List<String> months = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();

        for (Object[] row : monthlyData) {
            months.add("Month " + row[0]);
            revenues.add(((Number) row[1]).doubleValue());
        }

        model.addAttribute("months", months);
        model.addAttribute("revenues", revenues);
        model.addAttribute("content", "admin/dashboard-content");
        return "admin/layout";
    }

    @GetMapping("/users")
    public String listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean status,
            Model model) {

        List<User> users;

        if (keyword != null && !keyword.isEmpty() && status != null) {

            users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndStatus(keyword, keyword, status);

        } else if (keyword != null && !keyword.isEmpty()) {
            users = userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);

        } else if (status != null) {

            users = userRepository.findByStatus(status);

        } else {
            users = userRepository.findAll();
        }

        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("content", "admin/user/list");

        return "admin/layout";
    }

    @GetMapping("/users/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Integer id) {

        User user = userRepository.findById(id).orElse(null);

        if (user != null) {
            user.setStatus(!user.getStatus());
            userRepository.save(user);
        }

        return "redirect:/admin/users";
    }

}