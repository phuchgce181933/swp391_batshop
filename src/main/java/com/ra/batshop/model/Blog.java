package com.ra.batshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;

    private String image;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;
}
