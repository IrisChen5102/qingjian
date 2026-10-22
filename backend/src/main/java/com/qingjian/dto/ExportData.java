package com.qingjian.dto;

import com.qingjian.entity.DailySummary;
import com.qingjian.entity.Note;
import com.qingjian.entity.Pomodoro;
import com.qingjian.entity.Timeline;
import com.qingjian.entity.Todo;

import java.util.List;

/** 整库导入导出结构 */
public record ExportData(
        String exportedAt,
        List<Todo> todos,
        List<Note> notes,
        List<Timeline> timeline,
        List<Pomodoro> pomodoros,
        List<DailySummary> summaries
) {
}
