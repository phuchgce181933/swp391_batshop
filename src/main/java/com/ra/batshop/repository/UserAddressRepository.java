package com.ra.batshop.repository;

import com.ra.batshop.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Integer> {
    List<UserAddress> findByUserId(Integer userId);

    Optional<UserAddress> findByUserIdAndIsDefaultTrue(Integer userId);
}
