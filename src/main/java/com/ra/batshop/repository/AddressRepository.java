package com.ra.batshop.repository;

import com.ra.batshop.model.Address;
import com.ra.batshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // Hàm này để Controller gọi list địa chỉ
    List<Address> findByUser(User user);

    // Dùng cho Checkout
    List<Address> findByUserId(Integer userId);

    // Dùng cho Checkout
    Optional<Address> findByUserIdAndIsDefaultTrue(Integer userId);

    // Câu lệnh SQL thuần để xóa vĩnh viễn
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM addresses WHERE id = ?1", nativeQuery = true)
    void hardDeleteAddressById(Long id);
}