package com.sims.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ImportExportService {

    Map<String, Object> importStudents(MultipartFile file);

    void exportStudents(String keyword, Long classId, String status, HttpServletResponse response);

    void downloadTemplate(HttpServletResponse response);
}
