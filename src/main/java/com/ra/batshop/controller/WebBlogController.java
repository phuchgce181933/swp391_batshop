package com.ra.batshop.controller;

import com.ra.batshop.model.Blog;
import com.ra.batshop.repository.BlogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WebBlogController {

    private final BlogRepository blogRepository;

    public WebBlogController(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    @GetMapping("/blog")
    public String blogPage(
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {

        // Phân trang: 12 bài/trang, sắp xếp từ mới nhất đến cũ nhất
        int pageSize = 12;
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());

        Page<Blog> blogPage = blogRepository.searchBlogs(keyword, pageable);

        model.addAttribute("blogPage", blogPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", blogPage.getTotalPages());

        // Thay đổi đường dẫn này tùy thuộc vào cách bạn setup layout cho trang user
        return "user/blog";
    }

    // --- Xử lý trang chi tiết bài viết ---
    @GetMapping("/blog/{id}")
    public String blogDetailPage(@PathVariable Integer id, Model model) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết với id: " + id));

        // Lấy 5 bài viết gợi ý ngẫu nhiên
        List<Blog> suggestedBlogs = blogRepository.findRandom5Blogs(id);

        model.addAttribute("blog", blog);
        model.addAttribute("suggestedBlogs", suggestedBlogs); // Đẩy ra View

        return "user/blog-detail";
    }
}