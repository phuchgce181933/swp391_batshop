package com.ra.batshop.controller;

import com.ra.batshop.model.*;
import com.ra.batshop.model.Enum.*;
import com.ra.batshop.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/products")
public class ProductController {

    @ModelAttribute
    public void addActiveMenu(Model model) {
        model.addAttribute("activeMenu", "products");
    }

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
                       @RequestParam(required = false) Long brandId,
                       HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
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
                       MultipartHttpServletRequest request,
                       Model model) {

        if (file == null || file.isEmpty()) {

            model.addAttribute("errorMessage", "Product must have at least 1 image!");
            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            model.addAttribute("sizes", sizeRepository.findAll());
            model.addAttribute("colors", colorRepository.findAll());
            model.addAttribute("content", "admin/product/add");
            return "admin/layout";
        }
        if (product.getPrice() == null ||
                product.getPrice().compareTo(BigDecimal.ZERO) < 0) {

            model.addAttribute("errorMessage", "Giá sản phẩm phải lớn hơn 0!");

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
        if (product.getVariants() != null) {
            for (ProductVariant v : product.getVariants()) {

                if (v.getStock() == null || v.getStock() < 0) {
                    model.addAttribute("errorMessage", "Số lượng phải lớn hơn 0!");

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

                if (v.getAdditionalPrice() == null ||
                        v.getAdditionalPrice().compareTo(BigDecimal.ZERO) < 0) {

                    model.addAttribute("errorMessage", "Giá biến thể phải lớn hơn 0!");

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
            }
        }
        // CHECK TRÙNG TÊN
        if (productRepository.existsByNameIgnoreCase(product.getName().trim())) {

            model.addAttribute("errorMessage", "Product name already exists!");

            model.addAttribute("categories", categoryRepository.findAll());
            model.addAttribute("brands", brandRepository.findAll());
            model.addAttribute("sizes", sizeRepository.findAll());
            model.addAttribute("colors", colorRepository.findAll());
            model.addAttribute("content", "admin/product/add");

            return "admin/layout";
        }
        try {

            String uploadDir = "uploads/product/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // ===== LẤY FILE VARIANT =====
            Map<String, List<MultipartFile>> variantImages = request.getMultiFileMap();

            System.out.println("ALL FILE KEYS: " + variantImages.keySet());

            // ===== IMAGE PRODUCT =====
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Files.copy(file.getInputStream(),
                    uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING);

            product.setCreatedAt(LocalDateTime.now());
            product.setStatus(true);
            //productRepository.save(product);

            Category category = categoryRepository
                    .findById(product.getCategory().getId())
                    .orElseThrow();

            product.setCategory(category);

            ProductImage image = new ProductImage();
            image.setImage(fileName);
            product.addImage(image);

            // ===== VARIANTS =====
            if (product.getVariants() != null) {

                for (int i = 0; i < product.getVariants().size(); i++) {

                    ProductVariant variant = product.getVariants().get(i);
                    variant.setProduct(product);
                    // ===== FIX RACKET =====
                    if (product.getCategory().getId() == 3) {

                        if (variant.getRacketDetail() == null) {
                            variant.setRacketDetail(new RacketDetail());
                        }

                        variant.getRacketDetail().setVariant(variant);
                    }
                    // ===== SET BRAND =====
                    if (variant.getBrand() != null && variant.getBrand().getId() != null) {
                        variant.setBrand(
                                brandRepository.findById(variant.getBrand().getId()).orElse(null)
                        );
                    }

                    // ===== SET SIZE =====
                    if (variant.getSize() != null && variant.getSize().getId() != null && variant.getSize().getId() != 0) {
                        variant.setSize(sizeRepository.findById(variant.getSize().getId()).orElse(null));
                    } else {
                        variant.setSize(null); // gán null nếu không chọn
                    }

                    if (variant.getColor() != null && variant.getColor().getId() != null && variant.getColor().getId() != 0) {
                        variant.setColor(colorRepository.findById(variant.getColor().getId()).orElse(null));
                    } else {
                        variant.setColor(null);
                    }

                    // ===== UPLOAD VARIANT IMAGES =====
                    String key = "variantImages[" + i + "]";
                    List<MultipartFile> images = variantImages.get(key);

                    System.out.println("Checking key: " + key);

                    if (images != null && !images.isEmpty()) {

                        System.out.println("Found " + images.size() + " images for variant " + i);

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

                    } else {
                        System.out.println("NO IMAGE for variant " + i);
                    }
                }
            }
            if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                model.addAttribute("errorMessage", "Gi!");
                return "admin/layout";
            }


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
        for (ProductVariant variant : product.getVariants()) {
            if (product.getCategory().getId() == 3) {
                if (variant.getRacketDetail() == null) {
                    RacketDetail detail = new RacketDetail();
                    detail.setVariant(variant);
                    variant.setRacketDetail(detail);
                }
            }
        }
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
                         MultipartHttpServletRequest request) {

        try {
            Product existing = productRepository.findById(product.getId()).orElseThrow();

            // Cập nhật thông tin cơ bản
            existing.setName(product.getName());
            existing.setDescription(product.getDescription());
            existing.setPrice(product.getPrice());
            existing.setUpdatedAt(LocalDateTime.now());
            existing.setStatus(product.getStatus());
            existing.setCategory(categoryRepository.findById(product.getCategory().getId()).orElseThrow());
            existing.setBrand(brandRepository.findById(product.getBrand().getId()).orElseThrow());

            String uploadDir = "uploads/product/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            if (file != null && !file.isEmpty()) {

                // xóa ảnh cũ nếu có
                if (existing.getImages() != null && !existing.getImages().isEmpty()) {
                    String oldImage = existing.getImages().get(0).getImage();

                    try {
                        Files.deleteIfExists(uploadPath.resolve(oldImage));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    existing.getImages().clear();
                }

                // lưu ảnh mới
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

                Files.copy(file.getInputStream(),
                        uploadPath.resolve(fileName),
                        StandardCopyOption.REPLACE_EXISTING);

                ProductImage newImage = new ProductImage();
                newImage.setImage(fileName);

                existing.addImage(newImage);
            }
            Map<String, List<MultipartFile>> variantImages = request.getMultiFileMap();

            // Xử lý tất cả biến thể
            if (product.getVariants() != null) {
                for (int i = 0; i < product.getVariants().size(); i++) {
                    ProductVariant updated = product.getVariants().get(i);

                    if (updated.getId() != null) {
                        // Biến thể cũ → update
                        ProductVariant dbVariant = existing.getVariants()
                                .stream()
                                .filter(v -> v.getId().equals(updated.getId()))
                                .findFirst()
                                .orElse(null);

                        if (dbVariant != null) {
                            dbVariant.setStock(updated.getStock());
                            dbVariant.setAdditionalPrice(updated.getAdditionalPrice());
                            // ===== UPDATE RACKET DETAIL =====
                            if (product.getCategory().getId() == 3) {

                                if (dbVariant.getRacketDetail() == null) {
                                    RacketDetail detail = new RacketDetail();
                                    detail.setProduct(product);
                                    detail.setVariant(dbVariant);
                                    dbVariant.setRacketDetail(detail);
                                }

                                RacketDetail updatedDetail = updated.getRacketDetail();

                                if (updatedDetail != null) {
                                    dbVariant.getRacketDetail().setLevel(updatedDetail.getLevel());
                                    dbVariant.getRacketDetail().setLength(updatedDetail.getLength());
                                    dbVariant.getRacketDetail().setRacketHandleLength(updatedDetail.getRacketHandleLength());
                                    dbVariant.getRacketDetail().setStyle(updatedDetail.getStyle());
                                    dbVariant.getRacketDetail().setWeight(updatedDetail.getWeight());
                                    dbVariant.getRacketDetail().setTechnology(updatedDetail.getTechnology());
                                    dbVariant.getRacketDetail().setGamecontent(updatedDetail.getGamecontent());
                                    dbVariant.getRacketDetail().setSwingWeight(updatedDetail.getSwingWeight());
                                    dbVariant.getRacketDetail().setEquilibriumPoint(updatedDetail.getEquilibriumPoint());
                                    dbVariant.getRacketDetail().setChopstickHardness(updatedDetail.getChopstickHardness());
                                }
                            }
                            // Cập nhật brand, size, color
                            if (updated.getBrand() != null && updated.getBrand().getId() != null) {
                                dbVariant.setBrand(brandRepository.findById(updated.getBrand().getId()).orElse(null));
                            }
                            if (updated.getSize() != null && updated.getSize().getId() != null) {
                                dbVariant.setSize(sizeRepository.findById(updated.getSize().getId()).orElse(null));
                            }
                            if (updated.getColor() != null && updated.getColor().getId() != null) {
                                dbVariant.setColor(colorRepository.findById(updated.getColor().getId()).orElse(null));
                            }

                            // Upload ảnh
                            String key = "variantImages[" + i + "]";
                            List<MultipartFile> images = variantImages.get(key);
                            if (images != null && !images.isEmpty()) {
                                for (MultipartFile img : images) {
                                    if (!img.isEmpty()) {
                                        String fileName = System.currentTimeMillis() + "_" + img.getOriginalFilename();
                                        Files.copy(img.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

                                        ProductVariantImage variantImage = new ProductVariantImage();
                                        variantImage.setImage(fileName);
                                        dbVariant.addImage(variantImage);
                                    }
                                }
                            }
                        }

                    } else {
                        // Biến thể mới → thêm vào
                        updated.setProduct(existing);

                        if (product.getCategory().getId() == 3) {
                            // Nếu là racket, tạo RacketDetail
                            if (updated.getRacketDetail() == null) updated.setRacketDetail(new RacketDetail());
                            updated.getRacketDetail().setVariant(updated);
                        }

                        if (updated.getBrand() != null && updated.getBrand().getId() != null)
                            updated.setBrand(brandRepository.findById(updated.getBrand().getId()).orElse(null));
                        if (updated.getSize() != null && updated.getSize().getId() != null)
                            updated.setSize(sizeRepository.findById(updated.getSize().getId()).orElse(null));
                        if (updated.getColor() != null && updated.getColor().getId() != null)
                            updated.setColor(colorRepository.findById(updated.getColor().getId()).orElse(null));

                        // Upload ảnh biến thể mới
                        String key = "variantImages[" + i + "]";
                        List<MultipartFile> images = variantImages.get(key);
                        if (images != null && !images.isEmpty()) {
                            for (MultipartFile img : images) {
                                if (!img.isEmpty()) {
                                    String fileName = System.currentTimeMillis() + "_" + img.getOriginalFilename();
                                    Files.copy(img.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

                                    ProductVariantImage variantImage = new ProductVariantImage();
                                    variantImage.setImage(fileName);
                                    updated.addImage(variantImage);
                                }
                            }
                        }

                        existing.addVariant(updated);
                    }
                }
            }

            productRepository.save(existing);

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