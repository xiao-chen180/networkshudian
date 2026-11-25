package com.example.bookstore.service;

import com.example.bookstore.dto.CheckInDTO;
import com.example.bookstore.entity.CheckIn;
import com.example.bookstore.exception.CustomException;
import com.example.bookstore.repository.CheckInRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class CheckInService {

    private final CheckInRepository checkInRepository;
    private final PointsService pointsService;
    private final MessageService messageService;

    public CheckInService(CheckInRepository checkInRepository, PointsService pointsService,
                          MessageService messageService) {
        this.checkInRepository = checkInRepository;
        this.pointsService = pointsService;
        this.messageService = messageService;
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

        // 当天是否已抽奖，供前端决定按钮状态
        checkInRepository.findByUserIdAndCheckInDate(userId, today)
                .ifPresent(ci -> dto.setLotteryDrawn(Boolean.TRUE.equals(ci.getLotteryDrawn())));

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
        checkIn.setLotteryDrawn(false);
        checkInRepository.save(checkIn);

        int points = 10;
        if (consecutiveDays >= 7) points = 15;
        else if (consecutiveDays >= 3) points = 12;
        pointsService.addPoints(userId, points, "每日签到奖励");

        if (consecutiveDays == 7) pointsService.addPoints(userId, 50, "连续签到7天奖励");
        else if (consecutiveDays == 15) pointsService.addPoints(userId, 150, "连续签到15天奖励");
        else if (consecutiveDays == 30) pointsService.addPoints(userId, 500, "连续签到30天奖励");

        // 签到里程碑提醒
        if (consecutiveDays == 7) {
            messageService.sendMessage(userId, "连续签到 7 天",
                    "已连续签到 7 天，今天的抽奖机会已解锁，去签到页抽一张吧。", "SYSTEM");
        } else if (consecutiveDays % 7 == 0) {
            messageService.sendMessage(userId, "连续签到 " + consecutiveDays + " 天",
                    "连续签到满 " + consecutiveDays + " 天，积分奖励已到账。", "SYSTEM");
        }

        return getCheckInStatus(userId);
    }

    /**
     * 签到抽奖：连续签到满 7 天且当天已签到才可抽一次，结果落库（积分入账 + 站内信）。
     * 奖池为积分类奖品，避免虚拟商品与库存复杂度。
     */
    @Transactional
    public Map<String, Object> drawLottery(Long userId) {
        LocalDate today = LocalDate.now();

        CheckIn checkIn = checkInRepository.findByUserIdAndCheckInDate(userId, today)
                .orElseThrow(() -> new CustomException("请先完成今日签到", HttpStatus.BAD_REQUEST.value()));

        int consecutive = checkIn.getConsecutiveDays() != null ? checkIn.getConsecutiveDays() : 0;
        if (consecutive < 7) {
            throw new CustomException("连续签到满 7 天才能抽奖", HttpStatus.BAD_REQUEST.value());
        }
        if (Boolean.TRUE.equals(checkIn.getLotteryDrawn())) {
            throw new CustomException("今天已经抽过奖了", HttpStatus.BAD_REQUEST.value());
        }

        // 奖池：积分（概率递减）
        int[] pool = {10, 10, 20, 20, 50, 50, 100, 200};
        int points = pool[ThreadLocalRandom.current().nextInt(pool.length)];

        pointsService.addPoints(userId, points, "签到抽奖奖励");
        checkIn.setLotteryDrawn(true);
        checkInRepository.save(checkIn);

        messageService.sendMessage(userId, "抽奖结果",
                "恭喜抽中 " + points + " 积分，已存入你的积分账户。", "SYSTEM");

        return Map.of(
                "points", points,
                "name", points + " 积分",
                "consecutiveDays", consecutive
        );
    }
}