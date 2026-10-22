package com.qingjian.service;

import com.qingjian.dto.ExportData;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataService {

    private final TodoMapper todoMapper;
    private final TimelineMapper timelineMapper;
    private final NoteMapper noteMapper;
    private final PomodoroMapper pomodoroMapper;
    private final DailySummaryMapper summaryMapper;

    public DataService(TodoMapper todoMapper, TimelineMapper timelineMapper, NoteMapper noteMapper,
                       PomodoroMapper pomodoroMapper, DailySummaryMapper summaryMapper) {
        this.todoMapper = todoMapper;
        this.timelineMapper = timelineMapper;
        this.noteMapper = noteMapper;
        this.pomodoroMapper = pomodoroMapper;
        this.summaryMapper = summaryMapper;
    }

    public ExportData exportData() {
        return new ExportData(
                LocalDateTime.now().toString(),
                todoMapper.selectList(null),
                noteMapper.selectList(null),
                timelineMapper.selectList(null),
                pomodoroMapper.selectList(null),
                summaryMapper.selectList(null)
        );
    }

    @Transactional
    public Map<String, Integer> importData(ExportData data) {
        timelineMapper.delete(null);
        todoMapper.delete(null);
        noteMapper.delete(null);
        pomodoroMapper.delete(null);
        summaryMapper.delete(null);

        // 先导入笔记，并记录旧 id → 新 id 的映射，保证时间线的关联不丢
        Map<Integer, Integer> noteIdMap = new HashMap<>();
        if (data.notes() != null) {
            for (Note note : data.notes()) {
                Integer oldId = note.getId();
                note.setId(null);
                noteMapper.insert(note);
                if (oldId != null) {
                    noteIdMap.put(oldId, note.getId());
                }
            }
        }

        int todoCount = 0;
        if (data.todos() != null) {
            for (Todo todo : data.todos()) {
                todo.setId(null);
                todoMapper.insert(todo);
                todoCount++;
            }
        }

        int timelineCount = 0;
        if (data.timeline() != null) {
            for (Timeline item : data.timeline()) {
                item.setId(null);
                if (item.getNoteId() != null) {
                    item.setNoteId(noteIdMap.get(item.getNoteId()));
                }
                timelineMapper.insert(item);
                timelineCount++;
            }
        }

        int pomodoroCount = 0;
        if (data.pomodoros() != null) {
            for (Pomodoro p : data.pomodoros()) {
                p.setId(null);
                pomodoroMapper.insert(p);
                pomodoroCount++;
            }
        }

        int summaryCount = 0;
        if (data.summaries() != null) {
            for (DailySummary s : data.summaries()) {
                summaryMapper.insert(s);
                summaryCount++;
            }
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("todos", todoCount);
        result.put("notes", noteIdMap.size());
        result.put("timeline", timelineCount);
        result.put("pomodoros", pomodoroCount);
        result.put("summaries", summaryCount);
        return result;
    }
}
