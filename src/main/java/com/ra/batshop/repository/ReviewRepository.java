package com.ra.batshop.repository;

import com.ra.batshop.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByProduct_IdOrderByCreatedAtDesc(Integer productId);

}