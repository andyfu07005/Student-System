<template>
  <div class="grade-student">
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="学期">
          <el-select v-model="filters.semester" clearable placeholder="全部学期" @change="fetchData">
            <el-option label="2024-2025-1" value="2024-2025-1" />
            <el-option label="2024-2025-2" value="2024-2025-2" />
            <el-option label="2025-2026-1" value="2025-2026-1" />
            <el-option label="2025-2026-2" value="2025-2026-2" />
          </el-select>
        </el-form-item>
        <el-form-item label="学年">
          <el-select v-model="filters.academicYear" clearable placeholder="全部学年" @change="fetchData">
            <el-option label="2024-2025" value="2024-2025" />
            <el-option label="2025-2026" value="2025-2026" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.avgScore || '-' }}</div>
          <div class="stat-label">平均分</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.maxScore || '-' }}</div>
          <div class="stat-label">最高分</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.passRate || '-' }}%</div>
          <div class="stat-label">及格率</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.excellentRate || '-' }}%</div>
          <div class="stat-label">优秀率</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card">
      <template #header>成绩分布</template>
      <div ref="chartRef" style="width:100%;height:300px"></div>
    </el-card>

    <el-card class="table-card">
      <template #header>成绩列表</template>
      <el-table :data="grades" stripe v-loading="loading" empty-text="暂无成绩记录">
        <el-table-column prop="courseName" label="课程" min-width="150" />
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        <el-table-column prop="score" label="成绩" width="100">
          <template #default="{ row }">
            <el-tag :type="scoreType(row.score)">{{ row.score }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="examType" label="考试类型" width="100" />
        <el-table-column prop="semester" label="学期" width="140" />
        <el-table-column prop="academicYear" label="学年" width="120" />
      </el-table>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @change="fetchGrades"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'
import { myGrades, myStatistics, type Grade, type GradeStats } from '@/api/grade'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const grades = ref<Grade[]>([])
const stats = reactive<GradeStats>({})
const chartRef = ref<HTMLDivElement>()
let chartInstance: echarts.ECharts | null = null

const filters = reactive({ semester: '', academicYear: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })

function scoreType(score: number) {
  if (score >= 90) return 'success'
  if (score >= 75) return 'warning'
  if (score >= 60) return ''
  return 'danger'
}

async function fetchGrades() {
  loading.value = true
  try {
    const data = await myGrades({
      semester: filters.semester,
      academicYear: filters.academicYear,
      page: pagination.page,
      size: pagination.size,
    })
    grades.value = data.records || []
    pagination.total = data.total || 0
  } catch {
    ElMessage.error('获取成绩失败')
  } finally {
    loading.value = false
  }
}

async function fetchStats() {
  try {
    const data = await myStatistics(filters.academicYear || undefined)
    Object.assign(stats, data)
    await nextTick()
    renderChart(data.distribution || [])
  } catch {
    // statistics are optional
  }
}

function renderChart(distribution: { scoreRange: string; count: number }[]) {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: distribution.map(d => ({ name: d.scoreRange, value: d.count })),
      label: { formatter: '{b}\n{d}%' },
    }],
  })
}

function fetchData() {
  pagination.page = 1
  fetchGrades()
  fetchStats()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.grade-student { display: flex; flex-direction: column; gap: 16px; }
.filter-card { margin-bottom: 0; }
.stats-row { margin-bottom: 0; }
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: bold; color: #409EFF; }
.stat-label { font-size: 13px; color: #909399; margin-top: 8px; }
.pagination-wrap { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
