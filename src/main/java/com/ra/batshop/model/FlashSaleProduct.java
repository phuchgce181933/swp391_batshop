package com.ra.batshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "flash_sale_product")
public class FlashSaleProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private BigDecimal salePrice;

    @ManyToOne
    @JoinColumn(name = "flash_sale_id")
    private FlashSale flashSale;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
