package com.ra.batshop.repository;

import com.ra.batshop.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem,Integer> {
    List<CartItem> findByUserId(Integer userId);
    Optional<CartItem> findByUserIdAndProductVariantId(Integer userId, Integer productVariantId);
    @Query("""
        SELECT SUM(ci.quantity * pv.additionalPrice)
        FROM CartItem ci
        JOIN ci.productVariant pv
        WHERE ci.user.id = :userId
    """)
    Double calculateTotalByUserId(@Param("userId") Integer userId);
}
