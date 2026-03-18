package com.ra.batshop.controller;

import com.ra.batshop.model.*;
import com.ra.batshop.model.Enum.*;
import com.ra.batshop.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SizeRepository sizeRepository;
    private final ColorRepository colorRepository;
    private final ProductVariantImageRepository productVariantImageRepository;
    // 1. KHAI BÁO THÊM REPOSITORY CỦA FLASH SALE
    private final FlashSaleProductRepository flashSaleProductRepository;
    // 2. INJECT VÀO CONSTRUCTOR

    public ProductController(ProductRepository productRepository,
                             CategoryRepository categoryRepository,
                             BrandRepository brandRepository,
                             SizeRepository sizeRepository,
                             ColorRepository colorRepository,
                             FlashSaleProductRepository flashSaleProductRepository,
                             ProductVariantImageRepository productVariantImageRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.sizeRepository = sizeRepository;
        this.colorRepository = colorRepository;
        this.flashSaleProductRepository = flashSaleProductRepository;
        this.productVariantImageRepository = productVariantImageRepository;
    }

    // LIST
    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer categoryId,
                       @RequestParam(required = false) Long brandId) {
        Page<Product> productPage = productRepository.searchProduct(
                keyword, categoryId, brandId,
                PageRequest.of(page, size, Sort.by("id").descending())
        );
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("content", "admin/product/list");
        return "admin/layout";
    }

    // ADD FORM
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("racketDetail", new RacketDetail());
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("content", "admin/product/add");
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("colors", colorRepository.findAll());
        model.addAttribute("racketLevels", RacketLevel.values());
        model.addAttribute("racketLengths", RacketLength.values());
        model.addAttribute("handleLengths", RacketHandleLength.values());
        model.addAttribute("racketStyles", RacketStyle.values());
        model.addAttribute("racketWeights", RacketWeight.values());
        model.addAttribute("equilibriumPoints", EquilibriumPoint.values());
        model.addAttribute("chopstickHardness", ChopstickHardness.values());
        model.addAttribute("content", "admin/product/add");
        return "admin/layout";
    }

    // SAVE
    @PostMapping("/add")
    public String save(@ModelAttribute Product product,
                       @RequestParam("imageFile") MultipartFile file,
                       @RequestParam(value = "variantImages", required = false) List<MultipartFile[]> variantImages,
                       Model model) {

        if (file == null || file.isEmpty()) {
            model.addAttribute("errorMessage", "Product must have at least 1 image!");
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            model.addAttribute("sizes", sizeRepository.findAll());
            model.addAttribute("colors", colorRepository.findAll());
            model.addAttribute("racketLevels", RacketLevel.values());
            model.addAttribute("racketLengths", RacketLength.values());
            model.addAttribute("handleLengths", RacketHandleLength.values());
            model.addAttribute("racketStyles", RacketStyle.values());
            model.addAttribute("racketWeights", RacketWeight.values());
            model.addAttribute("equilibriumPoints", EquilibriumPoint.values());
            model.addAttribute("chopstickHardness", ChopstickHardness.values());
            model.addAttribute("content", "admin/product/add");
            return "admin/layout";
        }

        try {

            // ===== UPLOAD IMAGE =====
            String uploadDir = "uploads/product/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Files.copy(file.getInputStream(),
                    uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);

            product.setCreatedAt(LocalDateTime.now());
            product.setStatus(true);

            // ===== SET CATEGORY =====
            Category category = categoryRepository
                    .findById(product.getCategory().getId())
                    .orElseThrow();

            product.setCategory(category);

            // them anh
            ProductImage image = new ProductImage();
            image.setImage(fileName);
            product.addImage(image);

            // đat variant
            if (product.getVariants() != null) {
                for (int i = 0; i < product.getVariants().size(); i++) {

                    ProductVariant variant = product.getVariants().get(i);

                    variant.setProduct(product);

                    // Nếu có racketDetail thì set quan hệ
                    if (variant.getRacketDetail() != null) {
                        variant.getRacketDetail().setVariant(variant);
                    }

                    // set brand
                    if (variant.getBrand() != null) {
                        variant.setBrand(
                                brandRepository
                                        .findById(variant.getBrand().getId())
                                        .orElseThrow()
                        );
                    }

                    if (variant.getSize() != null && variant.getSize().getId() != null) {
                        variant.setSize(
                                sizeRepository
                                        .findById(variant.getSize().getId())
                                        .orElse(null)
                        );
                    }

                    if (variant.getColor() != null && variant.getColor().getId() != null) {
                        variant.setColor(
                                colorRepository
                                        .findById(variant.getColor().getId())
                                        .orElse(null)
                        );
                    }

                    // ===== UPLOAD VARIANT IMAGE =====
                    if (variantImages != null && variantImages.size() > i) {
                        MultipartFile[] images = variantImages.get(i);

                        for (MultipartFile img : images) {

                            if (!img.isEmpty()) {

                                String variantFileName =
                                        System.currentTimeMillis() + "_" + img.getOriginalFilename();

                                Files.copy(
                                        img.getInputStream(),
                                        uploadPath.resolve(variantFileName),
                                        StandardCopyOption.REPLACE_EXISTING
                                );

                                ProductVariantImage variantImage = new ProductVariantImage();
                                variantImage.setImage(variantFileName);

                                variant.addImage(variantImage);
                            }
                        }
                    }

                }
            }

            // ===== SAVE PRODUCT =====
            productRepository.save(product);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/admin/products";
    }

    // EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {

        Product product = productRepository.findById(id).orElseThrow();

        model.addAttribute("product", product);

        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("sizes", sizeRepository.findAll());
        model.addAttribute("colors", colorRepository.findAll());
        model.addAttribute("racketCategoryId", 3);
        model.addAttribute("racketLevels", RacketLevel.values());
        model.addAttribute("racketLengths", RacketLength.values());
        model.addAttribute("handleLengths", RacketHandleLength.values());
        model.addAttribute("racketStyles", RacketStyle.values());
        model.addAttribute("racketWeights", RacketWeight.values());
        model.addAttribute("equilibriumPoints", EquilibriumPoint.values());
        model.addAttribute("chopstickHardness", ChopstickHardness.values());

        model.addAttribute("content", "admin/product/edit");

        return "admin/layout";
    }

    // UPDATE
    @PostMapping("/edit")
    public String update(@ModelAttribute Product product,
                         @RequestParam("imageFile") MultipartFile file,
                         @RequestParam(value = "variantImages", required = false)
                         List<MultipartFile[]> variantImages) {

        try {

            Product existing =
                    productRepository.findById(product.getId()).orElseThrow();

            // 3. KIỂM TRA XEM GIÁ CÓ BỊ THAY ĐỔI KHÔNG
            boolean isPriceChanged = existing.getPrice().compareTo(product.getPrice()) != 0;

            //  UPDATE BASIC INFO
            existing.setName(product.getName());
            existing.setDescription(product.getDescription());
            existing.setPrice(product.getPrice());
            existing.setUpdatedAt(LocalDateTime.now());
            existing.setStatus(product.getStatus());
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

            //  UPDATE IMAGE
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


            // UPDATE VARIANTS
            if (product.getVariants() != null) {

                for (int i = 0; i < product.getVariants().size(); i++) {

                    ProductVariant updated = product.getVariants().get(i);

                    // =========================
                    // VARIANT MỚI
                    // =========================
                    if (updated.getId() == null) {

                        updated.setProduct(existing);

                        updated.setBrand(
                                brandRepository
                                        .findById(updated.getBrand().getId())
                                        .orElseThrow()
                        );

                        if (updated.getSize() != null && updated.getSize().getId() != null) {
                            updated.setSize(
                                    sizeRepository
                                            .findById(updated.getSize().getId())
                                            .orElse(null)
                            );
                        }

                        if (updated.getColor() != null && updated.getColor().getId() != null) {
                            updated.setColor(
                                    colorRepository
                                            .findById(updated.getColor().getId())
                                            .orElse(null)
                            );
                        }

                        existing.getVariants().add(updated);

                        continue;
                    }

                    // =========================
                    // VARIANT CŨ
                    // =========================
                    ProductVariant dbVariant =
                            existing.getVariants()
                                    .stream()
                                    .filter(v -> v.getId().equals(updated.getId()))
                                    .findFirst()
                                    .orElse(null);

                    if (dbVariant == null) continue;

                    dbVariant.setStock(updated.getStock());
                    dbVariant.setAdditionalPrice(updated.getAdditionalPrice());

                    dbVariant.setBrand(
                            brandRepository
                                    .findById(updated.getBrand().getId())
                                    .orElseThrow()
                    );

                    if (updated.getSize() != null && updated.getSize().getId() != null) {
                        dbVariant.setSize(
                                sizeRepository
                                        .findById(updated.getSize().getId())
                                        .orElse(null)
                        );
                    }

                    if (updated.getColor() != null && updated.getColor().getId() != null) {
                        dbVariant.setColor(
                                colorRepository
                                        .findById(updated.getColor().getId())
                                        .orElse(null)
                        );
                    }

                    // =========================
                    // UPLOAD VARIANT IMAGE
                    // =========================
                    if (variantImages != null && variantImages.size() > i) {

                        MultipartFile[] images = variantImages.get(i);

                        String uploadDir = "uploads/product/";
                        Path uploadPath = Paths.get(uploadDir);

                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }

                        for (MultipartFile img : images) {

                            if (!img.isEmpty()) {

                                String variantFileName =
                                        System.currentTimeMillis() + "_" + img.getOriginalFilename();

                                Files.copy(
                                        img.getInputStream(),
                                        uploadPath.resolve(variantFileName),
                                        StandardCopyOption.REPLACE_EXISTING
                                );

                                ProductVariantImage variantImage = new ProductVariantImage();
                                variantImage.setImage(variantFileName);

                                dbVariant.addImage(variantImage);
                            }
                        }
                    }
                }
            }

            // Lưu sản phẩm
            productRepository.save(existing);

            // 4. LOGIC ĐỒNG BỘ: TỰ ĐỘNG CẬP NHẬT LẠI GIÁ FLASH SALE
            if (isPriceChanged && existing.getFlashSales() != null) {
                for (FlashSaleProduct fsp : existing.getFlashSales()) {
                    FlashSale fs = fsp.getFlashSale();

                    if (fs != null) {
                        // Tính lại giá sale dựa trên giá gốc mới và % giảm của đợt sale đó
                        BigDecimal discount = new BigDecimal(fs.getDiscountPercent()).divide(new BigDecimal(100));
                        BigDecimal discountAmount = existing.getPrice().multiply(discount);
                        BigDecimal newSalePrice = existing.getPrice().subtract(discountAmount);

                        fsp.setSalePrice(newSalePrice);
                        flashSaleProductRepository.save(fsp);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/admin/products";
    }

    // variant trong sản phẩm
    @GetMapping("/{id}/variants")
    public String viewVariants(@PathVariable Integer id, Model model) {

        Product product = productRepository.findById(id).orElseThrow();

        boolean hasSize = product.getVariants()
                .stream()
                .anyMatch(v -> v.getSize() != null);

        boolean hasColor = product.getVariants()
                .stream()
                .anyMatch(v -> v.getColor() != null);

        boolean hasRacketLevel = product.getVariants()
                .stream()
                .anyMatch(v -> v.getRacketDetail() != null && v.getRacketDetail().getLevel() != null);

        boolean hasRacketLength = product.getVariants()
                .stream()
                .anyMatch(v -> v.getRacketDetail() != null && v.getRacketDetail().getLength() != null);

        model.addAttribute("product", product);
        model.addAttribute("variants", product.getVariants());
        model.addAttribute("hasSize", hasSize);
        model.addAttribute("hasColor", hasColor);
        model.addAttribute("hasRacketLevel", hasRacketLevel);
        model.addAttribute("hasRacketLength", hasRacketLength);

        model.addAttribute("content", "admin/product/variant-list");

        return "admin/layout";
    }

    @GetMapping("/delete-variant-image/{id}")
    public String deleteVariantImage(@PathVariable Integer id) {

        ProductVariantImage image =
                productVariantImageRepository.findById(id).orElseThrow();

        Integer productId =
                image.getVariant().getProduct().getId();

        try {

            Path uploadPath = Paths.get("uploads/product/");

            Files.deleteIfExists(uploadPath.resolve(image.getImage()));

        } catch (Exception e) {
            e.printStackTrace();
        }

        productVariantImageRepository.delete(image);

        return "redirect:/admin/products/edit/" + productId;
    }
}