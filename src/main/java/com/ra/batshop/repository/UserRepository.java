package com.ra.batshop.repository;

import com.ra.batshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    // THÊM DÒNG NÀY VÀO:
    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);
    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String fullName, String email);
    List<User> findByStatus(Boolean status);
    List<User> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndStatus(String fullName, String email, Boolean status);
}