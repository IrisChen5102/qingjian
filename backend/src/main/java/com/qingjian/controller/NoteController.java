package com.qingjian.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingjian.entity.Note;
import com.qingjian.mapper.NoteMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteMapper noteMapper;

    public NoteController(NoteMapper noteMapper) {
        this.noteMapper = noteMapper;
    }

    @GetMapping
    public List<Note> list(@RequestParam(required = false) String month) {
        LambdaQueryWrapper<Note> query = new LambdaQueryWrapper<>();
        if (month != null && !month.isBlank()) {
            YearMonth ym = YearMonth.parse(month);
            query.ge(Note::getLogDate, ym.atDay(1)).lt(Note::getLogDate, ym.plusMonths(1).atDay(1));
        }
        query.orderByDesc(Note::getLogDate).orderByDesc(Note::getId);
        return noteMapper.selectList(query);
    }

    @GetMapping("/{id}")
    public Note get(@PathVariable Integer id) {
        return noteMapper.selectById(id);
    }

    @PostMapping
    public Note create(@RequestBody Note note) {
        note.setId(null);
        if (note.getLogDate() == null) {
            note.setLogDate(LocalDate.now());
        }
        if (note.getCategory() == null) {
            note.setCategory("日记");
        }
        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.insert(note);
        return note;
    }

    @PutMapping("/{id}")
    public Note update(@PathVariable Integer id, @RequestBody Note note) {
        note.setId(id);
        note.setUpdatedAt(LocalDateTime.now());
        noteMapper.updateById(note);
        return noteMapper.selectById(id);
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> delete(@PathVariable Integer id) {
        noteMapper.deleteById(id);
        return Map.of("ok", true);
    }
}
