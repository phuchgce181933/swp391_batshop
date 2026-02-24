package com.ra.batshop.repository;

import com.ra.batshop.model.OrderItem;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderItemRepository extends CrudRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderId(Integer orderId);
}
