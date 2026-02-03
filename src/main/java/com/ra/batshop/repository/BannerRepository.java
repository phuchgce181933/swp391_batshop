package com.ra.batshop.repository;

import com.ra.batshop.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Integer> {
    // Lấy danh sách banner có status = true
    List<Banner> findAllByStatusTrue();
}