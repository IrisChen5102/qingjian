package com.qingjian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingjian.dto.DayView;
import com.qingjian.entity.DailySummary;
import com.qingjian.entity.Note;
import com.qingjian.entity.Pomodoro;
import com.qingjian.entity.Timeline;
import com.qingjian.entity.Todo;
import com.qingjian.mapper.DailySummaryMapper;
import com.qingjian.mapper.NoteMapper;
import com.qingjian.mapper.PomodoroMapper;
import com.qingjian.mapper.TimelineMapper;
import com.qingjian.mapper.TodoMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DayService {

    private final TodoMapper todoMapper;
    private final TimelineMapper timelineMapper;
    private final NoteMapper noteMapper;
    private final PomodoroMapper pomodoroMapper;
    private final DailySummaryMapper summaryMapper;

    public DayService(TodoMapper todoMapper, TimelineMapper timelineMapper, NoteMapper noteMapper,
                      PomodoroMapper pomodoroMapper, DailySummaryMapper summaryMapper) {
        this.todoMapper = todoMapper;
        this.timelineMapper = timelineMapper;
        this.noteMapper = noteMapper;
        this.pomodoroMapper = pomodoroMapper;
        this.summaryMapper = summaryMapper;
    }

    public DayView build(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Todo> todos = todoMapper.selectList(new LambdaQueryWrapper<Todo>()
                .eq(Todo::getDueDate, date)
                .orderByAsc(Todo::getStatus)
                .orderByDesc(Todo::getId));

        List<Todo> overdue = todoMapper.selectList(new LambdaQueryWrapper<Todo>()
                .lt(Todo::getDueDate, date)
                .eq(Todo::getStatus, "待办")
                .orderByAsc(Todo::getDueDate));

        List<Timeline> timeline = timelineMapper.selectList(new LambdaQueryWrapper<Timeline>()
                .ge(Timeline::getHappenedAt, start)
                .lt(Timeline::getHappenedAt, end)
                .orderByAsc(Timeline::getHappenedAt));

        List<Note> notes = noteMapper.selectList(new LambdaQueryWrapper<Note>()
                .eq(Note::getLogDate, date)
                .orderByDesc(Note::getUpdatedAt)
                .orderByDesc(Note::getId));

        List<Pomodoro> pomodoros = pomodoroMapper.selectList(new LambdaQueryWrapper<Pomodoro>()
                .ge(Pomodoro::getStartedAt, start)
                .lt(Pomodoro::getStartedAt, end)
                .orderByAsc(Pomodoro::getStartedAt));

        DailySummary summary = summaryMapper.selectById(date);

        return new DayView(date, todos, overdue, timeline, notes, pomodoros, summary);
    }

    public void upsertSummary(LocalDate date, String mood, String tomorrowFocus) {
        DailySummary summary = new DailySummary();
        summary.setLogDate(date);
        summary.setMood(mood);
        summary.setTomorrowFocus(tomorrowFocus);
        summary.setUpdatedAt(LocalDateTime.now());
        if (summaryMapper.selectById(date) == null) {
            summaryMapper.insert(summary);
        } else {
            summaryMapper.updateById(summary);
        }
    }
}
