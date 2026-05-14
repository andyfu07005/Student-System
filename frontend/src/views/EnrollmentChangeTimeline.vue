<template>
  <div class="timeline-container">
    <el-card>
      <div class="header">
        <el-button @click="goBack" :icon="'Back'">返回列表</el-button>
        <h2>学生 #{{ studentId }} 学籍异动时间线</h2>
      </div>
    </el-card>

    <el-card style="margin-top: 16px" v-loading="loading">
      <el-empty v-if="!loading && timelineData.length === 0" description="暂无学籍异动记录" />

      <el-timeline v-else>
        <el-timeline-item
          v-for="item in timelineData"
          :key="item.id"
          :timestamp="item.changeDate"
          placement="top"
          :type="timelineItemType(item)"
          :hollow="!!item.correctedRecordId"
        >
          <el-card :class="{ 'correction-card': !!item.correctedRecordId }">
            <template #header>
              <div class="card-header">
                <el-tag :type="changeTypeTag(item.changeType)">{{ changeTypeLabel(item.changeType) }}</el-tag>
                <span class="status-flow">{{ item.previousStatus }} → {{ item.newStatus }}</span>
                <el-tag v-if="item.correctedRecordId" type="warning" size="small">更正记录</el-tag>
                <el-tag v-else type="success" size="small">原始记录</el-tag>
              </div>
            </template>

            <div class="card-body">
              <div class="info-row">
                <span class="label">记录ID：</span>
                <span>{{ item.id }}</span>
              </div>
              <div v-if="item.correctedRecordId" class="info-row">
                <span class="label">关联更正：</span>
                <span>更正了记录 #{{ item.correctedRecordId }}</span>
              </div>
              <div v-if="item.correctionReason" class="info-row">
                <span class="label">更正原因：</span>
                <span class="correction-reason">{{ item.correctionReason }}</span>
              </div>
              <div v-if="item.previousClassId || item.newClassId" class="info-row">
                <span class="label">班级变更：</span>
                <span v-if="item.previousClassId">班级 #{{ item.previousClassId }}</span>
                <span v-if="item.previousClassId && item.newClassId"> → </span>
                <span v-if="item.newClassId">班级 #{{ item.newClassId }}</span>
              </div>
              <div class="info-row">
                <span class="label">原因：</span>
                <span>{{ item.reason }}</span>
              </div>
              <div class="info-row">
                <span class="label">操作人ID：</span>
                <span>{{ item.operatorId }}</span>
              </div>
              <div class="info-row">
                <span class="label">记录时间：</span>
                <span>{{ item.createdAt }}</span>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTimelineByStudentId, type EnrollmentChange } from '@/api/enrollmentChange'

const route = useRoute()
const router = useRouter()
const studentId = Number(route.params.studentId)

const loading = ref(false)
const timelineData = ref<EnrollmentChange[]>([])

const changeTypeMap: Record<string, string> = {
  SUSPENSION: '休学',
  WITHDRAWAL: '退学',
  TRANSFER: '转班',
  GRADUATION: '毕业'
}

const changeTypeTagMap: Record<string, string> = {
  SUSPENSION: 'warning',
  WITHDRAWAL: 'danger',
  TRANSFER: 'info',
  GRADUATION: 'success'
}

const timelineTypeMap: Record<string, string> = {
  SUSPENSION: 'warning',
  WITHDRAWAL: 'danger',
  TRANSFER: 'primary',
  GRADUATION: 'success'
}

function changeTypeLabel(type: string) {
  return changeTypeMap[type] || type
}

function changeTypeTag(type: string) {
  return changeTypeTagMap[type] || 'info'
}

function timelineItemType(item: EnrollmentChange) {
  return timelineTypeMap[item.changeType] || 'primary'
}

function goBack() {
  router.push('/enrollment-changes')
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getTimelineByStudentId(studentId)
    timelineData.value = res.data
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.timeline-container {
  padding: 20px;
}
.header {
  display: flex;
  align-items: center;
  gap: 16px;
}
.header h2 {
  margin: 0;
}
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.status-flow {
  font-weight: bold;
  color: #409eff;
}
.card-body {
  font-size: 14px;
  line-height: 2;
}
.info-row {
  display: flex;
}
.label {
  color: #909399;
  min-width: 90px;
}
.correction-card {
  border-left: 3px solid #e6a23c;
}
.correction-reason {
  color: #e6a23c;
  font-weight: bold;
}
</style>
