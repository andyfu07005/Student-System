<template>
  <div class="course-roster">
    <el-card>
      <div class="toolbar">
        <span class="title">我的授课课程</span>
      </div>

      <el-table :data="myTeachingCourses" border stripe v-loading="loading" style="margin-top: 16px"
        @row-click="selectCourse" highlight-current-row>
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        <el-table-column prop="name" label="课程名称" width="200" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === '必修' ? 'danger' : 'info'" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column label="选课人数" width="120">
          <template #default="{ row }">
            {{ row.enrolledCount }} / {{ row.capacity || 30 }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card v-if="selectedCourseId" style="margin-top: 16px">
      <template #header>
        <span>选课学生名单 — {{ selectedCourseName }}</span>
      </template>

      <el-table :data="students" border stripe v-loading="studentLoading">
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column prop="studentName" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="enrolledAt" label="选课时间" width="170" />
      </el-table>

      <el-pagination
        v-model:current-page="studentPage" v-model:page-size="studentSize" :total="studentTotal"
        :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end" @change="fetchStudents"
      />
    </el-card>

    <el-empty v-else description="请从上方选择一门课程查看选课名单" style="margin-top: 40px" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { listTeachingCourses, listCourseStudents, type EnrolledStudent } from '@/api/courseEnrollment'

interface TeachingCourse {
  id: number
  courseNo: string
  name: string
  type: string
  semester?: string
  capacity: number
  enrolledCount: number
}

const loading = ref(false)
const myTeachingCourses = ref<TeachingCourse[]>([])

const selectedCourseId = ref<number | null>(null)
const selectedCourseName = ref('')
const students = ref<EnrolledStudent[]>([])
const studentLoading = ref(false)
const studentPage = ref(1)
const studentSize = ref(10)
const studentTotal = ref(0)

async function fetchMyCourses() {
  loading.value = true
  try {
    const res = await listTeachingCourses({ page: 1, size: 100 })
    myTeachingCourses.value = res.data?.records || []
  } finally { loading.value = false }
}

function selectCourse(row: TeachingCourse) {
  selectedCourseId.value = row.id
  selectedCourseName.value = row.name
  studentPage.value = 1
  fetchStudents()
}

async function fetchStudents() {
  if (!selectedCourseId.value) return
  studentLoading.value = true
  try {
    const res = await listCourseStudents(selectedCourseId.value, {
      page: studentPage.value,
      size: studentSize.value,
    })
    students.value = res.data?.records || []
    studentTotal.value = res.data?.total || 0
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '获取名单失败')
  } finally { studentLoading.value = false }
}

onMounted(fetchMyCourses)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; align-items: center; }
.title { font-size: 16px; font-weight: bold; }
</style>
