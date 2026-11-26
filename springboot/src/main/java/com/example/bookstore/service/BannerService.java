package com.example.bookstore.service;

import com.example.bookstore.dto.BannerDTO;
import com.example.bookstore.entity.Banner;
import com.example.bookstore.repository.BannerRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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

    /** 后台列表：全部 Banner，按排序号与创建时间倒序 */
    public List<BannerDTO> getAllBanners() {
        return bannerRepository.findAll().stream()
                .sorted(Comparator.comparing((Banner b) -> b.getSortOrder() == null ? 0 : b.getSortOrder())
                        .thenComparing(b -> b.getCreatedAt() == null ? java.time.LocalDateTime.MIN : b.getCreatedAt(),
                                Comparator.reverseOrder()))
                .map(this::toDTO)
                .toList();
    }

    public BannerDTO toDTO(Banner banner) {
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