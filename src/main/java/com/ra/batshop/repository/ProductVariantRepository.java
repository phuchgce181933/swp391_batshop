package com.ra.batshop.repository;

import com.ra.batshop.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ra.batshop.model.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    List<ProductVariant> findByProduct_Id(Integer productId);
    List<ProductVariant> findByProduct_Category_Id(Integer categoryId);
    List<ProductVariant> findByProduct_StatusTrue();
    List<ProductVariant> findByProduct_Category_IdAndProduct_StatusTrue(Integer categoryId);
    Page<ProductVariant> findByProduct_StatusTrue(Pageable pageable);

    Page<ProductVariant> findByProduct_Category_IdAndProduct_StatusTrue(
            Integer categoryId,
            Pageable pageable);

    Page<ProductVariant> findByBrand_IdAndProduct_StatusTrue(
            Integer brandId,
            Pageable pageable);

    Page<ProductVariant> findByProduct_Category_IdAndBrand_IdAndProduct_StatusTrue(
            Integer categoryId,
            Integer brandId,
            Pageable pageable);

    Page<ProductVariant> findByProduct_NameContainingIgnoreCaseAndProduct_StatusTrue(
            String keyword,
            Pageable pageable);
}
