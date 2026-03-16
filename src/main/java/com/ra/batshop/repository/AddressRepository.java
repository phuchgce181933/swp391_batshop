package com.ra.batshop.repository;

import com.ra.batshop.model.Address;
import com.ra.batshop.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    // 1. Dùng cho trang danh sách địa chỉ (Xóa mềm)
    List<Address> findByUserAndIsDeletedFalse(User user);

    // 2. Dùng cho Checkout (Sửa lại đúng tên hàm mà CheckoutController đang gọi)
    // Spring Data JPA sẽ tự hiểu userId là user.id
    Optional<Address> findByUserIdAndIsDefaultTrue(Integer userId);

    // 3. Hàm bổ sung nếu CheckoutController gọi findByUserId
    List<Address> findByUserId(Integer userId);

    // 4. Hàm tìm địa chỉ mặc định nhưng phải chưa bị xóa
    Optional<Address> findByUserAndIsDefaultTrueAndIsDeletedFalse(User user);
}