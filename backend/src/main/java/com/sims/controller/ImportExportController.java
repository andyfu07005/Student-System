package com.sims.controller;

import com.sims.common.Result;
import com.sims.service.ImportExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ImportExportController {

    private final ImportExportService importExportService;

    @PostMapping("/import-students")
    public Result<Map<String, Object>> importStudents(@RequestParam("file") MultipartFile file) {
        return Result.ok(importExportService.importStudents(file));
    }

    @GetMapping("/export-students")
    public void exportStudents(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String status,
            HttpServletResponse response) {
        importExportService.exportStudents(keyword, classId, status, response);
    }

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) {
        importExportService.downloadTemplate(response);
    }
}
