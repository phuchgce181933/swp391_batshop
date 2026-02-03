package com.ra.batshop.model;


import com.ra.batshop.model.Enum.RacketLevel;
import com.ra.batshop.model.Enum.RacketStyle;
import com.ra.batshop.model.Enum.RacketWeight;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class RacketDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    private RacketLevel level;

    @Enumerated(EnumType.STRING)
    private RacketStyle style;

    @Enumerated(EnumType.STRING)
    private RacketWeight weight;
}
