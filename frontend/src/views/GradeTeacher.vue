<template>
  <div class="grade-teacher">
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="选择课程">
          <el-select v-model="selectedCourseId" placeholder="请选择课程" @change="onCourseChange" style="width:240px">
            <el-option
              v-for="tc in myCourses"
              :key="tc.id"
              :label="`${tc.courseId} - 学期:${tc.semester}`"
              :value="tc.courseId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学期筛选">
          <el-select v-model="filters.semester" clearable placeholder="全部学期" @change="fetchCourseData">
            <el-option label="2024-2025-1" value="2024-2025-1" />
            <el-option label="2024-2025-2" value="2024-2025-2" />
            <el-option label="2025-2026-1" value="2025-2026-1" />
            <el-option label="2025-2026-2" value="2025-2026-2" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <template v-if="selectedCourseId">
      <el-row :gutter="16" class="stats-row">
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.totalCount || 0 }}</div>
            <div class="stat-label">总人数</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.avgScore || '-' }}</div>
            <div class="stat-label">平均分</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.maxScore || '-' }}</div>
            <div class="stat-label">最高分</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.minScore || '-' }}</div>
            <div class="stat-label">最低分</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.passRate || '-' }}%</div>
            <div class="stat-label">及格率</div>
          </el-card>
        </el-col>
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-value">{{ stats.excellentRate || '-' }}%</div>
            <div class="stat-label">优秀率</div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16" class="chart-row">
        <el-col :span="12">
          <el-card>
            <template #header>成绩分布（饼图）</template>
            <div ref="pieChartRef" style="width:100%;height:300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>成绩分布（柱状图）</template>
            <div ref="barChartRef" style="width:100%;height:300px"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="table-card">
        <template #header>成绩明细</template>
        <el-table :data="gradeList" stripe v-loading="loading" empty-text="暂无成绩记录">
          <el-table-column prop="studentName" label="学生姓名" width="120" />
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="score" label="成绩" width="100">
            <template #default="{ row }">
              <el-tag :type="scoreType(row.score)">{{ row.score }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="examType" label="考试类型" width="100" />
          <el-table-column prop="semester" label="学期" width="140" />
        </el-table>
        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            layout="total, prev, pager, next"
            @change="loadCourseGrades"
          />
        </div>
      </el-card>
    </template>
    <el-empty v-else description="请先选择所授课程" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { myTeacherCourses, type TeacherCourse } from '@/api/teacherCourse'
import { courseGrades as apiCourseGrades, courseStatistics, type Grade, type GradeStats } from '@/api/grade'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const myCourses = ref<TeacherCourse[]>([])
const selectedCourseId = ref<number | null>(null)
const gradeList = ref<Grade[]>([])
const stats = reactive<GradeStats>({})

const pieChartRef = ref<HTMLDivElement>()
const barChartRef = ref<HTMLDivElement>()
let pieInstance: echarts.ECharts | null = null
let barInstance: echarts.ECharts | null = null

const filters = reactive({ semester: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

function scoreType(score: number) {
  if (score >= 90) return 'success'
  if (score >= 75) return 'warning'
  if (score >= 60) return ''
  return 'danger'
}

async function fetchMyCourses() {
  try {
    myCourses.value = await myTeacherCourses()
  } catch {
    ElMessage.error('获取课程列表失败')
  }
}

function onCourseChange() {
  pagination.page = 1
  fetchCourseData()
}

async function loadCourseGrades() {
  if (!selectedCourseId.value) return
  loading.value = true
  try {
    const data = await apiCourseGrades(selectedCourseId.value, {
      semester: filters.semester,
      page: pagination.page,
      size: pagination.size,
    })
    gradeList.value = data.records || []
    pagination.total = data.total || 0
  } catch {
    ElMessage.error('获取成绩列表失败')
  } finally {
    loading.value = false
  }
}

async function fetchCourseStats() {
  if (!selectedCourseId.value) return
  try {
    const data = await courseStatistics(selectedCourseId.value, filters.semester || undefined)
    Object.assign(stats, data)
    await nextTick()
    renderCharts(data.distribution || [])
  } catch {
    // optional
  }
}

function renderCharts(distribution: { scoreRange: string; count: number }[]) {
  const names = distribution.map(d => d.scoreRange)
  const values = distribution.map(d => d.count)

  if (pieChartRef.value) {
    if (!pieInstance) pieInstance = echarts.init(pieChartRef.value)
    pieInstance.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: '70%',
        data: distribution.map(d => ({ name: d.scoreRange, value: d.count })),
        label: { formatter: '{b}\n{d}%' },
      }],
    })
  }

  if (barChartRef.value) {
    if (!barInstance) barInstance = echarts.init(barChartRef.value)
    barInstance.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: names, axisLabel: { rotate: 15 } },
      yAxis: { type: 'value', name: '人数' },
      series: [{
        type: 'bar',
        data: values,
        itemStyle: { color: '#409EFF' },
      }],
      grid: { bottom: 60 },
    })
  }
}

function fetchCourseData() {
  pagination.page = 1
  loadCourseGrades()
  fetchCourseStats()
}

onMounted(() => {
  fetchMyCourses()
})
</script>

<style scoped>
.grade-teacher { display: flex; flex-direction: column; gap: 16px; }
.filter-card { margin-bottom: 0; }
.stats-row { margin-bottom: 0; }
.chart-row { margin-bottom: 0; }
.stat-card { text-align: center; }
.stat-value { font-size: 24px; font-weight: bold; color: #409EFF; }
.stat-label { font-size: 13px; color: #909399; margin-top: 8px; }
.table-card { margin-top: 0; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
