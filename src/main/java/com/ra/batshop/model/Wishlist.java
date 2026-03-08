package com.ra.batshop.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlists")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Người dùng nào thích?

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product; // Thích sản phẩm nào?
}