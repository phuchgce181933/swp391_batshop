package com.ra.batshop.model;

import com.ra.batshop.model.Enum.OrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor // Thêm Constructor không đối số cho Hibernate
@AllArgsConstructor // Thêm Constructor đầy đủ đối số
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal totalPrice;

    private String paymentMethod;
    private String paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OrderStatus status;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = true)
    private Address shippingAddress;

    @ManyToOne
    @JoinColumn(name = "voucher_id")
    private Voucher voucher;

    private Integer discountAmount;
    // Hàm tự động gán thời gian tạo khi lưu vào DB
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}


