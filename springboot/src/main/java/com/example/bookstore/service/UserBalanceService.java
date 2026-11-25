package com.example.bookstore.service;

import com.example.bookstore.entity.UserBalance;
import com.example.bookstore.repository.UserBalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserBalanceService {

    private final UserBalanceRepository userBalanceRepository;

    public UserBalanceService(UserBalanceRepository userBalanceRepository) {
        this.userBalanceRepository = userBalanceRepository;
    }

    @Transactional
    public UserBalance getOrCreateBalance(Long userId) {
        return userBalanceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserBalance newBalance = new UserBalance(userId, new BigDecimal("100.00"));
                    return userBalanceRepository.save(newBalance);
                });
    }

    @Transactional
    public UserBalance recharge(Long userId, BigDecimal amount) {
        UserBalance balance = getOrCreateBalance(userId);
        balance.setBalance(balance.getBalance().add(amount));
        return userBalanceRepository.save(balance);
    }

    @Transactional
    public boolean deduct(Long userId, BigDecimal amount) {
        UserBalance balance = getOrCreateBalance(userId);
        if (balance.getBalance().compareTo(amount) >= 0) {
            balance.setBalance(balance.getBalance().subtract(amount));
            userBalanceRepository.save(balance);
            return true;
        }
        return false;
    }

    public BigDecimal getBalance(Long userId) {
        return getOrCreateBalance(userId).getBalance();
    }
}