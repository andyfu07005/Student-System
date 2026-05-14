package com.sims.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.dto.EnrollmentChangeCorrectDTO;
import com.sims.dto.EnrollmentChangeCreateDTO;
import com.sims.entity.EnrollmentChange;

import java.util.List;

public interface EnrollmentChangeService {
    EnrollmentChange create(EnrollmentChangeCreateDTO dto, Long operatorId);
    EnrollmentChange correct(EnrollmentChangeCorrectDTO dto, Long operatorId);
    List<EnrollmentChange> getTimelineByStudentId(Long studentId);
    Page<EnrollmentChange> page(int pageNum, int pageSize, Long studentId, String changeType);
    EnrollmentChange getById(Long id);
}
