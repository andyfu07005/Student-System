<template>
  <div class="grade-query">
    <el-card>
      <div class="toolbar">
        <el-select v-model="filters.studentId" filterable clearable placeholder="选择学生" style="width: 180px" @change="handleStudentChange">
          <el-option v-for="item in students" :key="item.id" :label="`${item.studentNo} ${item.name}`" :value="item.id!" />
        </el-select>
        <el-select v-model="filters.courseId" filterable clearable placeholder="选择课程" style="width: 180px" @change="fetchRecords">
          <el-option v-for="item in courses" :key="item.id" :label="`${item.courseNo} ${item.name}`" :value="item.id!" />
        </el-select>
        <el-input v-model="filters.academicYear" placeholder="学年" clearable style="width: 130px" @keyup.enter="fetchRecords" />
        <el-select v-model="filters.semester" clearable placeholder="学期" style="width: 120px" @change="fetchRecords">
          <el-option label="第一学期" value="第一学期" />
          <el-option label="第二学期" value="第二学期" />
        </el-select>
        <el-segmented v-model="algorithm" :options="algorithmOptions" @change="handleAlgorithmChange" />
        <el-button type="primary" @click="fetchRecords">查询</el-button>
        <el-button type="success" @click="openCreate">录入成绩</el-button>
      </div>

      <div class="gpa-summary">
        <div class="metric">
          <span>累计 GPA</span>
          <strong>{{ summary?.cumulativeGpa ?? '--' }}</strong>
        </div>
        <div class="metric">
          <span>累计学分</span>
          <strong>{{ summary?.cumulativeCredits ?? '--' }}</strong>
        </div>
        <div class="semester-list">
          <el-tag v-for="item in summary?.semesters || []" :key="`${item.academicYear}-${item.semester}`" type="info">
            {{ item.academicYear }} {{ item.semester }} GPA {{ item.gpa }}
          </el-tag>
          <el-empty v-if="filters.studentId && summary && summary.semesters.length === 0" description="暂无 GPA 数据" :image-size="60" />
        </div>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top: 16px">
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="academicYear" label="学年" width="120" />
        <el-table-column prop="semester" label="学期" width="110" />
        <el-table-column prop="score" label="成绩" width="90" />
        <el-table-column prop="gradePoint" label="绩点" width="90" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该成绩?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无成绩记录" />
        </template>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @change="fetchRecords"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑成绩' : '录入成绩'" width="560px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="form.studentId" filterable placeholder="请选择学生" style="width: 100%">
            <el-option v-for="item in students" :key="item.id" :label="`${item.studentNo} ${item.name}`" :value="item.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" filterable placeholder="请选择课程" style="width: 100%">
            <el-option v-for="item in courses" :key="item.id" :label="`${item.courseNo} ${item.name}`" :value="item.id!" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学年" prop="academicYear">
              <el-input v-model="form.academicYear" placeholder="2025-2026" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学期" prop="semester">
              <el-select v-model="form.semester" style="width: 100%">
                <el-option label="第一学期" value="第一学期" />
                <el-option label="第二学期" value="第二学期" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="成绩" prop="score">
          <el-input-number v-model="form.score" :min="0" :max="100" :precision="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listStudents, type Student } from '@/api/student'
import { listCourses, type Course } from '@/api/course'
import {
  createGradeRecord,
  deleteGradeRecord,
  getGpaSummary,
  listGradeRecords,
  updateGradeRecord,
  type GpaAlgorithm,
  type GpaSummary,
  type GradeRecord,
} from '@/api/grade'

const algorithmOptions = [
  { label: '4.0', value: 'FOUR_POINT' },
  { label: '5.0', value: 'FIVE_POINT' },
]
const algorithm = ref<GpaAlgorithm>('FOUR_POINT')
const students = ref<Student[]>([])
const courses = ref<Course[]>([])
const tableData = ref<GradeRecord[]>([])
const summary = ref<GpaSummary | null>(null)
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref()
const page = ref(1)
const size = ref(10)
const total = ref(0)

const filters = ref<{ studentId?: number; courseId?: number; academicYear: string; semester: string }>({
  academicYear: '',
  semester: '',
})

const form = ref<GradeRecord>({
  studentId: undefined as unknown as number,
  courseId: undefined as unknown as number,
  academicYear: '',
  semester: '第一学期',
  score: 0,
})

const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  academicYear: [{ required: true, message: '请输入学年', trigger: 'blur' }],
  semester: [{ required: true, message: '请选择学期', trigger: 'change' }],
  score: [{ required: true, message: '请输入成绩', trigger: 'blur' }],
}

async function fetchOptions() {
  try {
    const [studentRes, courseRes] = await Promise.all([
      listStudents({ page: 1, size: 1000 }),
      listCourses({ page: 1, size: 1000 }),
    ])
    students.value = studentRes.data?.records || []
    courses.value = courseRes.data?.records || []
  } catch (err: any) {
    ElMessage.error(err?.message || '基础数据加载失败')
  }
}

async function fetchRecords() {
  loading.value = true
  try {
    const res = await listGradeRecords({
      ...filters.value,
      studentId: filters.value.studentId || undefined,
      courseId: filters.value.courseId || undefined,
      academicYear: filters.value.academicYear || undefined,
      semester: filters.value.semester || undefined,
      algorithm: algorithm.value,
      page: page.value,
      size: size.value,
    })
    tableData.value = res.records || []
    total.value = res.total || 0
    await fetchSummary()
  } catch (err: any) {
    ElMessage.error(err?.message || '成绩查询失败')
  } finally {
    loading.value = false
  }
}

async function fetchSummary() {
  if (!filters.value.studentId) {
    summary.value = null
    return
  }
  summary.value = await getGpaSummary({ studentId: filters.value.studentId, algorithm: algorithm.value })
}

function handleStudentChange() {
  page.value = 1
  fetchRecords()
}

function handleAlgorithmChange() {
  fetchRecords()
}

function openCreate() {
  editingId.value = null
  resetForm()
  if (filters.value.studentId) form.value.studentId = filters.value.studentId
  dialogVisible.value = true
}

function openEdit(row: GradeRecord) {
  editingId.value = row.id!
  form.value = { ...row }
  dialogVisible.value = true
}

function resetForm() {
  form.value = {
    studentId: undefined as unknown as number,
    courseId: undefined as unknown as number,
    academicYear: '',
    semester: '第一学期',
    score: 0,
  }
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await updateGradeRecord(editingId.value, form.value, algorithm.value)
      ElMessage.success('更新成功')
    } else {
      await createGradeRecord(form.value, algorithm.value)
      ElMessage.success('录入成功')
    }
    dialogVisible.value = false
    fetchRecords()
  } catch (err: any) {
    ElMessage.error(err?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id?: number) {
  if (!id) return
  try {
    await deleteGradeRecord(id)
    ElMessage.success('删除成功')
    fetchRecords()
  } catch (err: any) {
    ElMessage.error(err?.message || '删除失败')
  }
}

onMounted(async () => {
  await fetchOptions()
  fetchRecords()
})
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.toolbar .el-button--success { margin-left: auto; }
.gpa-summary { display: flex; gap: 16px; align-items: stretch; flex-wrap: wrap; margin-top: 16px; }
.metric { min-width: 120px; padding: 12px 14px; border: 1px solid #ebeef5; border-radius: 6px; background: #fafafa; }
.metric span { display: block; color: #606266; font-size: 13px; margin-bottom: 6px; }
.metric strong { font-size: 24px; color: #303133; }
.semester-list { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; min-height: 58px; flex: 1; }
</style>
