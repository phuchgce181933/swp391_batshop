package com.ra.batshop.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ContactReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT")
    private String message; // Nội dung email đã gửi

    private LocalDateTime createdAt; // Thời gian gửi

    // Liên kết ManyToOne: Nhiều lịch sử thuộc về 1 ContactSupport
    @ManyToOne
    @JoinColumn(name = "contact_id")
    private ContactSupport contactSupport;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}