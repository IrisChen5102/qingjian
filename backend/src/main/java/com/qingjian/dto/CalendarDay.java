package com.qingjian.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 日历中每天的聚合标记 */
public record CalendarDay(
        LocalDate date,
        int todoTotal,
        int todoDone,
        int timelineCount,
        int pomodoroCount,
        BigDecimal expense,
        boolean hasNote
) {
}
