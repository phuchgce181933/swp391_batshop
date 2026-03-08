package com.ra.batshop.advice;

import com.ra.batshop.model.Category;
import com.ra.batshop.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class HeaderAdvice {

    @Autowired
    private CategoryRepository categoryRepository;

    // ModelAttribute này đảm bảo biến "categories" luôn có mặt ở mọi view (HTML)
    @ModelAttribute("categories")
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }
}