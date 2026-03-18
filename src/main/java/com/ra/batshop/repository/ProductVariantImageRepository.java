package com.ra.batshop.repository;

import com.ra.batshop.model.Product;
import com.ra.batshop.model.ProductVariantImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductVariantImageRepository extends JpaRepository<ProductVariantImage, Integer> {
}
