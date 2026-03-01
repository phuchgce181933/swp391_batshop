package com.ra.batshop.repository;

import com.ra.batshop.model.Address;
import com.ra.batshop.model.User;
import com.ra.batshop.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
    Optional<Address> findByUserIdAndIsDefaultTrue(Integer  userId);
    List<Address> findByUserId(Integer  userId);
}