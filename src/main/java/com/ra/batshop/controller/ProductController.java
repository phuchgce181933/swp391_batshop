package com.ra.batshop.controller;

import com.ra.batshop.model.Product;
//import com.ra.batshop.repository.BrandRepository;
import com.ra.batshop.model.ProductImage;
import com.ra.batshop.model.ProductVariant;
import com.ra.batshop.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;
    public ProductController(ProductRepository productRepository,
                                  CategoryRepository categoryRepository,
                                  BrandRepository brandRepository,
                             SizeRepository sizeRepository,
                             ColorRepository colorRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.sizeRepository = sizeRepository;
        this.colorRepository = colorRepository;
    }

    // LIST
    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("content", "admin/product/list");
        return "admin/layout";
    }

    // ADD FORM
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("content", "admin/product/add");
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("colors", colorRepository.findAll());
        return "admin/layout";
    }

    // SAVE
    @PostMapping("/add")
    public String save(@ModelAttribute Product product,
                       @RequestParam("imageFile") MultipartFile file,
                       @RequestParam Integer stock,
                       @RequestParam BigDecimal additionalPrice,
                       @RequestParam Integer sizeId,
                       @RequestParam Integer colorId,
                       @RequestParam Long variantBrandId,
                       Model model) {

        if (file == null || file.isEmpty()) {
            model.addAttribute("errorMessage", "Product must have at least 1 image!");
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            model.addAttribute("content", "admin/product/add");
            return "admin/layout";
        }

        try {

            String uploadDir = "uploads/product/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            product.setCreatedAt(LocalDateTime.now());
            product.setStatus(true);

            // tạo img
            ProductImage image = new ProductImage();
            image.setImage(fileName);

            // add vao
            product.addImage(image);
            //add variant
            ProductVariant variant = new ProductVariant();
            variant.setStock(stock);
            variant.setAdditionalPrice(additionalPrice);
            variant.setSize(sizeRepository.findById(sizeId).orElseThrow());
            variant.setColor(colorRepository.findById(colorId).orElseThrow());
            variant.setBrand(brandRepository.findById(variantBrandId).orElseThrow());

            product.addVariant(variant);
            // save
            productRepository.save(product);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/admin/products";
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("product", productRepository.findById(id).orElseThrow());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("colors", colorRepository.findAll());
        model.addAttribute("content", "admin/product/edit");
        return "admin/layout";
    }

    // UPDATE
    @PostMapping("/edit")
    public String update(@ModelAttribute Product product,
                         @RequestParam("imageFile") MultipartFile file) {

        try {

            Product existing =
                    productRepository.findById(product.getId()).orElseThrow();

            // ===== UPDATE BASIC INFO =====
            existing.setName(product.getName());
            existing.setDescription(product.getDescription());
            existing.setPrice(product.getPrice());
            existing.setUpdatedAt(LocalDateTime.now());

            existing.setCategory(
                    categoryRepository
                            .findById(product.getCategory().getId())
                            .orElseThrow()
            );

            existing.setBrand(
                    brandRepository
                            .findById(product.getBrand().getId())
                            .orElseThrow()
            );

            // ===== UPDATE IMAGE =====
            if (file != null && !file.isEmpty()) {

                String uploadDir = "uploads/product/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // XÓA FILE CŨ
                if (!existing.getImages().isEmpty()) {

                    String oldFile =
                            existing.getImages().get(0).getImage();

                    Files.deleteIfExists(uploadPath.resolve(oldFile));

                    existing.getImages().clear();
                }

                String fileName =
                        System.currentTimeMillis() + "_" +
                                file.getOriginalFilename();

                Files.copy(file.getInputStream(),
                        uploadPath.resolve(fileName),
                        StandardCopyOption.REPLACE_EXISTING);

                ProductImage image = new ProductImage();
                image.setImage(fileName);

                existing.addImage(image);
            }

            // ===== UPDATE VARIANTS =====
            for (ProductVariant updated : product.getVariants()) {

                ProductVariant dbVariant =
                        existing.getVariants()
                                .stream()
                                .filter(v -> v.getId().equals(updated.getId()))
                                .findFirst()
                                .orElseThrow();

                dbVariant.setStock(updated.getStock());
                dbVariant.setAdditionalPrice(updated.getAdditionalPrice());

                dbVariant.setSize(
                        sizeRepository
                                .findById(updated.getSize().getId())
                                .orElseThrow()
                );

                dbVariant.setColor(
                        colorRepository
                                .findById(updated.getColor().getId())
                                .orElseThrow()
                );

                dbVariant.setBrand(
                        brandRepository
                                .findById(updated.getBrand().getId())
                                .orElseThrow()
                );
            }

            productRepository.save(existing);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/admin/products";
    }

    // DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        productRepository.deleteById(id);
        return "redirect:/admin/products";
    }
}