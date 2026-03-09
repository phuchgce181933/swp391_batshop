package com.ra.batshop.repository;

import com.ra.batshop.model.User;
import com.ra.batshop.model.Wishlist;
import com.ra.batshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // 1. Lấy tất cả danh sách yêu thích của 1 User
    List<Wishlist> findByUser(User user);

    // 2. Kiểm tra xem sản phẩm đã có trong wishlist chưa
    Optional<Wishlist> findByUserAndProduct(User user, Product product);

    // 3. THÊM MỚI: Tìm kiếm và lọc theo Category ID cho User
    @Query("SELECT w FROM Wishlist w WHERE w.user = :user " +
            "AND (:catId IS NULL OR w.product.category.id = :catId)")
    List<Wishlist> findByUserAndCategoryId(@Param("user") User user, @Param("catId") Integer catId);
}