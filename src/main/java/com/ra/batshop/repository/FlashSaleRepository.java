package com.ra.batshop.repository;

import com.ra.batshop.model.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlashSaleRepository extends JpaRepository<FlashSale, Integer> {

    @Query("SELECT f FROM FlashSale f WHERE f.startDate <= :now AND f.endDate >= :now ORDER BY f.endDate ASC")
    List<FlashSale> findActiveFlashSales(@Param("now") LocalDateTime now);
    Optional<FlashSale> findFirstByStartDateAfterOrderByStartDateAsc(LocalDateTime now);

    // KIỂM TRA TRÙNG LẶP SẢN PHẨM
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
            "FROM FlashSale f JOIN f.products fp " +
            "WHERE fp.product.id = :productId " +
            "AND f.id != :currentFlashSaleId " +
            "AND f.startDate <= :endDate AND f.endDate >= :startDate")
    boolean isProductInOverlappingSale(@Param("productId") Integer productId,
                                       @Param("currentFlashSaleId") Integer currentFlashSaleId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    // KIỂM TRA TRÙNG LẶP THỜI GIAN FLASH SALE
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FlashSale f " +
            "WHERE f.startDate < :endDate AND f.endDate > :startDate " +
            "AND (f.id != :excludeId OR :excludeId IS NULL)")
    boolean isFlashSaleTimeOverlapping(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("excludeId") Integer excludeId);

    // ===============================================
    // LẤY DANH SÁCH FLASH SALE TRONG 1 NGÀY (Để check khung giờ trống)
    // ===============================================
    @Query("SELECT f FROM FlashSale f WHERE f.startDate >= :startOfDay AND f.startDate < :nextDay AND (f.id != :excludeId OR :excludeId IS NULL)")
    List<FlashSale> findFlashSalesByDate(@Param("startOfDay") LocalDateTime startOfDay,
                                         @Param("nextDay") LocalDateTime nextDay,
                                         @Param("excludeId") Integer excludeId);
}