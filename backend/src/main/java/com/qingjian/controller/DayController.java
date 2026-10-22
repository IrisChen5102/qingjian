package com.qingjian.controller;

import com.qingjian.dto.DayView;
import com.qingjian.service.DayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/day")
public class DayController {

    private final DayService dayService;

    public DayController(DayService dayService) {
        this.dayService = dayService;
    }

    @GetMapping("/{date}")
    public DayView get(@PathVariable String date) {
        return dayService.build(LocalDate.parse(date));
    }

    @PutMapping("/{date}/summary")
    public Map<String, Boolean> saveSummary(@PathVariable String date, @RequestBody Map<String, String> body) {
        dayService.upsertSummary(LocalDate.parse(date), body.get("mood"), body.get("tomorrowFocus"));
        return Map.of("ok", true);
    }
}
