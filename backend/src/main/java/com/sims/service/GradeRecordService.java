package com.sims.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.dto.GradeRecordDTO;
import com.sims.entity.GpaAlgorithm;
import com.sims.entity.GradeRecord;
import com.sims.vo.GpaSummaryVO;
import com.sims.vo.GradeRecordVO;

public interface GradeRecordService extends IService<GradeRecord> {
    Page<GradeRecordVO> search(Long studentId, Long courseId, String academicYear, String semester,
                               GpaAlgorithm algorithm, int page, int size);

    GradeRecordVO create(GradeRecordDTO dto, GpaAlgorithm algorithm);

    GradeRecordVO update(Long id, GradeRecordDTO dto, GpaAlgorithm algorithm);

    GpaSummaryVO calculateGpa(Long studentId, GpaAlgorithm algorithm);
}
