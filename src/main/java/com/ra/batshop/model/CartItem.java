package com.ra.batshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer quantity;

    @Transient
    private BigDecimal displayPrice;
    public BigDecimal getDisplayPrice() {
        return displayPrice;
    }

    public void setDisplayPrice(BigDecimal displayPrice) {
        this.displayPrice = displayPrice;
    }
    @ManyToOne
    private User user;

    @ManyToOne
    private ProductVariant productVariant;
}
