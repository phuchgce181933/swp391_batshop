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

@Controller
@RequestMapping("/admin/banners")
public class BannerController {

    private final BannerRepository bannerRepository;

    private final String UPLOAD_DIR = "uploads/product/";

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
        Banner banner = new Banner();
        banner.setStatus(true); // mặc định active
        model.addAttribute("banner", banner);
        return "admin/banner/add";
    }

    @PostMapping("/add")
    public String save(
            @ModelAttribute Banner banner,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            banner.setImage(fileName);
        }

        bannerRepository.save(banner);
        return "redirect:/admin/banners";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("banner",
                bannerRepository.findById(id).orElseThrow());
        return "admin/banner/edit";
    }

    @PostMapping("/edit")
    public String update(
            @ModelAttribute Banner banner,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        Banner oldBanner = bannerRepository.findById(banner.getId()).orElseThrow();

        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            banner.setImage(fileName);
        } else {
            banner.setImage(oldBanner.getImage());
        }

        bannerRepository.save(banner);
        return "redirect:/admin/banners";
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id) {
        Banner banner = bannerRepository.findById(id).orElseThrow();
        banner.setStatus(!banner.getStatus());
        bannerRepository.save(banner);
        return "redirect:/admin/banners";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        bannerRepository.deleteById(id);
        return "redirect:/admin/banners";
    }
}
