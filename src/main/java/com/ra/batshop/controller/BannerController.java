package com.ra.batshop.controller;

import com.ra.batshop.model.Banner;
import com.ra.batshop.repository.BannerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/banners")
public class BannerController {

    private final BannerRepository bannerRepository;

    public BannerController(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    // LIST
    @GetMapping
    public String list(Model model) {
        model.addAttribute("banners", bannerRepository.findAll());
        return "admin/banner/list";
    }

    // ADD FORM
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("banner", new Banner());
        return "admin/banner/add";
    }

    // SAVE
    @PostMapping("/add")
    public String save(@ModelAttribute Banner banner) {
        bannerRepository.save(banner);
        return "redirect:/admin/banners";
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("banner",
                bannerRepository.findById(id).orElseThrow());
        return "admin/banner/edit";
    }

    // UPDATE
    @PostMapping("/edit")
    public String update(@ModelAttribute Banner banner) {
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
