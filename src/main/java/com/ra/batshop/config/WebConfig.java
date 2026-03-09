package com.ra.batshop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. Cấu hình cho thư mục uploads (Chứa ảnh sản phẩm, avatar, file đính kèm)
        // Đường dẫn file thực tế trên máy tính
        Path uploadDir = Paths.get("uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");

        // 2. Cấu hình cho các tài nguyên tĩnh mặc định (css, js, images trong static)
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}