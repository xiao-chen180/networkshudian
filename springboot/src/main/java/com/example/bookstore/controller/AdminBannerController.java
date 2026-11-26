package com.example.bookstore.controller;

import com.example.bookstore.dto.BannerDTO;
import com.example.bookstore.entity.Banner;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.BannerRepository;
import com.example.bookstore.service.BannerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台运营配置：首页 Banner 的增删改查（/api/admin/** 已在 SecurityConfig 中要求 ADMIN）。
 */
@RestController
@RequestMapping("/api/admin/banners")
public class AdminBannerController {

    private final BannerRepository bannerRepository;
    private final BannerService bannerService;

    public AdminBannerController(BannerRepository bannerRepository, BannerService bannerService) {
        this.bannerRepository = bannerRepository;
        this.bannerService = bannerService;
    }

    @GetMapping
    public ResponseEntity<List<BannerDTO>> list() {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    @PostMapping
    public ResponseEntity<BannerDTO> create(@RequestBody Map<String, Object> body) {
        Banner banner = new Banner();
        applyBody(banner, body);
        Banner saved = bannerRepository.save(banner);
        return ResponseEntity.ok(bannerService.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BannerDTO> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new CustomException("Banner 不存在", HttpStatus.NOT_FOUND.value()));
        applyBody(banner, body);
        return ResponseEntity.ok(bannerService.toDTO(bannerRepository.save(banner)));
    }

    /** 快速切换上下架 */
    @PutMapping("/{id}/status")
    public ResponseEntity<BannerDTO> toggleStatus(@PathVariable Long id) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new CustomException("Banner 不存在", HttpStatus.NOT_FOUND.value()));
        banner.setStatus("ACTIVE".equals(banner.getStatus()) ? "INACTIVE" : "ACTIVE");
        return ResponseEntity.ok(bannerService.toDTO(bannerRepository.save(banner)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new CustomException("Banner 不存在", HttpStatus.NOT_FOUND.value());
        }
        bannerRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private void applyBody(Banner banner, Map<String, Object> body) {
        String title = asString(body.get("title"));
        if (title == null || title.isBlank()) {
            throw new CustomException("标题不能为空", HttpStatus.BAD_REQUEST.value());
        }
        banner.setTitle(title.trim());
        banner.setSubtitle(asString(body.get("subtitle")));
        banner.setImageUrl(asString(body.get("imageUrl")));
        banner.setBookId(asLong(body.get("bookId")));
        banner.setSortOrder(body.get("sortOrder") == null ? 0 : asLong(body.get("sortOrder")).intValue());
        String status = asString(body.get("status"));
        banner.setStatus(status == null || status.isBlank() ? "ACTIVE" : status.toUpperCase());
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value == null || String.valueOf(value).isBlank()) return null;
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}