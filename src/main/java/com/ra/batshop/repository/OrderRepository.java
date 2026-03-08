package com.ra.batshop.repository;

import com.ra.batshop.model.Enum.OrderStatus;
import com.ra.batshop.model.Order;
import com.ra.batshop.model.Product;
import com.ra.batshop.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
