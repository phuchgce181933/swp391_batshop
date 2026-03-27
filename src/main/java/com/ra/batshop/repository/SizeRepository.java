package com.ra.batshop.repository;

import com.ra.batshop.model.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SizeRepository extends JpaRepository<Size, Integer> {
    // Tìm size theo tên để check trùng
    Optional<Size> findByName(String name);

}