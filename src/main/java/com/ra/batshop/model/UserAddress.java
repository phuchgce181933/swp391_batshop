//package com.ra.batshop.model;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Entity
//@Table(name = "user_address")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//public class UserAddress {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer id;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    private String fullName;
//    private String phone;
//
//    @Column(columnDefinition = "TEXT")
//    private String addressLine;
//
//    private String city;
//    private String district;
//
//    private Boolean isDefault;
//}
