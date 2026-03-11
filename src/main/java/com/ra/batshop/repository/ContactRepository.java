package com.ra.batshop.repository;

import com.ra.batshop.model.ContactSupport;
import com.ra.batshop.model.Enum.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<ContactSupport, Integer> {

    // Truy vấn thông minh: Lọc các trường nếu có dữ liệu truyền vào, nếu rỗng thì lấy tất cả
    @Query("SELECT c FROM ContactSupport c WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR c.name LIKE %:keyword% OR c.phone LIKE %:keyword% OR c.email LIKE %:keyword%) " +
            "AND (:topic IS NULL OR :topic = '' OR c.topic = :topic) " +
            "AND (:status IS NULL OR c.status = :status) " +
            "AND (:filterDate IS NULL OR DATE(c.createdAt) = :filterDate) " +
            "ORDER BY c.createdAt DESC")
    List<ContactSupport> searchAndFilter(
            @Param("keyword") String keyword,
            @Param("topic") String topic,
            @Param("status") ContactStatus status,
            @Param("filterDate") LocalDate filterDate
    );
}