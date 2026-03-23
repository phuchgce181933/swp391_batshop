package com.ra.batshop.controller;

import com.ra.batshop.model.Enum.OrderStatus;
import com.ra.batshop.model.Order;
import com.ra.batshop.model.User;
import com.ra.batshop.repository.UserRepository;
import com.ra.batshop.repository.ProductRepository;
import com.ra.batshop.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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

        Double totalRevenue = Optional.ofNullable(
                orderRepository.getTotalRevenue()
        ).orElse(0.0);

        // ===== Revenue theo thời gian =====
        LocalDateTime now = LocalDateTime.now();

        Double revenue3Days = Optional.ofNullable(
                orderRepository.getRevenueFromDate(now.minusDays(3))
        ).orElse(0.0);

        Double revenue7Days = Optional.ofNullable(
                orderRepository.getRevenueFromDate(now.minusDays(7))
        ).orElse(0.0);

        // Mặc định load 7 ngày
        Map<String, Double> chartData = buildChartData(7);

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalRevenue", totalRevenue);

        model.addAttribute("revenue3Days", revenue3Days);
        model.addAttribute("revenue7Days", revenue7Days);

        model.addAttribute("months", chartData.keySet());
        model.addAttribute("revenues", chartData.values());

        model.addAttribute("content", "admin/dashboard-content");

        return "admin/layout";
    }

    // ================= API FILTER CHART =================
    @GetMapping("/dashboard/chart")
    @ResponseBody
    public Map<String, Object> getChart(@RequestParam int days) {

        Map<String, Double> chartData = buildChartData(days);

        Map<String, Object> result = new HashMap<>();
        result.put("labels", chartData.keySet());
        result.put("values", chartData.values());

        return result;
    }

    // ================= LOGIC CHART =================
    private Map<String, Double> buildChartData(int days) {

        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);

        List<Order> orders = orderRepository
                .findByCreatedAtAfterAndStatus(fromDate, OrderStatus.COMPLETED);

        Map<String, Double> revenueMap = new LinkedHashMap<>();

        // tạo ngày rỗng trước
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            revenueMap.put(date.toString(), 0.0);
        }

        // cộng tiền
        for (Order o : orders) {
            String date = o.getCreatedAt().toLocalDate().toString();

            revenueMap.put(
                    date,
                    revenueMap.getOrDefault(date, 0.0) + o.getTotalPrice().doubleValue()
            );
        }

        return revenueMap;
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