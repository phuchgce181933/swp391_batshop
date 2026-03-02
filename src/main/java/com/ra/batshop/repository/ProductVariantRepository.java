package com.ra.batshop.repository;

import com.ra.batshop.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    List<ProductVariant> findByProduct_Id(Integer productId);
    List<ProductVariant> findByProduct_Category_Id(Integer categoryId);
    List<ProductVariant> findByProduct_StatusTrue();
    List<ProductVariant> findByProduct_Category_IdAndProduct_StatusTrue(Integer categoryId);
}
