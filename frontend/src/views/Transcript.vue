<template>
  <div class="transcript">
    <el-card>
      <div class="toolbar">
        <el-select v-model="studentId" filterable placeholder="选择学生" style="width: 220px" @change="fetchTranscript">
          <el-option v-for="item in students" :key="item.id" :label="`${item.studentNo} ${item.name}`" :value="item.id!" />
        </el-select>
        <el-segmented v-model="algorithm" :options="algorithmOptions" @change="fetchTranscript" />
        <el-button type="primary" @click="fetchTranscript">刷新</el-button>
      </div>

      <div class="headline">
        <div>
          <h2>{{ summary ? `${summary.studentName} 成绩单` : '成绩单' }}</h2>
          <p>{{ summary ? `${summary.studentNo} · ${algorithm === 'FOUR_POINT' ? '4.0 算法' : '5.0 算法'}` : '请选择学生查看成绩单' }}</p>
        </div>
        <div class="metrics">
          <div class="metric">
            <span>累计 GPA</span>
            <strong>{{ summary?.cumulativeGpa ?? '--' }}</strong>
          </div>
          <div class="metric">
            <span>累计学分</span>
            <strong>{{ summary?.cumulativeCredits ?? '--' }}</strong>
          </div>
        </div>
      </div>

      <el-table :data="records" border stripe v-loading="loading" style="margin-top: 16px">
        <el-table-column prop="academicYear" label="学年" width="120" />
        <el-table-column prop="semester" label="学期" width="110" />
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="score" label="成绩" width="90" />
        <el-table-column prop="gradePoint" label="绩点" width="90" />
        <template #empty>
          <el-empty description="暂无成绩单数据" />
        </template>
      </el-table>

      <el-table :data="summary?.semesters || []" border stripe style="margin-top: 16px">
        <el-table-column prop="academicYear" label="学年" />
        <el-table-column prop="semester" label="学期" />
        <el-table-column prop="credits" label="学期学分" />
        <el-table-column prop="gpa" label="学期 GPA" />
        <template #empty>
          <el-empty description="暂无学期 GPA" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listStudents, type Student } from '@/api/student'
import {
  getGpaSummary,
  listGradeRecords,
  type GpaAlgorithm,
  type GpaSummary,
  type GradeRecord,
} from '@/api/grade'

const algorithmOptions = [
  { label: '4.0', value: 'FOUR_POINT' },
  { label: '5.0', value: 'FIVE_POINT' },
]
const students = ref<Student[]>([])
const studentId = ref<number>()
const algorithm = ref<GpaAlgorithm>('FOUR_POINT')
const summary = ref<GpaSummary | null>(null)
const records = ref<GradeRecord[]>([])
const loading = ref(false)

async function fetchStudents() {
  try {
    const res = await listStudents({ page: 1, size: 1000 })
    students.value = res.data?.records || []
  } catch (err: any) {
    ElMessage.error(err?.message || '学生数据加载失败')
  }
}

async function fetchTranscript() {
  if (!studentId.value) {
    summary.value = null
    records.value = []
    return
  }
  loading.value = true
  try {
    const [summaryRes, recordRes] = await Promise.all([
      getGpaSummary({ studentId: studentId.value, algorithm: algorithm.value }),
      listGradeRecords({ studentId: studentId.value, algorithm: algorithm.value, page: 1, size: 500 }),
    ])
    summary.value = summaryRes
    records.value = recordRes.records || []
  } catch (err: any) {
    ElMessage.error(err?.message || '成绩单加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchStudents)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.headline { display: flex; justify-content: space-between; gap: 16px; align-items: flex-start; margin-top: 18px; }
.headline h2 { margin: 0 0 6px; font-size: 22px; color: #303133; }
.headline p { margin: 0; color: #606266; }
.metrics { display: flex; gap: 12px; }
.metric { min-width: 120px; padding: 12px 14px; border: 1px solid #ebeef5; border-radius: 6px; background: #fafafa; }
.metric span { display: block; color: #606266; font-size: 13px; margin-bottom: 6px; }
.metric strong { font-size: 24px; color: #303133; }
</style>
