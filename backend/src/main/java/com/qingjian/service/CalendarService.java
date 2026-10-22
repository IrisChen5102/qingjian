package com.qingjian.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingjian.dto.CalendarDay;
import com.qingjian.entity.Note;
import com.qingjian.entity.Pomodoro;
import com.qingjian.entity.Timeline;
import com.qingjian.entity.Todo;
import com.qingjian.mapper.NoteMapper;
import com.qingjian.mapper.PomodoroMapper;
import com.qingjian.mapper.TimelineMapper;
import com.qingjian.mapper.TodoMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CalendarService {

    private final TodoMapper todoMapper;
    private final TimelineMapper timelineMapper;
    private final PomodoroMapper pomodoroMapper;
    private final NoteMapper noteMapper;

    public CalendarService(TodoMapper todoMapper, TimelineMapper timelineMapper,
                           PomodoroMapper pomodoroMapper, NoteMapper noteMapper) {
        this.todoMapper = todoMapper;
        this.timelineMapper = timelineMapper;
        this.pomodoroMapper = pomodoroMapper;
        this.noteMapper = noteMapper;
    }

    public List<CalendarDay> build(YearMonth month) {
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
        List<Note> notes = noteMapper.selectList(new LambdaQueryWrapper<Note>()
                .ge(Note::getLogDate, start).lt(Note::getLogDate, end));

        Map<LocalDate, int[]> todoCounts = new HashMap<>();
        for (Todo t : todos) {
            int[] c = todoCounts.computeIfAbsent(t.getDueDate(), k -> new int[2]);
            c[0]++;
            if ("已办".equals(t.getStatus())) {
                c[1]++;
            }
        }

        Map<LocalDate, Integer> timelineCounts = new HashMap<>();
        Map<LocalDate, BigDecimal> expense = new HashMap<>();
        for (Timeline item : timeline) {
            LocalDate d = item.getHappenedAt().toLocalDate();
            timelineCounts.merge(d, 1, Integer::sum);
            if ("记账".equals(item.getType()) && item.getAmount() != null) {
                expense.merge(d, item.getAmount(), BigDecimal::add);
            }
        }

        Map<LocalDate, Integer> pomodoroCounts = new HashMap<>();
        for (Pomodoro p : pomodoros) {
            pomodoroCounts.merge(p.getStartedAt().toLocalDate(), 1, Integer::sum);
        }

        Set<LocalDate> noteDays = new HashSet<>();
        for (Note n : notes) {
            noteDays.add(n.getLogDate());
        }

        List<CalendarDay> result = new ArrayList<>();
        for (LocalDate d = start; d.isBefore(end); d = d.plusDays(1)) {
            int[] c = todoCounts.getOrDefault(d, new int[2]);
            result.add(new CalendarDay(
                    d,
                    c[0],
                    c[1],
                    timelineCounts.getOrDefault(d, 0),
                    pomodoroCounts.getOrDefault(d, 0),
                    expense.get(d),
                    noteDays.contains(d)
            ));
        }
        return result;
    }
}
