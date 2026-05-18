package com.sims.controller;

import com.sims.common.Result;
import com.sims.dto.TranscriptDTO;
import com.sims.service.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/transcripts")
@RequiredArgsConstructor
public class TranscriptController {

    private final TranscriptService transcriptService;

    @GetMapping("/{studentId}")
    public Result<TranscriptDTO> getTranscript(
            @PathVariable Long studentId,
            @RequestParam(required = false) String semester) {
        return Result.ok(transcriptService.getStudentTranscript(studentId, semester));
    }

    @GetMapping("/{studentId}/pdf")
    public ResponseEntity<byte[]> downloadPdf(
            @PathVariable Long studentId,
            @RequestParam(required = false) String semester) {
        byte[] pdf = transcriptService.generatePdf(studentId, semester);
        return buildPdfResponse(pdf, "transcript_" + studentId + ".pdf");
    }

    @GetMapping("/batch/class/{classId}/pdf")
    public ResponseEntity<byte[]> downloadBatchPdf(
            @PathVariable Long classId,
            @RequestParam(required = false) String semester) {
        byte[] pdf = transcriptService.generateBatchPdf(classId, semester);
        return buildPdfResponse(pdf, "transcript_class_" + classId + ".pdf");
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdf, String filename) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
