package com.ra.batshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;
    private Integer discountPercent;
    private Integer maxDiscountAmount;
    private Integer minOrderAmount;
    private Integer totalUsageLimit;
    private Integer totalUsed;
    private Boolean active;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    @OneToMany(mappedBy = "voucher")
    private List<UserVoucher> userVouchers;

}

