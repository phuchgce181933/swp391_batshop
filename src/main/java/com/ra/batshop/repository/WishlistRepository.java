package com.ra.batshop.repository;

import com.ra.batshop.model.Address;
import com.ra.batshop.model.User;
import com.ra.batshop.model.Wishlist;
import com.ra.batshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    // Lấy danh sách yêu thích của 1 User (giống findByUser trong Address)
    List<Wishlist> findByUser(User user);

    // Kiểm tra xem sản phẩm đã có trong wishlist của user chưa để tránh thêm trùng
    Optional<Wishlist> findByUserAndProduct(User user, Product product);
}