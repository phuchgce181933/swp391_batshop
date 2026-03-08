package com.ra.batshop.repository;

import com.ra.batshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // LIST
    List<Product> findByCategory_Id(Integer categoryId);
    List<Product> findByStatusTrue();
    List<Product> findByCategory_IdAndStatusTrue(Integer categoryId);
    List<Product> findByCategory_IdAndBrand_IdAndStatusTrue(Integer categoryId, Integer brandId);
    List<Product> findByBrand_IdAndStatusTrue(Integer brandId);

    // PAGEABLE
    Page<Product> findByStatusTrue(Pageable pageable);
    Page<Product> findByCategory_IdAndStatusTrue(Integer categoryId, Pageable pageable);
    Page<Product> findByBrand_IdAndStatusTrue(Integer brandId, Pageable pageable);
    Page<Product> findByCategory_IdAndBrand_IdAndStatusTrue(
            Integer categoryId,
            Integer brandId,
            Pageable pageable);

    // SEARCH
    Page<Product> findByNameContainingIgnoreCaseAndStatusTrue(
            String keyword,
            Pageable pageable);

    // LẤY SẢN PHẨM BÁN CHẠY NHẤT DỰA TRÊN SỐ LƯỢNG (ORDER ITEM)
    @Query("SELECT p FROM Product p " +
            "JOIN p.variants v " +
            "JOIN OrderItem oi ON oi.productVariant = v " +
            "WHERE p.status = true " +
            "GROUP BY p " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Product> findTopSellingProducts(Pageable pageable);

    @Query("""
        SELECT p FROM Product p
        WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:brandId IS NULL OR p.brand.id = :brandId)
    """)
    Page<Product> searchProduct(String keyword, Integer categoryId, Long brandId, Pageable pageable);
}