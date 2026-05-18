<template>
  <div class="schedule-view">
    <!-- 顶部导航 -->
    <div class="schedule-header">
      <el-button :icon="ArrowLeft" circle @click="prevWeek" />
      <h3>{{ schedule.weekLabel }}</h3>
      <el-button :icon="ArrowRight" circle @click="nextWeek" />
      <el-tag type="info" style="margin-left: 12px">{{ roleLabel }}</el-tag>
    </div>

    <!-- 课表格 -->
    <el-card shadow="never" v-loading="loading">
      <div v-if="!loading && timeSlots.length === 0" class="empty-hint">
        <el-empty description="暂无课表数据" />
      </div>
      <div v-else class="grid-wrapper">
        <table class="schedule-table">
          <thead>
            <tr>
              <th class="time-col">时间</th>
              <th v-for="(day, idx) in dayHeaders" :key="idx" :class="{ today: day.isToday }">
                <div class="day-label">{{ day.name }}</div>
                <div class="day-date">{{ day.date }}</div>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="slot in timeSlots" :key="slot.key">
              <td class="time-col">
                <div class="time-label">{{ slot.label }}</div>
              </td>
              <td
                v-for="dayIdx in 7"
                :key="dayIdx"
                :class="{ today: dayHeaders[dayIdx - 1]?.isToday }"
              >
                <div
                  v-if="slot.cells[dayIdx]"
                  class="course-card"
                  :style="{ backgroundColor: slot.cells[dayIdx].color }"
                >
                  <div class="course-name">{{ slot.cells[dayIdx].courseName }}</div>
                  <div class="course-info">{{ slot.cells[dayIdx].classroom }}</div>
                  <div class="course-info">{{ slot.cells[dayIdx].teacherName }}</div>
                  <div class="course-info course-no">{{ slot.cells[dayIdx].courseNo }}</div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getWeekSchedule, type WeekSchedule } from '@/api/schedule'

const DAY_NAMES = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const COLORS = ['#e8f5e9', '#e3f2fd', '#fff3e0', '#fce4ec', '#e0f2f1', '#f3e5f5', '#e8eaf6', '#fff8e1']

const loading = ref(false)
const currentDate = ref(new Date())
const schedule = ref<WeekSchedule>({
  weekLabel: '',
  weekStart: '',
  weekEnd: '',
  items: [],
})

const role = computed(() => localStorage.getItem('role') || 'STUDENT')
const roleLabel = computed(() => {
  const map: Record<string, string> = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
  return map[role.value] || '未知'
})

const dayHeaders = computed(() => {
  const start = new Date(currentDate.value)
  const dayOfWeek = start.getDay()
  const diff = dayOfWeek === 0 ? -6 : 1 - dayOfWeek
  const monday = new Date(start)
  monday.setDate(start.getDate() + diff)

  const today = new Date()
  const todayStr = today.toISOString().slice(0, 10)

  return DAY_NAMES.map((name, i) => {
    const d = new Date(monday)
    d.setDate(monday.getDate() + i)
    const dateStr = d.toISOString().slice(0, 10)
    return {
      name,
      date: `${d.getMonth() + 1}/${d.getDate()}`,
      fullDate: dateStr,
      isToday: dateStr === todayStr,
    }
  })
})

interface CellData {
  courseName: string
  teacherName: string
  classroom: string
  courseNo: string
  color: string
}

interface TimeSlot {
  key: string
  label: string
  cells: Record<number, CellData>
}

const timeSlots = computed(() => {
  const items = schedule.value.items
  if (items.length === 0) return []

  const slotMap = new Map<string, Record<number, CellData>>()
  let colorIdx = 0

  for (const item of items) {
    const key = `${item.startTime}-${item.endTime}`
    if (!slotMap.has(key)) {
      slotMap.set(key, {})
    }

    const cells = slotMap.get(key)!
    cells[item.dayOfWeek] = {
      courseName: item.courseName,
      teacherName: item.teacherName,
      classroom: item.classroom,
      courseNo: item.courseNo,
      color: COLORS[colorIdx % COLORS.length],
    }
    colorIdx++
  }

  const slots: TimeSlot[] = []
  for (const [key, cells] of slotMap) {
    const [start, end] = key.split('-')
    slots.push({
      key,
      label: `${start.slice(0, 5)} ~ ${end.slice(0, 5)}`,
      cells,
    })
  }
  slots.sort((a, b) => a.key.localeCompare(b.key))
  return slots
})

function changeWeek(offset: number) {
  const d = new Date(currentDate.value)
  d.setDate(d.getDate() + offset * 7)
  currentDate.value = d
  fetchData()
}

function prevWeek() { changeWeek(-1) }
function nextWeek() { changeWeek(1) }

async function fetchData() {
  loading.value = true
  try {
    const dateStr = currentDate.value.toISOString().slice(0, 10)
    const res = await getWeekSchedule(dateStr)
    schedule.value = res
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.schedule-view {
  max-width: 1200px;
  margin: 0 auto;
}

.schedule-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
}

.schedule-header h3 {
  margin: 0;
  font-size: 16px;
  min-width: 260px;
  text-align: center;
}

.empty-hint {
  padding: 60px 0;
}

.grid-wrapper {
  overflow-x: auto;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  min-width: 800px;
}

.schedule-table th,
.schedule-table td {
  border: 1px solid #ebeef5;
  padding: 6px;
  vertical-align: top;
  height: 90px;
  width: 12%;
}

.schedule-table th {
  background: #f5f7fa;
  text-align: center;
  height: auto;
  padding: 10px 6px;
}

.schedule-table .time-col {
  width: 110px;
  text-align: center;
  background: #fafafa;
}

.day-label {
  font-weight: 600;
  font-size: 14px;
}

.day-date {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.schedule-table th.today {
  background: #ecf5ff;
}

.schedule-table th.today .day-label {
  color: #409eff;
}

.schedule-table td.today {
  background: #fafcff;
}

.time-label {
  font-size: 12px;
  color: #606266;
  white-space: nowrap;
}

.course-card {
  border-radius: 4px;
  padding: 6px 8px;
  font-size: 12px;
  line-height: 1.6;
  height: 100%;
  min-height: 70px;
}

.course-name {
  font-weight: 600;
  font-size: 13px;
  color: #303133;
  margin-bottom: 2px;
}

.course-info {
  color: #606266;
  font-size: 11px;
}

.course-no {
  color: #909399;
}
</style>
