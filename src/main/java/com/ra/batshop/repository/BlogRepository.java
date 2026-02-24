package com.ra.batshop.repository;

import com.ra.batshop.model.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Integer> {

    /**
     * Lấy danh sách 4 bài viết mới nhất (sắp xếp giảm dần theo ngày tạo)
     * và có trạng thái là true (đang hiển thị).
     */
    List<Blog> findTop4ByStatusIsTrueOrderByCreatedAtDesc();

}