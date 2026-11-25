package com.example.bookstore.controller;

import com.example.bookstore.entity.Banner;
import com.example.bookstore.repository.BannerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    private final BannerRepository bannerRepository;

    public BannerController(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    @GetMapping("/active")
    public ResponseEntity<List<Banner>> getActiveBanners() {
        return ResponseEntity.ok(bannerRepository.findByStatusOrderBySortOrderAsc("ACTIVE"));
    }
}
