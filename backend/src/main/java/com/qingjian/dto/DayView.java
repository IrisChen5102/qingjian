package com.qingjian.dto;

import com.qingjian.entity.DailySummary;
import com.qingjian.entity.Note;
import com.qingjian.entity.Pomodoro;
import com.qingjian.entity.Timeline;
import com.qingjian.entity.Todo;

import java.time.LocalDate;
import java.util.List;

/** 某一天的全部数据（今日页一次请求拿全） */
public record DayView(
        LocalDate date,
        List<Todo> todos,
        List<Todo> overdue,
        List<Timeline> timeline,
        List<Note> notes,
        List<Pomodoro> pomodoros,
        DailySummary summary
) {
}
