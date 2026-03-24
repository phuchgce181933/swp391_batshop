package com.ra.batshop.repository;

import com.ra.batshop.model.FlashSaleProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FlashSaleProductRepository extends JpaRepository<FlashSaleProduct, Integer> {
    // check sản phẩm giảm dá
    @Query("SELECT fsp FROM FlashSaleProduct fsp " +
            "WHERE fsp.product.id = :productId " +
            "AND fsp.flashSale.startDate <= :now " +
            "AND fsp.flashSale.endDate >= :now")
    Optional<FlashSaleProduct> findActiveByProductId(@Param("productId") Integer productId,
                                                     @Param("now") LocalDateTime now);
}