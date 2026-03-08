package com.ra.batshop.model;

import com.ra.batshop.model.Enum.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 100, unique = true)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Boolean status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String resetCode;
    private LocalDateTime resetCodeExpiredAt;
    private String otp;
    private LocalDateTime otpExpiry;

    // PHẦN SỬA ĐÂY: Chỉ giữ lại 1 danh sách addresses duy nhất liên kết với Address class
    // FetchType.EAGER giúp load dữ liệu ngay lập tức để tránh lỗi Whitelabel 500
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<Address> addresses;
}