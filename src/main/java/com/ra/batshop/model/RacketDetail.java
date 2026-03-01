package com.ra.batshop.model;


import com.ra.batshop.model.Enum.*;
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
    private RacketLength length;

    @Enumerated(EnumType.STRING)
    private RacketHandleLength racketHandleLength;


    private String style;


    private String technology;


    private String gamecontent;

    private String weight;

    private String swingWeight;

    @Enumerated(EnumType.STRING)
    private EquilibriumPoint equilibriumPoint;

    @Enumerated(EnumType.STRING)
    private ChopstickHardness chopstickHardness;
}
