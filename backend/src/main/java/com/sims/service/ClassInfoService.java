package com.sims.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sims.entity.ClassInfo;
import com.sims.entity.Student;

import java.util.List;

public interface ClassInfoService extends IService<ClassInfo> {

    Page<ClassInfo> search(String keyword, int page, int size);

    List<Student> getStudentsByClassId(Long classId);

    @Override
    boolean save(ClassInfo classInfo);

    void deleteClass(Long id);
}
