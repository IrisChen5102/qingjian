package com.qingjian.controller;

import com.qingjian.dto.ExportData;
import com.qingjian.service.DataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class DataController {

    private final DataService dataService;

    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/export")
    public ExportData export() {
        return dataService.exportData();
    }

    @PostMapping("/import")
    public Map<String, Object> importData(@RequestBody ExportData data) {
        return Map.of("ok", true, "counts", dataService.importData(data));
    }
}
