package com.ra.batshop.controller;

import com.ra.batshop.config.VnpayConfig;
import com.ra.batshop.model.*;
import com.ra.batshop.model.Enum.OrderStatus;
import com.ra.batshop.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {
    private CartItemRepository cartItemRepository;
    private UserAddressRepository userAddressRepository;
    private final OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    public OrderController(OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           UserAddressRepository userAddressRepository,
                           CartItemRepository cartItemRepository,
                           ProductVariantRepository productVariantRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userAddressRepository = userAddressRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
    }

    // LIST
    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("content", "admin/order/list");
        return "admin/layout";
    }

    // DETAIL
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("order", orderRepository.findById(id).orElseThrow());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("content", "admin/order/detail");
        return "admin/layout";
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

    // phúc-thanh toán
    private void reduceStock(Order order) {

        List<OrderItem> items =
                orderItemRepository.findByOrderId(order.getId());

        for (OrderItem item : items) {

            ProductVariant variant = item.getProductVariant();

            int newStock = variant.getStock() - item.getQuantity();

            if (newStock < 0) {
                throw new RuntimeException("Sản phẩm không đủ hàng");
            }

            variant.setStock(newStock);
            productVariantRepository.save(variant);
        }
    }
    @PostMapping("/confirm")
    public String confirmCheckout(@RequestParam Integer addressId,
                                  @RequestParam String paymentMethod,
                                  HttpSession session,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) throws Exception {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        if (cartItems.isEmpty()) return "redirect:/cart/list";

        UserAddress address = userAddressRepository.findById(addressId).orElseThrow();

        Double total = cartItemRepository.calculateTotalByUserId(user.getId()) + 30000d;

        // TẠO ORDER
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(address);
        order.setTotalPrice(BigDecimal.valueOf(total));
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("UNPAID");
        order.setCreatedAt(LocalDateTime.now());

        orderRepository.save(order);

        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductVariant(cartItem.getProductVariant());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProductVariant().getAdditionalPrice());
            orderItemRepository.save(orderItem);
        }

        // NẾU VNPAY
        if (paymentMethod.equals("VNPAY")) {

            String vnp_TmnCode = "OYACTOJC";
            String secretKey = "XFGP8FIP0H7436QDT2IWF8U23FWE6OM4";
            String vnp_ReturnUrl = "http://localhost:8080/admin/orders/vnpay-return";
            String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

            long amount = order.getTotalPrice().longValue() * 100;

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", "2.1.0");
            vnp_Params.put("vnp_Command", "pay");
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_TxnRef", order.getId().toString());
            vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + order.getId());
            vnp_Params.put("vnp_OrderType", "other");
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", request.getRemoteAddr());

            // Tạo thời gian
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

            // Build query giống ajaxServlet
            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if (fieldValue != null && fieldValue.length() > 0) {
                    hashData.append(fieldName).append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII))
                            .append('&');
                    hashData.append('&');
                }
            }

            hashData.deleteCharAt(hashData.length() - 1);

            String vnp_SecureHash = VnpayConfig.hmacSHA512(secretKey, hashData.toString());
            query.append("vnp_SecureHash=").append(vnp_SecureHash);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt hàng thành công! Vui lòng chờ xác nhận.");
            return "redirect:" + vnp_PayUrl + "?" + query.toString();
        }

        // COD
        reduceStock(order);
        cartItemRepository.deleteAll(cartItems);
        redirectAttributes.addFlashAttribute("successMessage",
                "Đặt hàng thành công! Vui lòng chờ xác nhận.");
        return "redirect:/home";
    }
    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, HttpSession session,
                              RedirectAttributes redirectAttributes) {

        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");

        Order order = orderRepository.findById(Integer.parseInt(txnRef)).orElseThrow();

        if ("00".equals(responseCode)) {
            order.setPaymentStatus("PAID");
            order.setStatus(OrderStatus.CONFIRMED);
            // TRỪ STOCK
            reduceStock(order);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thanh toán VNPAY thành công!");            User user = (User) session.getAttribute("user");
            if (user != null) {
                List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
                cartItemRepository.deleteAll(cartItems);
            }
        } else {
            order.setPaymentStatus("FAILED");
        }

        orderRepository.save(order);

        return "redirect:/home";
    }
    @PostMapping("/edit/{id}")
    public String editOrder(@PathVariable Integer id,
                            @RequestParam OrderStatus status) {
        Order order = orderRepository.findById(id).orElseThrow();
        order.setStatus(status);
        orderRepository.save(order);
        return "redirect:/admin/orders";
    }
}
