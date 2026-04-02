package com.ra.batshop.controller;

import com.ra.batshop.config.VnpayConfig;
import com.ra.batshop.model.*;
import com.ra.batshop.model.Enum.OrderStatus;
import com.ra.batshop.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    @ModelAttribute
    public void addActiveMenu(Model model) {
        model.addAttribute("activeMenu", "orders");
    }

    private CartItemRepository cartItemRepository;
    //private UserAddressRepository userAddressRepository;
    private final OrderRepository orderRepository;
    private OrderItemRepository orderItemRepository;
    private final AddressRepository addressRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private  final  FlashSaleProductRepository flashSaleProductRepository;
    public OrderController(OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           //UserAddressRepository userAddressRepository,
                           CartItemRepository cartItemRepository,
                           ProductVariantRepository productVariantRepository,
                           AddressRepository addressRepository,
                           VoucherRepository voucherRepository,
                           UserVoucherRepository userVoucherRepository,
                           FlashSaleProductRepository flashSaleProductRepository) {

        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
       // this.userAddressRepository = userAddressRepository;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.addressRepository = addressRepository;
        this.voucherRepository = voucherRepository;
        this.userVoucherRepository = userVoucherRepository;
        this.flashSaleProductRepository = flashSaleProductRepository;
    }

    // LIST
    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(required = false) String paymentMethod,
                       @RequestParam(required = false) String paymentStatus,
                       @RequestParam(required = false) OrderStatus status,
                       HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        if (paymentMethod != null && paymentMethod.trim().isEmpty()) {
            paymentMethod = null;
        }

        if (paymentStatus != null && paymentStatus.trim().isEmpty()) {
            paymentStatus = null;
        }
        Page<Order> orderPage = orderRepository.filterOrders(paymentMethod, paymentStatus, status, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedMethod", paymentMethod);
        model.addAttribute("selectedPaymentStatus", paymentStatus);
        model.addAttribute("size", size);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("content", "admin/order/list");
        System.out.println("page = " + page);
        System.out.println("paymentMethod = " + paymentMethod);
        System.out.println("paymentStatus = " + paymentStatus);
        System.out.println("status = " + status);
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
    public String confirmCheckout(@RequestParam Long addressId,
                                  @RequestParam String paymentMethod,
                                  HttpSession session,
                                  HttpServletRequest request,
                                  RedirectAttributes redirectAttributes) throws Exception {

        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        List<CartItem> cartItems = cartItemRepository.findByUserId(user.getId());
        if (cartItems.isEmpty()) return "redirect:/cart/list";

        Voucher voucher = (Voucher) session.getAttribute("voucher");
        Address address = addressRepository.findById(addressId).orElseThrow();

        // ====== LẤY FLASH SALE TỪ SESSION ======
        Map<Integer, BigDecimal> flashSalePrices =
                (Map<Integer, BigDecimal>) session.getAttribute("flashSalePrices");

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal shippingFee = BigDecimal.valueOf(30000);

        // ====== TÍNH TOTAL ======
        for (CartItem item : cartItems) {
            BigDecimal price;
            Integer variantId = item.getProductVariant().getId(); // dùng variantId

            if (flashSalePrices != null && flashSalePrices.containsKey(variantId)) {
                // giá flash sale nếu có
                price = flashSalePrices.get(variantId);
                System.out.println("FS Price: " + price + " for variant " + variantId);
            } else {
                // giá variant chuẩn
                price = item.getProductVariant().getAdditionalPrice() != null
                        ? item.getProductVariant().getAdditionalPrice()
                        : BigDecimal.ZERO;
                System.out.println("Variant Price: " + price + " for variant " + variantId);
            }

            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(itemTotal);
        }

        // + ship
        total = total.add(shippingFee);

        // ====== ÁP DỤNG VOUCHER ======
        BigDecimal discount = BigDecimal.ZERO;

        if (voucher != null &&
                total.compareTo(BigDecimal.valueOf(voucher.getMinOrderAmount())) >= 0) {

            discount = total.multiply(BigDecimal.valueOf(voucher.getDiscountPercent()))
                    .divide(BigDecimal.valueOf(100));

            if (discount.compareTo(BigDecimal.valueOf(voucher.getMaxDiscountAmount())) > 0) {
                discount = BigDecimal.valueOf(voucher.getMaxDiscountAmount());
            }

            total = total.subtract(discount);
        }

        // ====== TẠO ORDER ======
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("UNPAID");
        order.setCreatedAt(LocalDateTime.now());

        // địa chỉ
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setCity(address.getCity());
        order.setDistrict(address.getDistrict());
        order.setWard(address.getWard());
        order.setDetail(address.getDetail());

        // voucher
        if (voucher != null && discount.compareTo(BigDecimal.ZERO) > 0) {
            order.setVoucher(voucher);
            order.setDiscountAmount(discount.intValue());

            Integer used = voucher.getTotalUsed() == null ? 0 : voucher.getTotalUsed();
            voucher.setTotalUsed(used + 1);
            voucherRepository.save(voucher);

            UserVoucher uv = new UserVoucher();
            uv.setUser(user);
            uv.setVoucher(voucher);
            uv.setUsedCount(1);
            uv.setLastUsedAt(LocalDateTime.now());
            userVoucherRepository.save(uv);
        }

        // total cuối cùng
        order.setTotalPrice(total);

        orderRepository.save(order);

        // ====== TẠO ORDER ITEM ======
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductVariant(cartItem.getProductVariant());
            orderItem.setQuantity(cartItem.getQuantity());

            BigDecimal price;
            Integer variantId = cartItem.getProductVariant().getId();

            if (flashSalePrices != null && flashSalePrices.containsKey(variantId)) {
                price = flashSalePrices.get(variantId); // giá FS
            } else {
                price = cartItem.getProductVariant().getAdditionalPrice() != null
                        ? cartItem.getProductVariant().getAdditionalPrice()
                        : BigDecimal.ZERO;
            }

            orderItem.setPrice(price);
            orderItemRepository.save(orderItem);
        }

        // ====== VNPAY ======
        if ("VNPAY".equals(paymentMethod)) {

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

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));

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
                    "Đặt hàng thành công! Vui lòng thanh toán.");

            return "redirect:" + vnp_PayUrl + "?" + query.toString();
        }

        // ====== COD ======
        reduceStock(order);
        cartItemRepository.deleteAll(cartItems);

        redirectAttributes.addFlashAttribute("successMessage",
                "Đặt hàng thành công!");

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
                            @RequestParam OrderStatus status,
                            @RequestParam(required = false) String cancelReason,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "5") int size,
                            @RequestParam(required = false) String paymentMethod,
                            @RequestParam(required = false) String paymentStatus,
                            RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findById(id).orElseThrow();
        //  éo cho sửa if can
        if (order.getStatus() == OrderStatus.CANCELLED) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Đơn hàng đã bị huỷ, không thể chỉnh sửa.");
            return "redirect:/admin/orders";
        }
        // if chuyển sang can hoàn stock
        if (status == OrderStatus.CANCELLED) {
            restoreStock(order);

            if (cancelReason != null && !cancelReason.trim().isEmpty()) {
                order.setCancelReason(cancelReason);
            } else if (order.getCancelReason() == null) {
                order.setCancelReason("Admin hủy đơn");
            }
        }
        // Nếu complete thì  paid
        if (status == OrderStatus.COMPLETED) {
            order.setPaymentStatus("PAID");
        }
        order.setStatus(status);
        orderRepository.save(order);
        // redirect kèm page và filter
        return "redirect:/admin/orders?page=" + page +
                "&size=" + size +
                (paymentMethod != null ? "&paymentMethod=" + paymentMethod : "") +
                (paymentStatus != null ? "&paymentStatus=" + paymentStatus : "");
    }
    private void restoreStock(Order order) {

        List<OrderItem> items =
                orderItemRepository.findByOrderId(order.getId());

        for (OrderItem item : items) {

            ProductVariant variant = item.getProductVariant();

            int newStock = variant.getStock() + item.getQuantity();

            variant.setStock(newStock);
            productVariantRepository.save(variant);
        }
    }
}
