package com.ra.batshop.controller;

import com.ra.batshop.model.Banner;
import com.ra.batshop.repository.BannerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/admin/banners")
public class BannerController {

    private final BannerRepository bannerRepository;
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    public BannerController(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("banners", bannerRepository.findAll());
        return "admin/banner/list";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("banner", new Banner());
        return "admin/banner/add";
    }

    @PostMapping("/add")
    public String save(@ModelAttribute Banner banner,
                       @RequestParam("file") MultipartFile file) {
        String fileName = handleFileUpload(file);
        if (fileName != null) {
            banner.setImageUrl("/uploads/" + fileName);
        }
        bannerRepository.save(banner);
        return "redirect:/admin/banners";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("banner",
                bannerRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid banner Id:" + id)));
        return "admin/banner/edit";
    }

    @PostMapping("/edit")
    public String update(@ModelAttribute Banner banner,
                         @RequestParam(value = "file", required = false) MultipartFile file) {
        Banner oldBanner = bannerRepository.findById(banner.getId()).orElse(null);
        if (oldBanner != null) {
            if (file != null && !file.isEmpty()) {
                String fileName = handleFileUpload(file);
                banner.setImageUrl("/uploads/" + fileName);
                deleteOldImage(oldBanner.getImageUrl());
            } else {
                banner.setImageUrl(oldBanner.getImageUrl());
            }
        }
        bannerRepository.save(banner);
        return "redirect:/admin/banners";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        Banner banner = bannerRepository.findById(id).orElse(null);
        if (banner != null) {
            deleteOldImage(banner.getImageUrl());
            bannerRepository.delete(banner);
        }
        return "redirect:/admin/banners";
    }

    // Hàm upload
    private String handleFileUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) { e.printStackTrace(); return null; }
    }

    // Hàm xóa ảnh
    private void deleteOldImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return;
        try {
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Files.deleteIfExists(Paths.get(UPLOAD_DIR).resolve(fileName));
        } catch (IOException e) { e.printStackTrace(); }
    }
}