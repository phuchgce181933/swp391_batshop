package com.ra.batshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class FlashSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer discountPercent;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "flashSale", cascade = CascadeType.ALL)
    private List<FlashSaleProduct> products;

    // ===============================================
    // THÊM ĐOẠN CODE NÀY ĐỂ TÍNH TRẠNG THÁI TỰ ĐỘNG
    // ===============================================
    @Transient
    public String getStatus() {
        if (startDate == null || endDate == null) {
            return "Chưa xác định";
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(startDate)) {
            return "Sắp diễn ra";
        } else if (now.isAfter(endDate)) {
            return "Đã kết thúc";
        } else {
            return "Đang diễn ra";
        }
    }
}