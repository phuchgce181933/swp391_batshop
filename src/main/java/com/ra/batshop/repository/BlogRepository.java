package com.ra.batshop.repository;

import com.ra.batshop.model.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Integer> {

    /**
     * Lấy danh sách 4 bài viết mới nhất (sắp xếp giảm dần theo ngày tạo)
     * và có trạng thái là true (đang hiển thị).
     * (Dùng cho trang chủ hoặc phần tin tức nổi bật)
     */
    List<Blog> findTop4ByStatusIsTrueOrderByCreatedAtDesc();

    /**
     * Lấy các blog đang active (status = true), tìm kiếm theo từ khóa (tiêu đề) và phân trang.
     * (Dùng cho trang danh sách tất cả blog)
     */
    @Query("SELECT b FROM Blog b WHERE b.status = true AND (:keyword IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Blog> searchBlogs(@Param("keyword") String keyword, Pageable pageable);

    // "Có thể bạn sẽ thích" Lấy ngẫu nhiên 5 bài viết đang hiển thị (trừ bài đang xem)
    @Query(value = "SELECT * FROM blog WHERE status = true AND id != :currentId ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<Blog> findRandom5Blogs(@Param("currentId") Integer currentId);

}