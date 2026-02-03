package com.ra.batshop.model;

import com.ra.batshop.model.Enum.RacketLevel;
import com.ra.batshop.model.Enum.RacketStyle;
import com.ra.batshop.model.Enum.RacketWeight;
import jakarta.persistence.*;

@Entity
public class RacketDetail {

    @Id
    private Integer productId;

    @OneToOne
    @MapsId
    private Product product;

    @Enumerated(EnumType.STRING)
    private RacketLevel level;

    @Enumerated(EnumType.STRING)
    private RacketStyle style;

    @Enumerated(EnumType.STRING)
    private RacketWeight weight;
}
