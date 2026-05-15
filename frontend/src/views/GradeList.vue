<template>
  <div class="grade-list">
    <el-card>
      <div class="toolbar">
        <el-input v-model="searchKeyword" placeholder="搜索学生姓名/学号" clearable style="width: 200px" @keyup.enter="fetchData" />
        <el-select v-model="filterStudentId" placeholder="筛选学生" clearable filterable style="width: 180px" @change="fetchData">
          <el-option v-for="s in studentList" :key="s.id!" :label="`${s.name} (${s.studentNo})`" :value="s.id!" />
        </el-select>
        <el-select v-model="filterCourseId" placeholder="筛选课程" clearable filterable style="width: 160px" @change="fetchData">
          <el-option v-for="c in courseList" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-input v-model="filterSemester" placeholder="学期 如: 2024-2025-1" clearable style="width: 180px" @keyup.enter="fetchData" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button type="success" @click="openCreate">录入成绩</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top: 16px">
        <el-table-column prop="studentName" label="学生姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="courseName" label="课程" width="140" />
        <el-table-column prop="courseType" label="课程类型" width="80" />
        <el-table-column prop="credit" label="学分" width="60" />
        <el-table-column prop="score" label="成绩" width="80">
          <template #default="{ row }">
            <el-tag :type="scoreType(row.score)" size="small">{{ row.score }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column prop="examType" label="考试类型" width="80" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该成绩?" @confirm="handleDelete(row.id!)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @change="fetchData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑成绩' : '录入成绩'" width="520px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="form.studentId" style="width: 100%" filterable placeholder="请选择学生">
            <el-option v-for="s in studentList" :key="s.id!" :label="`${s.name} (${s.studentNo})`" :value="s.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" style="width: 100%" filterable placeholder="请选择课程">
            <el-option v-for="c in courseList" :key="c.id!" :label="`${c.name} (${c.courseNo}, ${c.credit}学分)`" :value="c.id!" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="成绩" prop="score">
              <el-input-number v-model="form.score" :min="0" :max="100" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="考试类型" prop="examType">
              <el-select v-model="form.examType" style="width: 100%">
                <el-option label="期末" value="期末" />
                <el-option label="期中" value="期中" />
                <el-option label="补考" value="补考" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学期" prop="semester">
              <el-input v-model="form.semester" placeholder="如: 2024-2025-1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学年" prop="academicYear">
              <el-input v-model="form.academicYear" placeholder="如: 2024-2025" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remarks" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listGrades, createGrade, updateGrade, deleteGrade, type Grade } from '@/api/grade'
import { listStudents } from '@/api/student'
import type { Student } from '@/api/student'
import { listCourses } from '@/api/course'
import type { Course } from '@/api/course'

const searchKeyword = ref('')
const filterStudentId = ref<number | undefined>()
const filterCourseId = ref<number | undefined>()
const filterSemester = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref<Grade[]>([])
const loading = ref(false)
const studentList = ref<Student[]>([])
const courseList = ref<Course[]>([])

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref()
const form = ref<Grade>({
  studentId: 0,
  courseId: 0,
  score: 0,
  semester: '',
  academicYear: '',
  examType: '期末',
  remarks: '',
})

const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  score: [{ required: true, message: '请输入成绩', trigger: 'blur' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }],
  academicYear: [{ required: true, message: '请输入学年', trigger: 'blur' }],
}

function scoreType(score: number) {
  if (score >= 90) return 'success'
  if (score >= 80) return ''
  if (score >= 60) return 'warning'
  return 'danger'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listGrades({
      keyword: searchKeyword.value,
      studentId: filterStudentId.value,
      courseId: filterCourseId.value,
      semester: filterSemester.value || undefined,
      page: page.value,
      size: size.value,
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadOptions() {
  try {
    const [stuRes, courseRes] = await Promise.all([
      listStudents({ size: 500 }),
      listCourses({ size: 500 }),
    ])
    studentList.value = stuRes.data?.records || []
    courseList.value = courseRes.data?.records || []
  } catch { /* ignore */ }
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: Grade) {
  editingId.value = row.id!
  form.value = { ...row }
  dialogVisible.value = true
}

function resetForm() {
  form.value = {
    studentId: 0,
    courseId: 0,
    score: 0,
    semester: '',
    academicYear: '',
    examType: '期末',
    remarks: '',
  }
  editingId.value = null
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (editingId.value) {
      await updateGrade(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createGrade(form.value)
      ElMessage.success('录入成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await deleteGrade(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '删除失败')
  }
}

onMounted(() => {
  loadOptions()
  fetchData()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}
</style>
