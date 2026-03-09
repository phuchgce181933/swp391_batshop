package com.ra.batshop.repository;

import com.ra.batshop.model.OrderItem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.ra.batshop.model.Enum.OrderStatus;
import java.util.List;

public interface OrderItemRepository extends CrudRepository<OrderItem, Integer> {
    List<OrderItem> findByOrderId(Integer orderId);
    @Query("""
           SELECT COUNT(oi)
           FROM OrderItem oi
           WHERE oi.order.user.id = :userId
           AND oi.productVariant.product.id = :productId
           AND oi.order.status = :status
           """)
    Long checkUserPurchased(Integer userId,
                            Integer productId,
                            OrderStatus status);
}
