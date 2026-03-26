package com.ra.batshop.controller;

import com.ra.batshop.model.Banner;
import com.ra.batshop.repository.BannerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/admin/banners")
public class BannerController {

    @ModelAttribute
    public void addActiveMenu(Model model) {
        model.addAttribute("activeMenu", "banners");
    }

    private final BannerRepository bannerRepository;

    private final String UPLOAD_DIR = "uploads/product/";

    public BannerController(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    // LIST
    @GetMapping
    public String listBanners(
            @RequestParam(required = false) String keyword,
            Model model,
            HttpSession session
    ) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        List<Banner> banners;

        if (keyword != null && !keyword.isEmpty()) {
            banners = bannerRepository.findByNameContainingIgnoreCase(keyword);
        } else {
            banners = bannerRepository.findAll();
        }

        model.addAttribute("banners", banners);
        model.addAttribute("keyword", keyword);
        model.addAttribute("content", "admin/banner/list");

        return "admin/layout";
    }

    // ADD FORM
    @GetMapping("/add")
    public String addForm(Model model) {
        Banner banner = new Banner();
        banner.setStatus(true);
        model.addAttribute("banner", banner);
        model.addAttribute("content", "admin/banner/add");
        return "admin/layout";
    }

    // SAVE
    @PostMapping("/add")
    public String save(
            @ModelAttribute Banner banner,
            @RequestParam("file") MultipartFile file,
            Model model
    ) throws IOException {

        if (banner.getName() == null || banner.getName().trim().isEmpty()) {
            model.addAttribute("error", "Banner name cannot be empty");
            model.addAttribute("banner", banner);
            model.addAttribute("content", "admin/banner/add");
            return "admin/layout";
        }

        if (bannerRepository.existsByNameIgnoreCase(banner.getName().trim())) {
            model.addAttribute("error", "Banner name already exists");
            model.addAttribute("banner", banner);
            model.addAttribute("content", "admin/banner/add");
            return "admin/layout";
        }

        if (file.isEmpty()) {
            model.addAttribute("error", "Image is required");
            model.addAttribute("banner", banner);
            model.addAttribute("content", "admin/banner/add");
            return "admin/layout";
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());

        banner.setImage(fileName);
        banner.setName(banner.getName().trim());

        bannerRepository.save(banner);

        return "redirect:/admin/banners";
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Banner banner = bannerRepository.findById(id).orElseThrow();
        model.addAttribute("banner", banner);
        model.addAttribute("content", "admin/banner/edit");
        return "admin/layout";
    }

    // UPDATE
    @PostMapping("/edit")
    public String update(
            @ModelAttribute Banner banner,
            @RequestParam("file") MultipartFile file,
            Model model
    ) throws IOException {

        Banner oldBanner = bannerRepository.findById(banner.getId()).orElseThrow();

        if (banner.getName() == null || banner.getName().trim().isEmpty()) {
            model.addAttribute("error", "Banner name cannot be empty");
            model.addAttribute("banner", oldBanner);
            model.addAttribute("content", "admin/banner/edit");
            return "admin/layout";
        }

        // check duplicate
        List<Banner> all = bannerRepository.findAll();
        for (Banner b : all) {
            if (b.getName().equalsIgnoreCase(banner.getName().trim())
                    && !b.getId().equals(banner.getId())) {
                model.addAttribute("error", "Banner name already exists");
                model.addAttribute("banner", oldBanner);
                model.addAttribute("content", "admin/banner/edit");
                return "admin/layout";
            }
        }

        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            banner.setImage(fileName);
        } else {
            banner.setImage(oldBanner.getImage());
        }

        banner.setName(banner.getName().trim());

        bannerRepository.save(banner);

        return "redirect:/admin/banners";
    }

    // TOGGLE
    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id) {
        Banner banner = bannerRepository.findById(id).orElseThrow();
        banner.setStatus(!banner.getStatus());
        bannerRepository.save(banner);
        return "redirect:/admin/banners";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        bannerRepository.deleteById(id);
        return "redirect:/admin/banners";
    }
}