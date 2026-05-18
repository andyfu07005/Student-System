<template>
  <div class="transcript-page">
    <el-card v-if="!transcriptData">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="个人成绩单" name="single">
          <div class="toolbar">
            <el-select v-model="selectedStudentId" placeholder="请选择学生" clearable filterable style="width: 260px">
              <el-option v-for="s in studentList" :key="s.id!" :label="`${s.name} (${s.studentNo})`" :value="s.id!" />
            </el-select>
            <el-input v-model="selectedSemester" placeholder="学期筛选(可选) 如: 2024-2025-1" clearable style="width: 220px" />
            <el-button type="primary" @click="loadSingle" :disabled="!selectedStudentId">
              查看成绩单
            </el-button>
            <el-button type="success" @click="downloadSinglePdf" :disabled="!selectedStudentId">
              导出PDF
            </el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="批量打印" name="batch">
          <div class="toolbar">
            <el-select v-model="selectedClassId" placeholder="请选择班级" clearable filterable style="width: 220px">
              <el-option v-for="c in classList" :key="c.id!" :label="c.name" :value="c.id!" />
            </el-select>
            <el-input v-model="batchSemester" placeholder="学期筛选(可选) 如: 2024-2025-1" clearable style="width: 220px" />
            <el-button type="primary" @click="downloadBatchPdf" :disabled="!selectedClassId">
              批量导出PDF
            </el-button>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <div v-if="transcriptData" class="transcript-container">
      <div class="transcript-actions no-print">
        <el-button @click="transcriptData = null">返回查询</el-button>
        <el-button type="primary" @click="downloadSinglePdf">导出PDF</el-button>
        <el-button type="success" @click="doPrint">打印</el-button>
      </div>

      <div id="transcript-print" class="transcript-doc">
        <div class="transcript-header">
          <h1>{{ transcriptData.schoolName }} 成绩单</h1>
        </div>

        <table class="info-table">
          <tr>
            <td class="label">学号</td>
            <td class="value">{{ transcriptData.studentNo }}</td>
            <td class="label">姓名</td>
            <td class="value">{{ transcriptData.studentName }}</td>
          </tr>
          <tr>
            <td class="label">性别</td>
            <td class="value">{{ transcriptData.gender }}</td>
            <td class="label">班级</td>
            <td class="value">{{ transcriptData.className }}</td>
          </tr>
          <tr>
            <td class="label">年级</td>
            <td class="value">{{ transcriptData.grade }}</td>
            <td class="label">专业</td>
            <td class="value">{{ transcriptData.major }}</td>
          </tr>
          <tr>
            <td class="label">入学日期</td>
            <td class="value">{{ transcriptData.enrollmentDate }}</td>
            <td class="label">学年</td>
            <td class="value">{{ transcriptData.academicYear }}</td>
          </tr>
        </table>

        <div v-for="sg in transcriptData.semesters" :key="sg.semester" class="semester-block">
          <h3 class="semester-title">学期: {{ sg.semester }}</h3>
          <table class="grade-table">
            <thead>
              <tr>
                <th>课程编号</th>
                <th>课程名称</th>
                <th style="width:60px">学分</th>
                <th style="width:60px">类型</th>
                <th style="width:70px">成绩</th>
                <th style="width:70px">绩点</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="cg in sg.courses" :key="cg.courseNo">
                <td>{{ cg.courseNo }}</td>
                <td>{{ cg.courseName }}</td>
                <td class="center">{{ cg.credit }}</td>
                <td class="center">{{ cg.courseType }}</td>
                <td class="center">{{ cg.score }}</td>
                <td class="center">{{ cg.gradePoint }}</td>
              </tr>
            </tbody>
          </table>
          <div class="semester-summary">
            学期学分: {{ sg.semesterCredits }} &nbsp;&nbsp; 学期绩点: {{ sg.semesterGpa }}
          </div>
        </div>

        <div class="transcript-footer">
          <p>总学分: {{ transcriptData.totalCredits }} &nbsp;&nbsp;&nbsp;&nbsp; 平均绩点(GPA): {{ transcriptData.totalGpa }}</p>
        </div>
      </div>
    </div>
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
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTranscript, getTranscriptPdfUrl, getBatchPdfUrl, type Transcript } from '@/api/transcript'
import { listStudents } from '@/api/student'
import type { Student } from '@/api/student'
import { listClasses } from '@/api/classInfo'

const activeTab = ref('single')
const selectedStudentId = ref<number | undefined>()
const selectedSemester = ref('')
const selectedClassId = ref<number | undefined>()
const batchSemester = ref('')
const transcriptData = ref<Transcript | null>(null)
const studentList = ref<Student[]>([])
const classList = ref<{ id: number; name: string }[]>([])

function handleTabChange() {
  transcriptData.value = null
}

async function loadSingle() {
  if (!selectedStudentId.value) return
  try {
    const res = await getTranscript(selectedStudentId.value, selectedSemester.value || undefined)
    transcriptData.value = res.data
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '加载失败')
  }
}

function downloadSinglePdf() {
  if (!selectedStudentId.value) return
  const url = getTranscriptPdfUrl(selectedStudentId.value, selectedSemester.value || undefined)
  window.open(url, '_blank')
}

function downloadBatchPdf() {
  if (!selectedClassId.value) return
  const url = getBatchPdfUrl(selectedClassId.value, batchSemester.value || undefined)
  window.open(url, '_blank')
}

function doPrint() {
  window.print()
}

async function loadOptions() {
  try {
    const [stuRes, clsRes] = await Promise.all([
      listStudents({ size: 500 }),
      listClasses({ size: 100 }),
    ])
    studentList.value = stuRes.data?.records || []
    classList.value = clsRes.data?.records || []
  } catch { /* ignore */ }
}

onMounted(loadOptions)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 10px;
}

.transcript-container {
  background: #fff;
  padding: 20px;
}

.transcript-actions {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.transcript-doc {
  max-width: 800px;
  margin: 0 auto;
  padding: 30px 40px;
  border: 1px solid #ddd;
}

.transcript-header {
  text-align: center;
  margin-bottom: 20px;
}

.transcript-header h1 {
  font-size: 22px;
  margin: 0;
}

.info-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.info-table td {
  border: 1px solid #ccc;
  padding: 6px 10px;
  font-size: 13px;
}

.info-table .label {
  background: #f5f5f5;
  width: 15%;
  font-weight: bold;
}

.info-table .value {
  width: 35%;
}

.semester-block {
  margin-bottom: 20px;
}

.semester-title {
  font-size: 15px;
  margin: 10px 0 6px;
  padding: 4px 8px;
  background: #f0f0f0;
}

.grade-table {
  width: 100%;
  border-collapse: collapse;
}

.grade-table th,
.grade-table td {
  border: 1px solid #ccc;
  padding: 5px 8px;
  font-size: 13px;
  text-align: left;
}

.grade-table th {
  background: #e8e8e8;
  text-align: center;
}

.grade-table .center {
  text-align: center;
}

.semester-summary {
  text-align: right;
  font-size: 13px;
  font-weight: bold;
  padding: 6px 0;
  color: #555;
}

.transcript-footer {
  text-align: right;
  font-size: 15px;
  font-weight: bold;
  padding-top: 10px;
  border-top: 2px solid #333;
  margin-top: 10px;
}

@media print {
  .no-print {
    display: none !important;
  }
  .transcript-doc {
    border: none;
    padding: 0;
    max-width: 100%;
  }
}
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
