package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sims.common.BusinessException;
import com.sims.entity.Student;
import com.sims.mapper.StudentMapper;
import com.sims.service.ImportExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImportExportServiceImpl implements ImportExportService {

    private final StudentMapper studentMapper;

    private static final String[] HEADERS = {"学号", "姓名", "性别", "出生日期", "身份证号", "联系电话", "家庭住址", "入学日期", "班级ID", "学籍状态"};

    @Override
    public Map<String, Object> importStudents(MultipartFile file) {
        int success = 0;
        List<Map<String, Object>> failures = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                try {
                    Student student = parseRow(row);
                    if (StringUtils.hasText(student.getStudentNo())) {
                        validateNoDuplicate(student.getStudentNo(), student.getIdCard());
                        studentMapper.insert(student);
                        success++;
                    }
                } catch (Exception e) {
                    Map<String, Object> err = new LinkedHashMap<>();
                    err.put("row", i + 1);
                    err.put("reason", e.getMessage());
                    failures.add(err);
                }
            }
        } catch (IOException e) {
            throw new BusinessException(400, "文件读取失败: " + e.getMessage());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("fail", failures.size());
        result.put("failures", failures);
        return result;
    }

    private Student parseRow(Row row) {
        Student s = new Student();
        s.setStudentNo(getCellString(row, 0));
        s.setName(getCellString(row, 1));
        s.setGender(getCellString(row, 2));
        String birth = getCellString(row, 3);
        if (StringUtils.hasText(birth)) {
            s.setBirthDate(LocalDate.parse(birth, DateTimeFormatter.ISO_LOCAL_DATE));
        }
        s.setIdCard(getCellString(row, 4));
        s.setPhone(getCellString(row, 5));
        s.setAddress(getCellString(row, 6));
        String enroll = getCellString(row, 7);
        if (StringUtils.hasText(enroll)) {
            s.setEnrollmentDate(LocalDate.parse(enroll, DateTimeFormatter.ISO_LOCAL_DATE));
        }
        String classId = getCellString(row, 8);
        if (StringUtils.hasText(classId)) {
            s.setClassId(Long.parseLong(classId));
        }
        s.setStatus(StringUtils.hasText(getCellString(row, 9)) ? getCellString(row, 9) : "在读");
        return s;
    }

    private String getCellString(Row row, int index) {
        Cell cell = row.getCell(index);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                yield val == Math.floor(val) ? String.valueOf((long) val) : String.valueOf(val);
            }
            default -> null;
        };
    }

    private void validateNoDuplicate(String studentNo, String idCard) {
        if (studentMapper.selectCount(new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, studentNo)) > 0) {
            throw new BusinessException("学号 " + studentNo + " 已存在");
        }
        if (StringUtils.hasText(idCard) && studentMapper.selectCount(new LambdaQueryWrapper<Student>().eq(Student::getIdCard, idCard)) > 0) {
            throw new BusinessException("身份证号 " + idCard + " 已存在");
        }
    }

    @Override
    public void exportStudents(String keyword, Long classId, String status, HttpServletResponse response) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Student::getName, keyword).or().like(Student::getStudentNo, keyword));
        }
        if (classId != null) wrapper.eq(Student::getClassId, classId);
        if (StringUtils.hasText(status)) wrapper.eq(Student::getStatus, status);
        wrapper.orderByDesc(Student::getCreatedAt);
        List<Student> students = studentMapper.selectList(wrapper);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("学生信息");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(HEADERS[i]);
            }
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(s.getStudentNo());
                row.createCell(1).setCellValue(s.getName());
                row.createCell(2).setCellValue(s.getGender());
                row.createCell(3).setCellValue(s.getBirthDate() != null ? s.getBirthDate().toString() : "");
                row.createCell(4).setCellValue(s.getIdCard() != null ? s.getIdCard() : "");
                row.createCell(5).setCellValue(s.getPhone() != null ? s.getPhone() : "");
                row.createCell(6).setCellValue(s.getAddress() != null ? s.getAddress() : "");
                row.createCell(7).setCellValue(s.getEnrollmentDate() != null ? s.getEnrollmentDate().toString() : "");
                row.createCell(8).setCellValue(s.getClassId() != null ? String.valueOf(s.getClassId()) : "");
                row.createCell(9).setCellValue(s.getStatus());
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = URLEncoder.encode("学生信息导出.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
            }
        } catch (IOException e) {
            throw new BusinessException("导出失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("学生信息导入模板");
            Row headerRow = sheet.createRow(0);
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);

            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(style);
            }

            // Add a sample row
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("2024001");
            sampleRow.createCell(1).setCellValue("张三");
            sampleRow.createCell(2).setCellValue("男");
            sampleRow.createCell(3).setCellValue("2000-01-01");
            sampleRow.createCell(4).setCellValue("");
            sampleRow.createCell(5).setCellValue("13800138000");
            sampleRow.createCell(6).setCellValue("");
            sampleRow.createCell(7).setCellValue("2024-09-01");
            sampleRow.createCell(8).setCellValue("1");
            sampleRow.createCell(9).setCellValue("在读");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = URLEncoder.encode("学生信息导入模板.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
            try (OutputStream os = response.getOutputStream()) {
                workbook.write(os);
            }
        } catch (IOException e) {
            throw new BusinessException("模板生成失败: " + e.getMessage());
        }
    }
}
