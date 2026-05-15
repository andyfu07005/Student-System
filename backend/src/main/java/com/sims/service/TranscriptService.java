package com.sims.service;

import com.sims.dto.TranscriptDTO;

public interface TranscriptService {
    TranscriptDTO getStudentTranscript(Long studentId, String semester);
    byte[] generatePdf(Long studentId, String semester);
    byte[] generateBatchPdf(Long classId, String semester);
}
