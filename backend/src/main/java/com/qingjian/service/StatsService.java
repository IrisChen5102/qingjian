package com.qingjian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingjian.dto.StatsView;
import com.qingjian.entity.Pomodoro;
import com.qingjian.entity.Timeline;
import com.qingjian.entity.Todo;
import com.qingjian.mapper.PomodoroMapper;
import com.qingjian.mapper.TimelineMapper;
import com.qingjian.mapper.TodoMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {

    private final TodoMapper todoMapper;
    private final TimelineMapper timelineMapper;
    private final PomodoroMapper pomodoroMapper;

    public StatsService(TodoMapper todoMapper, TimelineMapper timelineMapper, PomodoroMapper pomodoroMapper) {
        this.todoMapper = todoMapper;
        this.timelineMapper = timelineMapper;
        this.pomodoroMapper = pomodoroMapper;
    }

    public StatsView build(YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = start.plusMonths(1);
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atStartOfDay();

        List<Todo> todos = todoMapper.selectList(new LambdaQueryWrapper<Todo>()
                .ge(Todo::getDueDate, start).lt(Todo::getDueDate, end));
        List<Timeline> timeline = timelineMapper.selectList(new LambdaQueryWrapper<Timeline>()
                .ge(Timeline::getHappenedAt, startTime).lt(Timeline::getHappenedAt, endTime));
        List<Pomodoro> pomodoros = pomodoroMapper.selectList(new LambdaQueryWrapper<Pomodoro>()
                .ge(Pomodoro::getStartedAt, startTime).lt(Pomodoro::getStartedAt, endTime));

        int todoTotal = todos.size();
        int todoDone = (int) todos.stream().filter(t -> "已办".equals(t.getStatus())).count();

        Map<LocalDate, BigDecimal> expenseByDay = new HashMap<>();
        Map<String, Long> typeCounts = new HashMap<>();
        Map<String, Long> tagCounts = new HashMap<>();
        for (Timeline item : timeline) {
            LocalDate d = item.getHappenedAt().toLocalDate();
            if ("记账".equals(item.getType()) && item.getAmount() != null) {
                expenseByDay.merge(d, item.getAmount(), BigDecimal::add);
            }
            typeCounts.merge(item.getType(), 1L, Long::sum);
            if (item.getTag() != null && !item.getTag().isBlank()) {
                tagCounts.merge(item.getTag(), 1L, Long::sum);
            }
        }

        Map<LocalDate, Long> pomodoroByDay = new HashMap<>();
        for (Pomodoro p : pomodoros) {
            pomodoroByDay.merge(p.getStartedAt().toLocalDate(), 1L, Long::sum);
        }

        List<StatsView.DayAmount> expenseList = new ArrayList<>();
        expenseByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> expenseList.add(new StatsView.DayAmount(e.getKey(), e.getValue())));

        List<StatsView.TypeCount> typeList = new ArrayList<>();
        typeCounts.forEach((k, v) -> typeList.add(new StatsView.TypeCount(k, v)));

        List<StatsView.DayCount> pomodoroList = new ArrayList<>();
        pomodoroByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> pomodoroList.add(new StatsView.DayCount(e.getKey(), e.getValue())));

        List<StatsView.TagCount> allTags = new ArrayList<>();
        tagCounts.forEach((k, v) -> allTags.add(new StatsView.TagCount(k, v)));
        allTags.sort(Comparator.comparingLong(StatsView.TagCount::count).reversed());
        List<StatsView.TagCount> tagList = allTags.size() > 5 ? allTags.subList(0, 5) : allTags;

        return new StatsView(expenseList, typeList, pomodoroList, todoTotal, todoDone, tagList);
    }
}
