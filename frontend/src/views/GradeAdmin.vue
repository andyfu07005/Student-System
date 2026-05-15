<template>
  <div class="grade-admin">
    <el-card class="filter-card">
      <el-form :inline="true">
        <el-form-item label="学年">
          <el-select v-model="filters.academicYear" clearable placeholder="全部学年" @change="fetchAll">
            <el-option label="2024-2025" value="2024-2025" />
            <el-option label="2025-2026" value="2025-2026" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.totalCount || 0 }}</div>
          <div class="stat-label">总人次</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.avgScore || '-' }}</div>
          <div class="stat-label">全校平均分</div>
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
      <el-col :span="4">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.excellentCount || 0 }}</div>
          <div class="stat-label">优秀人数</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>全校成绩分布（饼图）</template>
          <div ref="pieChartRef" style="width:100%;height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>全校成绩分布（柱状图）</template>
          <div ref="barChartRef" style="width:100%;height:300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card>
          <template #header>各分数段人数统计</template>
          <div ref="detailBarRef" style="width:100%;height:300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { adminStatistics, type GradeStats } from '@/api/grade'

const stats = reactive<GradeStats>({})
const filters = reactive({ academicYear: '' })

const pieChartRef = ref<HTMLDivElement>()
const barChartRef = ref<HTMLDivElement>()
const detailBarRef = ref<HTMLDivElement>()
let pieInstance: echarts.ECharts | null = null
let barInstance: echarts.ECharts | null = null
let detailInstance: echarts.ECharts | null = null

async function fetchAll() {
  try {
    const data = await adminStatistics(filters.academicYear || undefined)
    Object.assign(stats, data)
    await nextTick()
    renderCharts(data.distribution || [])
  } catch {
    // handle error silently
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
      series: [{ type: 'bar', data: values, itemStyle: { color: '#409EFF' } }],
      grid: { bottom: 60 },
    })
  }

  if (detailBarRef.value) {
    if (!detailInstance) detailInstance = echarts.init(detailBarRef.value)
    detailInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['人数'] },
      xAxis: { type: 'category', data: names, axisLabel: { rotate: 15 } },
      yAxis: { type: 'value', name: '人数' },
      series: [{
        name: '人数',
        type: 'bar',
        data: values,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#83bff6' },
            { offset: 1, color: '#188df0' },
          ]),
        },
        label: { show: true, position: 'top' },
      }],
      grid: { bottom: 60 },
    })
  }
}

onMounted(() => {
  fetchAll()
})
</script>

<style scoped>
.grade-admin { display: flex; flex-direction: column; gap: 16px; }
.filter-card { margin-bottom: 0; }
.stats-row { margin-bottom: 0; }
.chart-row { margin-bottom: 0; }
.stat-card { text-align: center; }
.stat-value { font-size: 24px; font-weight: bold; color: #409EFF; }
.stat-label { font-size: 13px; color: #909399; margin-top: 8px; }
</style>
