package com.ra.batshop.controller;

import com.ra.batshop.model.Blog;
import com.ra.batshop.repository.BlogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;


@Controller
@RequestMapping("/admin/blogs")
public class BlogController {

    private final BlogRepository blogRepository;

    public BlogController(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    @GetMapping
    public String listBlogs(
            @RequestParam(required = false) String keyword,
            Model model) {

        List<Blog> blogs;

        if (keyword != null && !keyword.isEmpty()) {
            blogs = blogRepository.findByTitleContainingIgnoreCase(keyword);
        } else {
            blogs = blogRepository.findAll();
        }

        model.addAttribute("blogs", blogs);
        model.addAttribute("keyword", keyword);
        model.addAttribute("content", "admin/blog/list");

        return "admin/layout";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("blog", new Blog());
        model.addAttribute("content", "admin/blog/add");
        return "admin/layout";
    }

    @PostMapping("/add")
    public String save(
            @ModelAttribute Blog blog,
            @RequestParam("file") MultipartFile file
    ) {

        blog.setCreatedAt(LocalDateTime.now());

        if (blog.getStatus() == null) {
            blog.setStatus(true);
        }

        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename();

            try {
                Path uploadDir = Paths.get("uploads/product");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                Files.copy(
                        file.getInputStream(),
                        uploadDir.resolve(fileName),
                        StandardCopyOption.REPLACE_EXISTING
                );

                blog.setImage(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        blogRepository.save(blog);
        return "redirect:/admin/blogs";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("blog", blogRepository.findById(id).orElseThrow());
        model.addAttribute("content", "admin/blog/edit");
        return "admin/layout";
    }

    @PostMapping("/edit")
    public String update(
            @ModelAttribute Blog blog,
            @RequestParam("file") MultipartFile file
    ) {

        Blog old = blogRepository.findById(blog.getId()).orElseThrow();

        blog.setCreatedAt(old.getCreatedAt());
        blog.setStatus(old.getStatus());

        if (!file.isEmpty()) {
            String fileName = file.getOriginalFilename();

            try {
                Path uploadDir = Paths.get("uploads/product");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                Files.copy(
                        file.getInputStream(),
                        uploadDir.resolve(fileName),
                        StandardCopyOption.REPLACE_EXISTING
                );

                blog.setImage(fileName);

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            blog.setImage(old.getImage());
        }

        blogRepository.save(blog);
        return "redirect:/admin/blogs";
    }

    @GetMapping("/toggle/{id}")
    public String toggle(@PathVariable Integer id) {
        Blog blog = blogRepository.findById(id).orElseThrow();
        blog.setStatus(!blog.getStatus());
        blogRepository.save(blog);
        return "redirect:/admin/blogs";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        blogRepository.deleteById(id);
        return "redirect:/admin/blogs";
    }
}