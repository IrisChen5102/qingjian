package com.qingjian.controller;

import com.qingjian.entity.Pomodoro;
import com.qingjian.mapper.PomodoroMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/pomodoros")
public class PomodoroController {

    private final PomodoroMapper pomodoroMapper;

    public PomodoroController(PomodoroMapper pomodoroMapper) {
        this.pomodoroMapper = pomodoroMapper;
    }

    @PostMapping
    public Pomodoro create(@RequestBody Pomodoro pomodoro) {
        pomodoro.setId(null);
        if (pomodoro.getMinutes() == null) {
            pomodoro.setMinutes(25);
        }
        if (pomodoro.getEndedAt() == null) {
            pomodoro.setEndedAt(LocalDateTime.now());
        }
        if (pomodoro.getStartedAt() == null) {
            pomodoro.setStartedAt(pomodoro.getEndedAt().minusMinutes(pomodoro.getMinutes()));
        }
        pomodoroMapper.insert(pomodoro);
        return pomodoro;
    }
}
