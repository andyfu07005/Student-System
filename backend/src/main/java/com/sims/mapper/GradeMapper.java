package com.sims.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sims.entity.Grade;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface GradeMapper extends BaseMapper<Grade> {

    @Select("SELECT g.*, s.name AS student_name, s.student_no, c.name AS course_name, c.course_no " +
            "FROM grade g " +
            "LEFT JOIN student s ON g.student_id = s.id " +
            "LEFT JOIN course c ON g.course_id = c.id " +
            "${ew.customSqlSegment}")
    IPage<Grade> selectPageWithDetails(IPage<Grade> page,
            @Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<Grade> wrapper);

    @Select("SELECT g.*, s.name AS student_name, s.student_no, c.name AS course_name, c.course_no " +
            "FROM grade g " +
            "LEFT JOIN student s ON g.student_id = s.id " +
            "LEFT JOIN course c ON g.course_id = c.id " +
            "${ew.customSqlSegment}")
    List<Grade> selectListWithDetails(@Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<Grade> wrapper);

    @Select("SELECT " +
            "  COUNT(*) AS total_count, " +
            "  AVG(g.score) AS avg_score, " +
            "  MAX(g.score) AS max_score, " +
            "  MIN(g.score) AS min_score, " +
            "  SUM(CASE WHEN g.score >= 90 THEN 1 ELSE 0 END) AS excellent_count, " +
            "  SUM(CASE WHEN g.score >= 75 AND g.score < 90 THEN 1 ELSE 0 END) AS good_count, " +
            "  SUM(CASE WHEN g.score >= 60 AND g.score < 75 THEN 1 ELSE 0 END) AS pass_count, " +
            "  SUM(CASE WHEN g.score < 60 THEN 1 ELSE 0 END) AS fail_count " +
            "FROM grade g ${ew.customSqlSegment}")
    Map<String, Object> selectStatistics(@Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<Grade> wrapper);

    @Select("SELECT " +
            "  CASE " +
            "    WHEN g.score >= 90 THEN '优秀(90-100)' " +
            "    WHEN g.score >= 80 THEN '良好(80-89)' " +
            "    WHEN g.score >= 70 THEN '中等(70-79)' " +
            "    WHEN g.score >= 60 THEN '及格(60-69)' " +
            "    ELSE '不及格(<60)' " +
            "  END AS score_range, " +
            "  COUNT(*) AS count " +
            "FROM grade g ${ew.customSqlSegment} " +
            "GROUP BY score_range " +
            "ORDER BY MIN(g.score)")
    List<Map<String, Object>> selectScoreDistribution(@Param("ew") com.baomidou.mybatisplus.core.conditions.Wrapper<Grade> wrapper);
}
