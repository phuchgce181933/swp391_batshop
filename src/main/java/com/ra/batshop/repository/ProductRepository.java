package com.ra.batshop.repository;

import com.ra.batshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCategory_Id(Integer categoryId);
    List<Product> findByStatusTrue();

    List<Product> findByCategory_IdAndStatusTrue(Integer categoryId);

    List<Product> findByCategory_IdAndBrand_IdAndStatusTrue(
            Integer categoryId, Integer brandId);

    List<Product> findByBrand_IdAndStatusTrue(Integer brandId);
}