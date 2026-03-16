package com.ra.batshop.repository;

import com.ra.batshop.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByProduct_IdOrderByCreatedAtDesc(Integer productId);


    Page<Review> findByProduct_IdAndParentIsNull(
            Integer productId,
            Pageable pageable
    );

    List<Review> findByParentIsNull();

    @Query("""
           SELECT AVG(r.rating)
           FROM Review r
           WHERE r.product.id = :productId
           AND r.parent IS NULL
           """)
    Double getAverageRating(Integer productId);
    Page<Review> findByParentIsNull(Pageable pageable);
    Long countByProduct_Id(Integer productId);
    boolean existsByUser_IdAndProduct_Id(Integer userId, Integer productId);
}