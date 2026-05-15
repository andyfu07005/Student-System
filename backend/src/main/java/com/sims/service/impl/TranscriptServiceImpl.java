package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.sims.common.BusinessException;
import com.sims.dto.TranscriptDTO;
import com.sims.entity.*;
import com.sims.mapper.*;
import com.sims.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TranscriptServiceImpl implements TranscriptService {

    private final StudentMapper studentMapper;
    private final ClassInfoMapper classInfoMapper;
    private final GradeMapper gradeMapper;
    private final CourseMapper courseMapper;

    @Override
    public TranscriptDTO getStudentTranscript(Long studentId, String semester) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
        TranscriptDTO dto = new TranscriptDTO();
        dto.setStudentNo(student.getStudentNo());
        dto.setStudentName(student.getName());
        dto.setGender(student.getGender());
        if (student.getEnrollmentDate() != null) {
            dto.setEnrollmentDate(student.getEnrollmentDate().toString());
        }

        if (student.getClassId() != null) {
            ClassInfo classInfo = classInfoMapper.selectById(student.getClassId());
            if (classInfo != null) {
                dto.setClassName(classInfo.getName());
                dto.setGrade(classInfo.getGrade());
                dto.setMajor(classInfo.getMajor());
            }
        }

        LambdaQueryWrapper<Grade> wrapper = new LambdaQueryWrapper<Grade>()
                .eq(Grade::getStudentId, studentId);
        if (semester != null && !semester.isEmpty()) {
            wrapper.eq(Grade::getSemester, semester);
        }
        wrapper.orderByDesc(Grade::getSemester).orderByAsc(Grade::getCourseId);
        List<Grade> grades = gradeMapper.selectList(wrapper);

        if (!grades.isEmpty()) {
            dto.setAcademicYear(grades.get(0).getAcademicYear());
            dto.setSemester(semester);
        }

        Map<Long, Course> courseMap = loadCourseMap(grades);
        fillGradeDetails(grades, courseMap);

        Map<String, List<Grade>> grouped = grades.stream()
                .collect(Collectors.groupingBy(Grade::getSemester, LinkedHashMap::new, Collectors.toList()));

        List<TranscriptDTO.SemesterGroup> semesterGroups = new ArrayList<>();
        BigDecimal totalWeightedGpa = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (Map.Entry<String, List<Grade>> entry : grouped.entrySet()) {
            TranscriptDTO.SemesterGroup sg = new TranscriptDTO.SemesterGroup();
            sg.setSemester(entry.getKey());

            List<TranscriptDTO.CourseGrade> courseGrades = new ArrayList<>();
            BigDecimal semesterWeightedGpa = BigDecimal.ZERO;
            BigDecimal semesterCredits = BigDecimal.ZERO;

            for (Grade g : entry.getValue()) {
                TranscriptDTO.CourseGrade cg = convertToCourseGrade(g);
                courseGrades.add(cg);
                BigDecimal credit = g.getCredit() != null ? g.getCredit() : BigDecimal.ZERO;
                semesterCredits = semesterCredits.add(credit);
                if (cg.getGradePoint() != null) {
                    semesterWeightedGpa = semesterWeightedGpa.add(cg.getGradePoint().multiply(credit));
                }
            }

            sg.setCourses(courseGrades);
            sg.setSemesterCredits(semesterCredits);
            if (semesterCredits.compareTo(BigDecimal.ZERO) > 0) {
                sg.setSemesterGpa(semesterWeightedGpa.divide(semesterCredits, 2, RoundingMode.HALF_UP));
            }
            semesterGroups.add(sg);

            totalCredits = totalCredits.add(semesterCredits);
            totalWeightedGpa = totalWeightedGpa.add(semesterWeightedGpa);
        }

        dto.setSemesters(semesterGroups);
        dto.setTotalCredits(totalCredits);
        if (totalCredits.compareTo(BigDecimal.ZERO) > 0) {
            dto.setTotalGpa(totalWeightedGpa.divide(totalCredits, 2, RoundingMode.HALF_UP));
        }

        return dto;
    }

    @Override
    public byte[] generatePdf(Long studentId, String semester) {
        TranscriptDTO dto = getStudentTranscript(studentId, semester);
        return buildPdf(Collections.singletonList(dto));
    }

    @Override
    public byte[] generateBatchPdf(Long classId, String semester) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<Student>()
                .eq(Student::getClassId, classId)
                .orderByAsc(Student::getStudentNo);
        List<Student> students = studentMapper.selectList(wrapper);

        List<TranscriptDTO> transcripts = new ArrayList<>();
        for (Student s : students) {
            transcripts.add(getStudentTranscript(s.getId(), semester));
        }

        if (transcripts.isEmpty()) {
            throw new BusinessException(404, "该班级没有学生数据");
        }

        return buildPdf(transcripts);
    }

    private byte[] buildPdf(List<TranscriptDTO> transcripts) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            BaseFont bf = loadChineseFont();
            Font titleFont = new Font(bf, 18, Font.BOLD);
            Font subtitleFont = new Font(bf, 12, Font.NORMAL);
            Font headerFont = new Font(bf, 10, Font.BOLD);
            Font cellFont = new Font(bf, 9, Font.NORMAL);

            for (int idx = 0; idx < transcripts.size(); idx++) {
                TranscriptDTO dto = transcripts.get(idx);
                if (idx > 0) {
                    document.newPage();
                }
                addTranscriptContent(document, dto, titleFont, subtitleFont, headerFont, cellFont);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF生成失败: " + e.getMessage(), e);
        }
    }

    private void addTranscriptContent(Document document, TranscriptDTO dto,
                                       Font titleFont, Font subtitleFont,
                                       Font headerFont, Font cellFont) throws Exception {
        Paragraph title = new Paragraph(dto.getSchoolName() + " 成绩单", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" "));

        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        float[] infoWidths = {1f, 2f, 1f, 2f};
        infoTable.setWidths(infoWidths);

        String[][] infoData = {
                {"学号", dto.getStudentNo(), "姓名", dto.getStudentName()},
                {"性别", dto.getGender(), "班级", dto.getClassName() != null ? dto.getClassName() : ""},
                {"年级", dto.getGrade() != null ? dto.getGrade() : "", "专业", dto.getMajor() != null ? dto.getMajor() : ""},
                {"入学日期", dto.getEnrollmentDate() != null ? dto.getEnrollmentDate() : "",
                 "学年", dto.getAcademicYear() != null ? dto.getAcademicYear() : ""}
        };

        for (String[] row : infoData) {
            for (int i = 0; i < row.length; i++) {
                PdfPCell cell;
                if (i % 2 == 0) {
                    cell = new PdfPCell(new Phrase(row[i], headerFont));
                    cell.setBackgroundColor(new java.awt.Color(240, 240, 240));
                } else {
                    cell = new PdfPCell(new Phrase(row[i], cellFont));
                }
                cell.setPadding(4);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                infoTable.addCell(cell);
            }
        }
        document.add(infoTable);

        if (dto.getSemesters() != null) {
            for (TranscriptDTO.SemesterGroup sg : dto.getSemesters()) {
                document.add(new Paragraph(" "));
                Paragraph semesterTitle = new Paragraph("学期: " + sg.getSemester(), subtitleFont);
                document.add(semesterTitle);

                PdfPTable gradeTable = new PdfPTable(6);
                gradeTable.setWidthPercentage(100);
                float[] gradeWidths = {1f, 3f, 1f, 1f, 1f, 1f};
                gradeTable.setWidths(gradeWidths);

                String[] headers = {"课程编号", "课程名称", "学分", "类型", "成绩", "绩点"};
                for (String h : headers) {
                    PdfPCell headerCell = new PdfPCell(new Phrase(h, headerFont));
                    headerCell.setBackgroundColor(new java.awt.Color(220, 220, 220));
                    headerCell.setPadding(3);
                    headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    gradeTable.addCell(headerCell);
                }

                for (TranscriptDTO.CourseGrade cg : sg.getCourses()) {
                    String[] cells = {
                            cg.getCourseNo(),
                            cg.getCourseName(),
                            cg.getCredit() != null ? cg.getCredit().toString() : "",
                            cg.getCourseType(),
                            cg.getScore() != null ? cg.getScore().toString() : "",
                            cg.getGradePoint() != null ? cg.getGradePoint().toString() : ""
                    };
                    for (String cellText : cells) {
                        PdfPCell cell = new PdfPCell(new Phrase(cellText, cellFont));
                        cell.setPadding(3);
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        gradeTable.addCell(cell);
                    }
                }

                PdfPCell summaryCell = new PdfPCell(new Phrase(
                        "学期学分: " + sg.getSemesterCredits() + "  学期绩点: " +
                                (sg.getSemesterGpa() != null ? sg.getSemesterGpa().toString() : "-"),
                        headerFont));
                summaryCell.setColspan(6);
                summaryCell.setPadding(4);
                summaryCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                gradeTable.addCell(summaryCell);

                document.add(gradeTable);
            }
        }

        document.add(new Paragraph(" "));
        Paragraph totalLine = new Paragraph(
                "总学分: " + (dto.getTotalCredits() != null ? dto.getTotalCredits().toString() : "0") +
                "    平均绩点(GPA): " + (dto.getTotalGpa() != null ? dto.getTotalGpa().toString() : "-"),
                subtitleFont);
        totalLine.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalLine);
    }

    private BaseFont loadChineseFont() throws Exception {
        String[] fontPaths = {
                "C:/Windows/Fonts/simsun.ttc,0",
                "C:/Windows/Fonts/msyh.ttc,0",
                "C:/Windows/Fonts/simhei.ttf",
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc,0",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0",
                "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc,0",
        };
        for (String path : fontPaths) {
            try {
                return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception ignored) {
            }
        }
        return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
    }

    private Map<Long, Course> loadCourseMap(List<Grade> grades) {
        Set<Long> courseIds = grades.stream().map(Grade::getCourseId).collect(Collectors.toSet());
        if (courseIds.isEmpty()) return Collections.emptyMap();
        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        return courses.stream().collect(Collectors.toMap(Course::getId, c -> c));
    }

    private void fillGradeDetails(List<Grade> grades, Map<Long, Course> courseMap) {
        for (Grade g : grades) {
            Course c = courseMap.get(g.getCourseId());
            if (c != null) {
                g.setCourseNo(c.getCourseNo());
                g.setCourseName(c.getName());
                g.setCredit(c.getCredit());
                g.setCourseType(c.getType());
            }
        }
    }

    private TranscriptDTO.CourseGrade convertToCourseGrade(Grade g) {
        TranscriptDTO.CourseGrade cg = new TranscriptDTO.CourseGrade();
        cg.setCourseNo(g.getCourseNo() != null ? g.getCourseNo() : "");
        cg.setCourseName(g.getCourseName() != null ? g.getCourseName() : "");
        cg.setCredit(g.getCredit());
        cg.setCourseType(g.getCourseType());
        cg.setScore(g.getScore());
        cg.setExamType(g.getExamType());
        cg.setGradePoint(calculateGradePoint(g.getScore()));
        return cg;
    }

    private BigDecimal calculateGradePoint(BigDecimal score) {
        if (score == null) return null;
        double s = score.doubleValue();
        if (s >= 90) return new BigDecimal("4.0");
        if (s >= 85) return new BigDecimal("3.7");
        if (s >= 82) return new BigDecimal("3.3");
        if (s >= 78) return new BigDecimal("3.0");
        if (s >= 75) return new BigDecimal("2.7");
        if (s >= 72) return new BigDecimal("2.3");
        if (s >= 68) return new BigDecimal("2.0");
        if (s >= 64) return new BigDecimal("1.5");
        if (s >= 60) return new BigDecimal("1.0");
        return BigDecimal.ZERO;
    }
}
