package com.ra.batshop.repository;

import com.ra.batshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
}