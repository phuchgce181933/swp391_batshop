package com.ra.batshop.controller;

import com.ra.batshop.model.Blog;
import com.ra.batshop.repository.BlogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/blogs")
public class BlogController {

    private final BlogRepository blogRepository;

    public BlogController(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    // LIST
    @GetMapping
    public String list(Model model) {
        model.addAttribute("blogs", blogRepository.findAll());
        return "admin/blog/list";
    }

    // ADD FORM
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("blog", new Blog());
        return "admin/blog/add";
    }

    // SAVE
    @PostMapping("/add")
    public String save(@ModelAttribute Blog blog) {
        blog.setCreatedAt(LocalDateTime.now());
        blogRepository.save(blog);
        return "redirect:/admin/blogs";
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("blog",
                blogRepository.findById(id).orElseThrow());
        return "admin/blog/edit";
    }

    // UPDATE
    @PostMapping("/edit")
    public String update(@ModelAttribute Blog blog) {
        blogRepository.save(blog);
        return "redirect:/admin/blogs";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        blogRepository.deleteById(id);
        return "redirect:/admin/blogs";
    }
}