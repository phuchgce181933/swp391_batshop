package com.ra.batshop.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(
        name = "category",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "name")
        }
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Tên category không được để trống")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Ảnh category không được để trống")
    @Column(nullable = false)
    private String image;

    @OneToMany(mappedBy = "category")
    private List<Product> products;

    @PrePersist
    @PreUpdate
    public void formatData() {
        if (name != null) {
            name = name.trim();
        }
    }
}
