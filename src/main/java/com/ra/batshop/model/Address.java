package com.ra.batshop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "addresses") // Nếu vẫn đỏ, con có thể tạm thời xóa dòng này hoặc cấu hình Database như trên
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String city;
    private String district;
    private String ward;
    private String detail;

    private boolean isDefault;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}