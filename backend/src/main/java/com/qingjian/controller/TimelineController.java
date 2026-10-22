package com.qingjian.controller;

import com.qingjian.entity.Timeline;
import com.qingjian.mapper.TimelineMapper;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/timeline")
public class TimelineController {

    private final TimelineMapper timelineMapper;

    public TimelineController(TimelineMapper timelineMapper) {
        this.timelineMapper = timelineMapper;
    }

    @PostMapping
    public Timeline create(@RequestBody Timeline item) {
        item.setId(null);
        if (item.getHappenedAt() == null) {
            item.setHappenedAt(LocalDateTime.now());
        }
        timelineMapper.insert(item);
        return item;
    }

    @PutMapping("/{id}")
    public Timeline update(@PathVariable Integer id, @RequestBody Timeline item) {
        item.setId(id);
        timelineMapper.updateById(item);
        return timelineMapper.selectById(id);
    }

    @DeleteMapping("/{id}")
    public Map<String, Boolean> delete(@PathVariable Integer id) {
        timelineMapper.deleteById(id);
        return Map.of("ok", true);
    }
}
