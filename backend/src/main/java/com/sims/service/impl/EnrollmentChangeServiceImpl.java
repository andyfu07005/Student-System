package com.sims.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sims.common.BusinessException;
import com.sims.dto.EnrollmentChangeCorrectDTO;
import com.sims.dto.EnrollmentChangeCreateDTO;
import com.sims.entity.EnrollmentChange;
import com.sims.entity.Student;
import com.sims.mapper.EnrollmentChangeMapper;
import com.sims.mapper.StudentMapper;
import com.sims.service.EnrollmentChangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EnrollmentChangeServiceImpl extends ServiceImpl<EnrollmentChangeMapper, EnrollmentChange>
        implements EnrollmentChangeService {

    private final StudentMapper studentMapper;

    private static final Set<String> VALID_CHANGE_TYPES = Set.of("SUSPENSION", "WITHDRAWAL", "TRANSFER", "GRADUATION");
    private static final Set<String> VALID_STATUSES = Set.of("在读", "休学", "退学", "毕业");

    @Override
    @Transactional
    public EnrollmentChange create(EnrollmentChangeCreateDTO dto, Long operatorId) {
        validateChangeType(dto.getChangeType());
        validateStatus(dto.getPreviousStatus());
        validateStatus(dto.getNewStatus());

        Student student = studentMapper.selectById(dto.getStudentId());
        if (student == null) {
            throw new BusinessException(404, "学生不存在");
        }
        if (!student.getStatus().equals(dto.getPreviousStatus())) {
            throw new BusinessException(400, "学生当前状态为【" + student.getStatus() + "】，与填写的异动前状态【" + dto.getPreviousStatus() + "】不一致");
        }

        EnrollmentChange change = new EnrollmentChange();
        change.setStudentId(dto.getStudentId());
        change.setChangeType(dto.getChangeType());
        change.setPreviousStatus(dto.getPreviousStatus());
        change.setNewStatus(dto.getNewStatus());
        change.setPreviousClassId(dto.getPreviousClassId());
        change.setNewClassId(dto.getNewClassId());
        change.setChangeDate(dto.getChangeDate());
        change.setReason(dto.getReason());
        change.setOperatorId(operatorId);
        change.setCorrectedRecordId(null);
        save(change);

        student.setStatus(dto.getNewStatus());
        if ("TRANSFER".equals(dto.getChangeType()) && dto.getNewClassId() != null) {
            student.setClassId(dto.getNewClassId());
        }
        studentMapper.updateById(student);

        return change;
    }

    @Override
    @Transactional
    public EnrollmentChange correct(EnrollmentChangeCorrectDTO dto, Long operatorId) {
        EnrollmentChange original = getById(dto.getCorrectedRecordId());
        if (original == null) {
            throw new BusinessException(404, "原始异动记录不存在");
        }
        if (original.getCorrectedRecordId() != null) {
            throw new BusinessException(400, "不能对更正记录再次更正，请更正原始记录");
        }

        validateChangeType(dto.getChangeType());
        validateStatus(dto.getPreviousStatus());
        validateStatus(dto.getNewStatus());

        Student student = studentMapper.selectById(original.getStudentId());
        if (student == null) {
            throw new BusinessException(404, "关联学生不存在");
        }

        EnrollmentChange correction = new EnrollmentChange();
        correction.setStudentId(original.getStudentId());
        correction.setChangeType(dto.getChangeType());
        correction.setPreviousStatus(dto.getPreviousStatus());
        correction.setNewStatus(dto.getNewStatus());
        correction.setPreviousClassId(dto.getPreviousClassId());
        correction.setNewClassId(dto.getNewClassId());
        correction.setChangeDate(original.getChangeDate());
        correction.setReason(dto.getReason());
        correction.setOperatorId(operatorId);
        correction.setCorrectedRecordId(dto.getCorrectedRecordId());
        correction.setCorrectionReason(dto.getCorrectionReason());
        save(correction);

        student.setStatus(dto.getNewStatus());
        if ("TRANSFER".equals(dto.getChangeType()) && dto.getNewClassId() != null) {
            student.setClassId(dto.getNewClassId());
        }
        studentMapper.updateById(student);

        return correction;
    }

    @Override
    public List<EnrollmentChange> getTimelineByStudentId(Long studentId) {
        LambdaQueryWrapper<EnrollmentChange> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnrollmentChange::getStudentId, studentId)
                .orderByDesc(EnrollmentChange::getChangeDate)
                .orderByDesc(EnrollmentChange::getCreatedAt);
        return list(wrapper);
    }

    @Override
    public Page<EnrollmentChange> page(int pageNum, int pageSize, Long studentId, String changeType) {
        LambdaQueryWrapper<EnrollmentChange> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(EnrollmentChange::getStudentId, studentId);
        }
        if (changeType != null && !changeType.isBlank()) {
            wrapper.eq(EnrollmentChange::getChangeType, changeType);
        }
        wrapper.orderByDesc(EnrollmentChange::getChangeDate)
                .orderByDesc(EnrollmentChange::getCreatedAt);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    private void validateChangeType(String changeType) {
        if (!VALID_CHANGE_TYPES.contains(changeType)) {
            throw new BusinessException(400, "无效的异动类型: " + changeType + "，有效值: " + VALID_CHANGE_TYPES);
        }
    }

    private void validateStatus(String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new BusinessException(400, "无效的状态值: " + status + "，有效值: " + VALID_STATUSES);
        }
    }
}
