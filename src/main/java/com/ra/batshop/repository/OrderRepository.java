package com.ra.batshop.repository;

import com.ra.batshop.model.Order;
import com.ra.batshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    // Lấy danh sách đơn của user
    List<Order> findByUser(User user);

    // Lấy đơn theo id và user (để bảo mật)
    Optional<Order> findByIdAndUser(Integer id, User user);
}
