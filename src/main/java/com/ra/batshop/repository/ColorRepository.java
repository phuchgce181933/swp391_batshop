package com.ra.batshop.repository;

import com.ra.batshop.model.Color;
import com.ra.batshop.model.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import java.util.Optional;

public interface ColorRepository extends CrudRepository<Color, Integer> {
    // Thêm dòng này để tìm màu theo tên
    Optional<Color> findByName(String name);
        }

