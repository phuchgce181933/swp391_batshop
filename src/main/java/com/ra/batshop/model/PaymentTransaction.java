package com.ra.batshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    private Order order;

    private BigDecimal amount;


    private String vnpTxnRef;

    // (00 = success)
    private String responseCode;

    private Boolean success;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}

