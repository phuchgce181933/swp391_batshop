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

    // SỬA ĐỔI QUAN TRỌNG:
    // Đường dẫn này trỏ vào source code để ảnh hiện ngay khi chạy localhost
    // Đảm bảo bạn đã tạo thư mục: src/main/resources/static/uploads/
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
        Banner banner = new Banner();
        banner.setStatus(true); // mặc định active khi thêm mới
        model.addAttribute("banner", banner);
        return "admin/banner/add";
    }

    @PostMapping("/add")
    public String save(
            @ModelAttribute Banner banner,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        if (!file.isEmpty()) {
            // Tạo tên file duy nhất để tránh trùng lặp
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // Tạo đường dẫn file
            Path path = Paths.get(UPLOAD_DIR + fileName);

            // Đảm bảo thư mục tồn tại (nếu chưa có nó sẽ tự tạo)
            Files.createDirectories(path.getParent());

            // Ghi dữ liệu file
            Files.write(path, file.getBytes());

            // Lưu tên file vào database
            banner.setImage(fileName);
        }

        bannerRepository.save(banner);
        return "redirect:/admin/banners";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("banner",
                bannerRepository.findById(id).orElseThrow(() -> new RuntimeException("Banner not found")));
        return "admin/banner/edit";
    }

    @PostMapping("/edit")
    public String update(
            @ModelAttribute Banner banner,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        // Lấy thông tin banner cũ để giữ lại ảnh cũ nếu không upload ảnh mới
        Banner oldBanner = bannerRepository.findById(banner.getId()).orElseThrow(() -> new RuntimeException("Banner not found"));

        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);

            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            banner.setImage(fileName);
        } else {
            // Nếu không chọn file mới, giữ nguyên tên ảnh cũ
            banner.setImage(oldBanner.getImage());
        }

        // Đảm bảo status không bị null (nếu form không gửi status lên)
        if (banner.getStatus() == null) {
            banner.setStatus(oldBanner.getStatus());
        }

        bannerRepository.save(banner);
        return "redirect:/admin/banners";
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(@PathVariable Integer id) {
        Banner banner = bannerRepository.findById(id).orElseThrow(() -> new RuntimeException("Banner not found"));
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