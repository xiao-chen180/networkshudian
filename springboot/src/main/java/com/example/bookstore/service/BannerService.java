package com.example.bookstore.service;

import com.example.bookstore.dto.BannerDTO;
import com.example.bookstore.entity.Banner;
import com.example.bookstore.repository.BannerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService {

    private final BannerRepository bannerRepository;

    public BannerService(BannerRepository bannerRepository) {
        this.bannerRepository = bannerRepository;
    }

    public List<BannerDTO> getActiveBanners() {
        return bannerRepository.findByStatusOrderBySortOrderAsc("ACTIVE").stream()
                .map(this::toDTO)
                .toList();
    }

    private BannerDTO toDTO(Banner banner) {
        return new BannerDTO(
                banner.getId(),
                banner.getTitle(),
                banner.getSubtitle(),
                banner.getBookId(),
                banner.getImageUrl(),
                banner.getSortOrder(),
                banner.getStatus(),
                banner.getCreatedAt()
        );
    }
}