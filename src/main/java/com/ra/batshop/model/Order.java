package com.ra.batshop.model;

import com.ra.batshop.model.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal totalPrice;

    private String paymentMethod;
    private String paymentStatus;
    //private String transactionNo;
    //private String vnpTxnRef;
    //private LocalDateTime paidAt;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;

    @ManyToOne
    private User user;

    @ManyToOne
    private UserAddress shippingAddress;
}

