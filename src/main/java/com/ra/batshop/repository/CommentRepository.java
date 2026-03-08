package com.ra.batshop.repository;

import com.ra.batshop.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Integer>{

    List<Comment> findByProduct_IdAndParentIsNullOrderByCreatedAtDesc(Integer productId);

}