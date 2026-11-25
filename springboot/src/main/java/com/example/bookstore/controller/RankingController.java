package com.example.bookstore.controller;

import com.example.bookstore.dto.BookDTO;
import com.example.bookstore.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rankings")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/sales")
    public ResponseEntity<List<BookDTO>> getSalesRanking() {
        return ResponseEntity.ok(rankingService.getSalesRanking());
    }

    @GetMapping("/rating")
    public ResponseEntity<List<BookDTO>> getRatingRanking() {
        return ResponseEntity.ok(rankingService.getRatingRanking());
    }

    @GetMapping("/new")
    public ResponseEntity<List<BookDTO>> getNewRanking() {
        return ResponseEntity.ok(rankingService.getNewRanking());
    }
}