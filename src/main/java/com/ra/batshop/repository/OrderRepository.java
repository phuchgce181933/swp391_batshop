package com.ra.batshop.repository;

import com.ra.batshop.model.Enum.OrderStatus;
import com.ra.batshop.model.Order;
import com.ra.batshop.model.Product;
import com.ra.batshop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    // Lấy danh sách đơn của user
    List<Order> findByUser(User user);

    // Lấy đơn theo id và user (để bảo mật)
    Optional<Order> findByIdAndUser(Integer id, User user);

    @Query("""
SELECT o FROM Order o
WHERE (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod)
AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)
AND (:status IS NULL OR o.status = :status)
""")
    Page<Order> filterOrders(String paymentMethod,
                             String paymentStatus,
                             OrderStatus status,
                             Pageable pageable);

    @Query("""
SELECT SUM(o.totalPrice)
FROM Order o
WHERE o.status = com.ra.batshop.model.Enum.OrderStatus.COMPLETED
""")
    Double getTotalRevenue();

    @Query("""
SELECT MONTH(o.createdAt), SUM(o.totalPrice)
FROM Order o
WHERE o.status = com.ra.batshop.model.Enum.OrderStatus.COMPLETED
GROUP BY MONTH(o.createdAt)
ORDER BY MONTH(o.createdAt)
""")
    List<Object[]> getMonthlyRevenue();


    @Query("""
SELECT COUNT(o)
FROM Order o
WHERE o.voucher.id = :voucherId
AND o.status = com.ra.batshop.model.Enum.OrderStatus.COMPLETED
""")
    int countVoucherUsed(Integer voucherId);

    @Query("""
SELECT SUM(o.totalPrice)
FROM Order o
WHERE o.status = com.ra.batshop.model.Enum.OrderStatus.COMPLETED
AND o.createdAt BETWEEN :fromDate AND :toDate
""")
    Double getRevenueByDateRange(LocalDateTime fromDate, LocalDateTime toDate);
}