package com.ra.batshop.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sizes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Size {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Đổi lại thành Integer ở đây

    private String name;

    @Column(columnDefinition = "boolean default true")
    private Boolean status = true;
}