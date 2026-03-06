package com.ra.batshop.repository;

import com.ra.batshop.model.FlashSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface FlashSaleRepository extends JpaRepository<FlashSale, Integer> {

    // Lấy Flash Sale đang diễn ra (thời gian hiện tại nằm giữa startDate và endDate)
    @Query("SELECT f FROM FlashSale f WHERE f.startDate <= :now AND f.endDate >= :now")
    Optional<FlashSale> findActiveFlashSale(@Param("now") LocalDateTime now);
}