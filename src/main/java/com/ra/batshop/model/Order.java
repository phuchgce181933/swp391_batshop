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
    @JoinColumn(name = "user_id") // Nên chỉ định rõ tên cột liên kết với User
    private User user;

    // CascadeType.ALL để khi xóa Order thì tự động xóa hết OrderItem bên trong
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    /**
     * PHẦN QUAN TRỌNG NHẤT:
     * 1. nullable = true: Cho phép cột address_id trong DB trống (khi địa chỉ bị xóa).
     * 2. OnDelete action: Báo cho database biết nếu Address bị xóa, hãy set address_id của Order thành NULL.
     */
    @ManyToOne
    @JoinColumn(name = "address_id", nullable = true)
    private Address shippingAddress;

    // Hàm tự động gán thời gian tạo khi lưu vào DB
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}