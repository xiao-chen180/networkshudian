package com.example.bookstore.service;

import com.example.bookstore.dto.CheckInDTO;
import com.example.bookstore.entity.CheckIn;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.CheckInRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final PointsService pointsService;

    public CheckInService(CheckInRepository checkInRepository, PointsService pointsService) {
        this.checkInRepository = checkInRepository;
        this.pointsService = pointsService;
    }

    public CheckInDTO getCheckInStatus(Long userId) {
        CheckInDTO dto = new CheckInDTO();
        LocalDate today = LocalDate.now();

        dto.setCheckedInToday(checkInRepository.existsByUserIdAndCheckInDate(userId, today));

        checkInRepository.findTopByUserIdOrderByCheckInDateDesc(userId).ifPresent(lastCheckIn -> {
            dto.setConsecutiveDays(lastCheckIn.getConsecutiveDays());
        });

        if (dto.getConsecutiveDays() == null) {
            dto.setConsecutiveDays(0);
        }

        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        List<CheckIn> monthCheckIns = checkInRepository.findByUserIdAndCheckInDateBetweenOrderByCheckInDateDesc(
                userId, startOfMonth, endOfMonth);
        dto.setCheckInDates(monthCheckIns.stream()
                .map(CheckIn::getCheckInDate)
                .collect(Collectors.toList()));

        dto.setTotalPoints(pointsService.getUserPoints(userId));

        return dto;
    }

    public CheckInDTO checkIn(Long userId) {
        LocalDate today = LocalDate.now();

        if (checkInRepository.existsByUserIdAndCheckInDate(userId, today)) {
            throw new CustomException("今天已经签到过了", HttpStatus.BAD_REQUEST.value());
        }

        int consecutiveDays = 1;
        LocalDate yesterday = today.minusDays(1);
        CheckIn lastCheckIn = checkInRepository.findByUserIdAndCheckInDate(userId, yesterday).orElse(null);

        if (lastCheckIn != null) {
            consecutiveDays = lastCheckIn.getConsecutiveDays() + 1;
        }

        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(userId);
        checkIn.setCheckInDate(today);
        checkIn.setConsecutiveDays(consecutiveDays);
        checkInRepository.save(checkIn);

        int points = 10;
        if (consecutiveDays >= 7) points = 15;
        if (consecutiveDays >= 3) points = 12;
        pointsService.addPoints(userId, points, "每日签到奖励");

        if (consecutiveDays == 7) pointsService.addPoints(userId, 50, "连续签到7天奖励");
        else if (consecutiveDays == 15) pointsService.addPoints(userId, 150, "连续签到15天奖励");
        else if (consecutiveDays == 30) pointsService.addPoints(userId, 500, "连续签到30天奖励");

        return getCheckInStatus(userId);
    }
}
