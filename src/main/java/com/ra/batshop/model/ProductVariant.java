package com.ra.batshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer stock;

    private BigDecimal additionalPrice;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "size_id")
    private Size size;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne
    @JoinColumn(name = "color_id")
    private Color color;


    @OneToOne(mappedBy = "variant", cascade = CascadeType.ALL)
    private RacketDetail racketDetail;

    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL)
    private List<ProductVariantImage> images = new ArrayList<>();

    public void addImage(ProductVariantImage image){
        images.add(image);
        image.setVariant(this);
    }
}

