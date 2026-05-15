<template>
  <div class="course-selection">
    <el-tabs v-model="activeTab" @tab-change="tabChange">
      <el-tab-pane label="可选课程" name="available">
        <el-card>
          <div class="toolbar">
            <el-input v-model="searchKeyword" placeholder="搜索课程名称/编号" clearable style="width: 220px" @keyup.enter="fetchAvailable" />
            <el-select v-model="filterType" placeholder="全部类型" clearable style="width: 120px" @change="fetchAvailable">
              <el-option label="必修" value="必修" />
              <el-option label="选修" value="选修" />
            </el-select>
            <el-button type="primary" @click="fetchAvailable">搜索</el-button>
          </div>

          <el-table :data="availableCourses" border stripe v-loading="loading" style="margin-top: 16px">
            <el-table-column prop="courseNo" label="课程编号" width="120" />
            <el-table-column prop="name" label="课程名称" width="180" />
            <el-table-column prop="teacherName" label="授课教师" width="100" />
            <el-table-column prop="credit" label="学分" width="60" />
            <el-table-column prop="hours" label="学时" width="60" />
            <el-table-column prop="type" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="row.type === '必修' ? 'danger' : 'info'" size="small">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="semester" label="学期" width="120" />
            <el-table-column label="容量" width="120">
              <template #default="{ row }">
                <span :class="{ 'text-danger': row.enrolledCount >= row.capacity }">
                  {{ row.enrolledCount }} / {{ row.capacity }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="major" label="所属专业" width="120" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button
                  size="small"
                  type="success"
                  :disabled="row.enrolledCount >= row.capacity"
                  @click="handleEnroll(row)"
                  :loading="enrollingId === row.id"
                >
                  {{ row.enrolledCount >= row.capacity ? '已满' : '选课' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="availPage" v-model:page-size="availSize" :total="availTotal"
            :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
            style="margin-top: 16px; justify-content: flex-end" @change="fetchAvailable"
          />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="已选课程" name="my">
        <el-card>
          <el-table :data="myCourses" border stripe v-loading="myLoading">
            <el-table-column prop="courseNo" label="课程编号" width="120" />
            <el-table-column prop="courseName" label="课程名称" width="180" />
            <el-table-column prop="teacherName" label="授课教师" width="100" />
            <el-table-column prop="type" label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="row.type === '必修' ? 'danger' : 'info'" size="small">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="semester" label="学期" width="120" />
            <el-table-column label="容量" width="120">
              <template #default="{ row }">
                {{ row.enrolledCount }} / {{ row.capacity }}
              </template>
            </el-table-column>
            <el-table-column prop="enrolledAt" label="选课时间" width="170" />
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-popconfirm title="确定要退选该课程吗?" @confirm="handleDrop(row)">
                  <template #reference>
                    <el-button size="small" type="danger">退选</el-button>
                  </template>
                </el-popconfirm>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="myPage" v-model:page-size="mySize" :total="myTotal"
            :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next"
            style="margin-top: 16px; justify-content: flex-end" @change="fetchMyCourses"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  listAvailableCourses, listMyCourses, enroll, dropCourse,
  type AvailableCourse, type EnrolledCourse,
} from '@/api/courseEnrollment'

const activeTab = ref('available')

const searchKeyword = ref('')
const filterType = ref('')
const availPage = ref(1)
const availSize = ref(10)
const availTotal = ref(0)
const availableCourses = ref<AvailableCourse[]>([])
const loading = ref(false)
const enrollingId = ref<number | null>(null)

const myPage = ref(1)
const mySize = ref(10)
const myTotal = ref(0)
const myCourses = ref<EnrolledCourse[]>([])
const myLoading = ref(false)

async function fetchAvailable() {
  loading.value = true
  try {
    const res = await listAvailableCourses({
      keyword: searchKeyword.value,
      type: filterType.value || undefined,
      page: availPage.value,
      size: availSize.value,
    })
    availableCourses.value = res.data?.records || []
    availTotal.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function fetchMyCourses() {
  myLoading.value = true
  try {
    const res = await listMyCourses({
      page: myPage.value,
      size: mySize.value,
    })
    myCourses.value = res.data?.records || []
    myTotal.value = res.data?.total || 0
  } finally { myLoading.value = false }
}

async function handleEnroll(row: AvailableCourse) {
  enrollingId.value = row.id
  try {
    await enroll(row.id)
    ElMessage.success('选课成功')
    fetchAvailable()
    fetchMyCourses()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '选课失败')
  } finally { enrollingId.value = null }
}

async function handleDrop(row: EnrolledCourse) {
  try {
    await dropCourse(row.courseId)
    ElMessage.success('退选成功')
    fetchAvailable()
    fetchMyCourses()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '退选失败')
  }
}

function tabChange(name: string) {
  if (name === 'available') fetchAvailable()
  else fetchMyCourses()
}

onMounted(fetchAvailable)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.text-danger { color: #f56c6c; font-weight: bold; }
</style>
