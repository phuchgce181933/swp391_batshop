package com.ra.batshop.repository;

import com.ra.batshop.model.Enum.RacketLevel;
import com.ra.batshop.model.Enum.RacketStyle;
import com.ra.batshop.model.Enum.RacketWeight;
import com.ra.batshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Import Param cho code gọn hơn

import java.math.BigDecimal; // Chú ý import BigDecimal
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
    boolean existsByNameIgnoreCase(String name);
    // SEARCH
    Page<Product> findByNameContainingIgnoreCaseAndStatusTrue(
            String keyword,
            Pageable pageable);

    // LẤY SẢN PHẨM BÁN CHẠY NHẤT DỰA TRÊN SỐ LƯỢNG VÀ ĐƠN HÀNG THÀNH CÔNG
    @Query("SELECT p, SUM(oi.quantity) FROM Product p " +
            "JOIN p.variants v " +
            "JOIN OrderItem oi ON oi.productVariant = v " +
            "JOIN oi.order o " +
            "WHERE p.status = true AND o.status = com.ra.batshop.model.Enum.OrderStatus.COMPLETED " +
            "GROUP BY p " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingProducts(Pageable pageable);


    @Query("""
        SELECT p FROM Product p
        WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:brandId IS NULL OR p.brand.id = :brandId)
    """)
    Page<Product> searchProduct(String keyword, Integer categoryId, Long brandId, Pageable pageable);

    // Lọc sản phẩm
    @Query("SELECT p FROM Product p WHERE p.status = true " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandIds IS NULL OR p.brand.id IN :brandIds) " +
            "AND (p.price >= :minPrice AND p.price <= :maxPrice)")
    Page<Product> filterProducts(
            @Param("categoryId") Integer categoryId,
            @Param("brandIds") List<Long> brandIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    // LỌC KẾT HỢP SẮP XẾP BÁN CHẠY NHẤT
    @Query("SELECT p FROM Product p " +
            "LEFT JOIN p.variants v " +
            "LEFT JOIN OrderItem oi ON oi.productVariant = v " +
            "WHERE p.status = true " +
            "AND (:categoryId IS NULL OR p.category.id = :categoryId) " +
            "AND (:brandIds IS NULL OR p.brand.id IN :brandIds) " +
            "AND (p.price >= :minPrice AND p.price <= :maxPrice) " +
            "GROUP BY p " +
            "ORDER BY COALESCE(SUM(oi.quantity), 0) DESC") // COALESCE(..., 0) để xử lý các sản phẩm chưa bán được cái nào (NULL)
    Page<Product> filterAndSortBestSellingProducts(
            @Param("categoryId") Integer categoryId,
            @Param("brandIds") List<Long> brandIds,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query("""
    SELECT DISTINCT p FROM Product p
    LEFT JOIN p.variants v
    LEFT JOIN v.racketDetail rd
    WHERE p.status = true
    AND (:categoryId IS NULL OR p.category.id = :categoryId)
    AND (:brandId IS NULL OR p.brand.id = :brandId)
    AND (:level IS NULL OR rd.level = :level)
    AND (:weight IS NULL OR rd.weight = :weight)
    AND (:style IS NULL OR rd.style = :style)
    AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
""")
    Page<Product> filterAllProducts(
            @Param("categoryId") Integer categoryId,
            @Param("brandId") Integer brandId,
            @Param("level") RacketLevel level,
            @Param("weight") RacketWeight weight,
            @Param("style") RacketStyle style,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}