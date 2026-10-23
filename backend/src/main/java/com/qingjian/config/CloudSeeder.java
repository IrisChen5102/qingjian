package com.qingjian.config;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 云端演示环境的演示数据（相对当天生成，保证打开就有内容）。
 * 本地开发使用 SQL Server 的种子脚本，不走这里。
 */
@Configuration
@Profile("cloud")
public class CloudSeeder implements CommandLineRunner {

    private final TodoMapper todoMapper;
    private final NoteMapper noteMapper;
    private final TimelineMapper timelineMapper;
    private final PomodoroMapper pomodoroMapper;
    private final DailySummaryMapper summaryMapper;

    public CloudSeeder(TodoMapper todoMapper, NoteMapper noteMapper, TimelineMapper timelineMapper,
                       PomodoroMapper pomodoroMapper, DailySummaryMapper summaryMapper) {
        this.todoMapper = todoMapper;
        this.noteMapper = noteMapper;
        this.timelineMapper = timelineMapper;
        this.pomodoroMapper = pomodoroMapper;
        this.summaryMapper = summaryMapper;
    }

    @Override
    public void run(String... args) {
        if (todoMapper.selectCount(null) > 0) {
            return;
        }
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        Note diary = note(today, "日记", "实习最后一天", "今天把项目验收和演示视频都完成了。晚上整理材料、发邮件，实习正式收官。");
        Note misc = note(today, "杂记", "演示复盘", "视频演示时注意：先展示部署平台与域名，再按模块完整走一遍功能。");
        note(yesterday, "日记", "项目收尾", "把番茄计时改成全局状态，切换页面不再中断。");

        todo("写实习报告", "高", today, "待办", "先写结构再填内容", null);
        todo("录大作业演示视频", "高", today, "待办", "先展示部署平台和域名", null);
        todo("背 30 个单词", "中", today, "已办", "", LocalDateTime.now());
        todo("跑步 3 公里", "低", today, "待办", "", null);
        todo("整理简历版本", "中", yesterday.minusDays(1), "待办", "示例：顺延区", null);

        timeline("饮食", "早餐", "食堂：豆浆和包子", null, new BigDecimal("6.50"), null, today.atTime(8, 10), null);
        timeline("娱乐", "听歌", "写代码时的歌单", "https://music.163.com/", null, "网易云 · 周杰伦", today.atTime(10, 0), null);
        timeline("随手记", "灵感：时间轴左滑删除", "记录在轴上的条目可以快速删除。", null, null, null, today.atTime(11, 5), null);
        timeline("饮食", "午饭", "和室友吃麻辣烫", null, new BigDecimal("24.00"), null, today.atTime(12, 15), null);
        timeline("娱乐", "看 B 站", "刷了两集动画", "https://www.bilibili.com/", null, "B站 · 新番", today.atTime(13, 30), null);
        timeline("记账", "奶茶", "下午提神", null, new BigDecimal("15.00"), null, today.atTime(15, 20), null);
        timeline("随手记", "面试复盘已整理", "详见关联笔记", null, null, null, today.atTime(16, 40), misc.getId());
        timeline("饮食", "晚饭", "宿舍楼下小馆", null, new BigDecimal("18.50"), null, today.atTime(18, 30), null);
        timeline("娱乐", "打游戏", "两把排位，一胜一负", null, null, "Minecraft", yesterday.atTime(21, 30), null);

        pomodoro(25, today.atTime(9, 0), today.atTime(9, 25), "写实习报告第一节");
        pomodoro(25, today.atTime(16, 0), today.atTime(16, 25), "整理大作业材料");
        pomodoro(25, yesterday.atTime(20, 0), yesterday.atTime(20, 25), "阅读器文档收尾");

        DailySummary summary = new DailySummary();
        summary.setLogDate(today);
        summary.setMood("好");
        summary.setTomorrowFocus("整理提交材料，完成实习总结");
        summary.setUpdatedAt(LocalDateTime.now());
        summaryMapper.insert(summary);

        System.out.println("[cloud-seed] 演示数据已写入：notes=" + noteMapper.selectCount(null));
        System.out.println("[cloud-seed] diaryId=" + diary.getId() + ", miscId=" + misc.getId());
    }

    private Note note(LocalDate date, String category, String title, String content) {
        Note note = new Note();
        note.setLogDate(date);
        note.setCategory(category);
        note.setTitle(title);
        note.setContent(content);
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.insert(note);
        return note;
    }

    private void todo(String title, String priority, LocalDate due, String status, String note, LocalDateTime completedAt) {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setPriority(priority);
        todo.setDueDate(due);
        todo.setStatus(status);
        todo.setNote(note);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setCompletedAt(completedAt);
        todoMapper.insert(todo);
    }

    private void timeline(String type, String title, String detail, String url, BigDecimal amount,
                          String tag, LocalDateTime happenedAt, Integer noteId) {
        Timeline item = new Timeline();
        item.setType(type);
        item.setTitle(title);
        item.setDetail(detail);
        item.setUrl(url);
        item.setAmount(amount);
        item.setTag(tag);
        item.setHappenedAt(happenedAt);
        item.setNoteId(noteId);
        timelineMapper.insert(item);
    }

    private void pomodoro(int minutes, LocalDateTime start, LocalDateTime end, String note) {
        Pomodoro pomodoro = new Pomodoro();
        pomodoro.setMinutes(minutes);
        pomodoro.setStartedAt(start);
        pomodoro.setEndedAt(end);
        pomodoro.setNote(note);
        pomodoroMapper.insert(pomodoro);
    }
}
