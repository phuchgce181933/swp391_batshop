package com.ra.batshop.controller;

import com.ra.batshop.model.User;
import com.ra.batshop.repository.UserRepository;
import com.ra.batshop.repository.ProductRepository;
import com.ra.batshop.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
//        Double totalRevenue = orderRepository.getTotalRevenue();

//        if (totalRevenue == null) totalRevenue = 0.0;

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
//        model.addAttribute("totalRevenue", totalRevenue);

        // ===== Revenue theo tháng =====
//        List<Object[]> monthlyData = orderRepository.getMonthlyRevenue();
//
//        List<String> months = new ArrayList<>();
//        List<Double> revenues = new ArrayList<>();
//
//        for (Object[] row : monthlyData) {
//            months.add("Month " + row[0]);
//            revenues.add((Double) row[1]);
//        }

//        model.addAttribute("months", months);
//        model.addAttribute("revenues", revenues);
        model.addAttribute("content", "admin/dashboard-content");
        return "admin/layout";
    }

    @GetMapping("/manageruser")
    public String userPage(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("content", "admin/user/list");
        return "admin/layout";
    }

}