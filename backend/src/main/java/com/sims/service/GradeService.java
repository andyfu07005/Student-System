package com.sims.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.entity.Grade;

public interface GradeService extends IService<Grade> {
    Page<Grade> search(String keyword, Long studentId, Long courseId, String semester, int page, int size);
}
