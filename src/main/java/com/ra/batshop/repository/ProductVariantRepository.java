package com.ra.batshop.repository;

import com.ra.batshop.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {

    List<ProductVariant> findByProduct_Id(Integer productId);
}
