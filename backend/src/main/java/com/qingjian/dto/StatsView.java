package com.qingjian.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** 月度统计 */
public record StatsView(
        List<DayAmount> expenseByDay,
        List<TypeCount> typeCounts,
        List<DayCount> pomodoroByDay,
        int todoTotal,
        int todoDone,
        List<TagCount> topTags
) {
    public record DayAmount(LocalDate date, BigDecimal amount) {
    }

    public record TypeCount(String type, long count) {
    }

    public record DayCount(LocalDate date, long count) {
    }

    public record TagCount(String tag, long count) {
    }
}
