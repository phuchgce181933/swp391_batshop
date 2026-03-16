package com.ra.batshop.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "colors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Color {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Giữ Integer để khớp với Repository của ông

    private String name;

    // Thêm trường này để giao diện không bị lỗi khi check status
    private Boolean status = true;
}