package com.example.bookstore.repository;

import com.example.bookstore.entity.AfterSale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AfterSaleRepository extends JpaRepository<AfterSale, Long> {

    List<AfterSale> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AfterSale> findAllByOrderByCreatedAtDesc();

    List<AfterSale> findByOrderIdAndStatus(Long orderId, String status);

    long countByStatus(String status);
}