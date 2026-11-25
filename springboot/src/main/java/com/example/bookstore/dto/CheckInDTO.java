package com.example.bookstore.dto;

import java.time.LocalDate;
import java.util.List;

public class CheckInDTO {

    private Boolean checkedInToday;
    private Integer consecutiveDays;
    private Integer totalPoints;
    private Boolean lotteryDrawn = false;
    private List<LocalDate> checkInDates;

    public CheckInDTO() {}

    public Boolean getCheckedInToday() { return checkedInToday; }
    public void setCheckedInToday(Boolean checkedInToday) { this.checkedInToday = checkedInToday; }

    public Integer getConsecutiveDays() { return consecutiveDays; }
    public void setConsecutiveDays(Integer consecutiveDays) { this.consecutiveDays = consecutiveDays; }

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    public Boolean getLotteryDrawn() { return lotteryDrawn; }
    public void setLotteryDrawn(Boolean lotteryDrawn) { this.lotteryDrawn = lotteryDrawn; }

    public List<LocalDate> getCheckInDates() { return checkInDates; }
    public void setCheckInDates(List<LocalDate> checkInDates) { this.checkInDates = checkInDates; }
}
