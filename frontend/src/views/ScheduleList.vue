<template>
  <div class="schedule-list">
    <el-card>
      <div class="toolbar">
        <el-select v-model="filterDayOfWeek" placeholder="全部星期" clearable style="width: 120px" @change="fetchData">
          <el-option label="星期一" :value="1" />
          <el-option label="星期二" :value="2" />
          <el-option label="星期三" :value="3" />
          <el-option label="星期四" :value="4" />
          <el-option label="星期五" :value="5" />
          <el-option label="星期六" :value="6" />
          <el-option label="星期日" :value="7" />
        </el-select>
        <el-input v-model="filterClassroom" placeholder="搜索教室" clearable style="width: 160px" @keyup.enter="fetchData" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button type="success" @click="openCreate">新增排课</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top: 16px">
        <el-table-column prop="courseName" label="课程名称" width="160" show-overflow-tooltip />
        <el-table-column prop="teacherName" label="授课教师" width="100" />
        <el-table-column prop="classroom" label="上课地点" width="140" />
        <el-table-column label="星期" width="80">
          <template #default="{ row }">
            {{ weekDayLabel(row.dayOfWeek) }}
          </template>
        </el-table-column>
        <el-table-column label="时段" width="140">
          <template #default="{ row }">
            {{ row.startTime }} ~ {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容纳人数" width="90" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该排课?" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑排课' : '新增排课'" width="560px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="form.courseId" filterable placeholder="请选择课程" style="width: 100%">
            <el-option v-for="c in courseOptions" :key="c.id" :label="c.name + ' (' + c.courseNo + ')'" :value="c.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="form.teacherId" filterable placeholder="请选择教师" style="width: 100%">
            <el-option v-for="t in teacherOptions" :key="t.id" :label="t.realName || t.username" :value="t.id!" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="星期" prop="dayOfWeek">
              <el-select v-model="form.dayOfWeek" style="width: 100%">
                <el-option v-for="d in weekOptions" :key="d.value" :label="d.label" :value="d.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上课地点" prop="classroom">
              <el-input v-model="form.classroom" placeholder="如: 教学楼A101" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-time-picker v-model="form.startTime" format="HH:mm" value-format="HH:mm:ss" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-time-picker v-model="form.endTime" format="HH:mm" value-format="HH:mm:ss" placeholder="选择时间" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="容纳人数">
          <el-input-number v-model="form.capacity" :min="1" :max="500" style="width: 100%" />
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
import { listSchedules, createSchedule, updateSchedule, deleteSchedule, type Schedule, type ScheduleDTO } from '@/api/schedule'
import { listCourses, type Course } from '@/api/course'
import { getUserList, type User } from '@/api/user'

const filterDayOfWeek = ref<number | null>(null)
const filterClassroom = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref<Schedule[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref()
const form = ref<ScheduleDTO>({
  courseId: 0, teacherId: 0, classroom: '', dayOfWeek: 1,
  startTime: '08:00:00', endTime: '09:40:00', capacity: 60,
})

const courseOptions = ref<Course[]>([])
const teacherOptions = ref<User[]>([])

const weekOptions = [
  { label: '星期一', value: 1 }, { label: '星期二', value: 2 }, { label: '星期三', value: 3 },
  { label: '星期四', value: 4 }, { label: '星期五', value: 5 }, { label: '星期六', value: 6 },
  { label: '星期日', value: 7 },
]

function weekDayLabel(day: number) {
  const m: Record<number, string> = { 1: '一', 2: '二', 3: '三', 4: '四', 5: '五', 6: '六', 7: '日' }
  return '星期' + (m[day] || day)
}

const rules = {
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择教师', trigger: 'change' }],
  classroom: [{ required: true, message: '请输入上课地点', trigger: 'blur' }],
  dayOfWeek: [{ required: true, message: '请选择星期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listSchedules({
      dayOfWeek: filterDayOfWeek.value || undefined,
      classroom: filterClassroom.value || undefined,
      page: page.value, size: size.value,
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function loadOptions() {
  try {
    const [cRes, tRes] = await Promise.all([
      listCourses({ page: 1, size: 200 }),
      getUserList({ page: 1, size: 200, roleCode: 'TEACHER' }),
    ])
    courseOptions.value = cRes.data?.records || []
    teacherOptions.value = tRes.data.data?.records || []
  } catch { /* ignore */ }
}

function openCreate() { editingId.value = null; resetForm(); dialogVisible.value = true }
function openEdit(row: Schedule) {
  editingId.value = row.id!
  form.value = {
    courseId: row.courseId, teacherId: row.teacherId, classroom: row.classroom,
    dayOfWeek: row.dayOfWeek, startTime: row.startTime, endTime: row.endTime,
    capacity: row.capacity,
  }
  dialogVisible.value = true
}
function resetForm() {
  form.value = { courseId: 0, teacherId: 0, classroom: '', dayOfWeek: 1, startTime: '08:00:00', endTime: '09:40:00', capacity: 60 }
  editingId.value = null
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await updateSchedule(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createSchedule(form.value)
      ElMessage.success('排课成功')
    }
    dialogVisible.value = false; fetchData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '操作失败')
  } finally { saving.value = false }
}

async function handleDelete(id: number) {
  try {
    await deleteSchedule(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '删除失败')
  }
}

onMounted(() => { fetchData(); loadOptions() })
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.toolbar .el-button--success { margin-left: auto; }
</style>
