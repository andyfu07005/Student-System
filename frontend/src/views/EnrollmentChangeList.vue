<template>
  <div class="enrollment-change-container">
    <el-card class="header-card">
      <div class="header">
        <h2>学籍异动管理</h2>
        <el-button type="primary" @click="showCreateDialog">新增异动记录</el-button>
      </div>
    </el-card>

    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="学生ID">
          <el-input v-model="searchForm.studentId" placeholder="输入学生ID" clearable />
        </el-form-item>
        <el-form-item label="异动类型">
          <el-select v-model="searchForm.changeType" placeholder="全部" clearable style="width: 140px">
            <el-option label="休学" value="SUSPENSION" />
            <el-option label="退学" value="WITHDRAWAL" />
            <el-option label="转班" value="TRANSFER" />
            <el-option label="毕业" value="GRADUATION" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentId" label="学生ID" width="100" />
        <el-table-column label="异动类型" width="100">
          <template #default="{ row }">
            <el-tag :type="changeTypeTag(row.changeType)">{{ changeTypeLabel(row.changeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="previousStatus" label="异动前状态" width="110" />
        <el-table-column prop="newStatus" label="异动后状态" width="110" />
        <el-table-column prop="changeDate" label="异动日期" width="120" />
        <el-table-column prop="reason" label="原因" min-width="180" show-overflow-tooltip />
        <el-table-column label="更正标记" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.correctedRecordId" type="warning" size="small">更正记录</el-tag>
            <el-tag v-else type="success" size="small">原始记录</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewTimeline(row.studentId)">时间线</el-button>
            <el-button link type="warning" @click="showCorrectDialog(row)" :disabled="!!row.correctedRecordId">更正</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增异动记录对话框 -->
    <el-dialog v-model="createDialogVisible" title="新增学籍异动记录" width="600px" @close="resetCreateForm">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="110px">
        <el-form-item label="学生ID" prop="studentId">
          <el-input-number v-model="createForm.studentId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="异动类型" prop="changeType">
          <el-select v-model="createForm.changeType" placeholder="请选择" style="width: 100%" @change="onChangeType">
            <el-option label="休学" value="SUSPENSION" />
            <el-option label="退学" value="WITHDRAWAL" />
            <el-option label="转班" value="TRANSFER" />
            <el-option label="毕业" value="GRADUATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="异动前状态" prop="previousStatus">
          <el-select v-model="createForm.previousStatus" placeholder="请选择" style="width: 100%">
            <el-option label="在读" value="在读" />
            <el-option label="休学" value="休学" />
            <el-option label="退学" value="退学" />
            <el-option label="毕业" value="毕业" />
          </el-select>
        </el-form-item>
        <el-form-item label="异动后状态" prop="newStatus">
          <el-select v-model="createForm.newStatus" placeholder="请选择" style="width: 100%">
            <el-option label="在读" value="在读" />
            <el-option label="休学" value="休学" />
            <el-option label="退学" value="退学" />
            <el-option label="毕业" value="毕业" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="createForm.changeType === 'TRANSFER'" label="原班级ID">
          <el-input-number v-model="createForm.previousClassId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="createForm.changeType === 'TRANSFER'" label="新班级ID">
          <el-input-number v-model="createForm.newClassId" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="异动日期" prop="changeDate">
          <el-date-picker v-model="createForm.changeDate" type="date" placeholder="选择日期" style="width: 100%" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="异动原因" prop="reason">
          <el-input v-model="createForm.reason" type="textarea" :rows="3" placeholder="请输入异动原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreate" :loading="createLoading">确认</el-button>
      </template>
    </el-dialog>

    <!-- 更正记录对话框 -->
    <el-dialog v-model="correctDialogVisible" title="更正学籍异动记录" width="600px" @close="resetCorrectForm">
      <el-alert type="info" :closable="false" style="margin-bottom: 16px">
        正在更正记录 #{{ correctTarget?.id }}（{{ changeTypeLabel(correctTarget?.changeType || '') }}），更正后将追加一条新的更正记录，原记录保留。
      </el-alert>
      <el-form :model="correctForm" :rules="correctRules" ref="correctFormRef" label-width="110px">
        <el-form-item label="更正原因" prop="correctionReason">
          <el-input v-model="correctForm.correctionReason" type="textarea" :rows="2" placeholder="请说明更正原因" />
        </el-form-item>
        <el-form-item label="异动类型" prop="changeType">
          <el-select v-model="correctForm.changeType" placeholder="请选择" style="width: 100%">
            <el-option label="休学" value="SUSPENSION" />
            <el-option label="退学" value="WITHDRAWAL" />
            <el-option label="转班" value="TRANSFER" />
            <el-option label="毕业" value="GRADUATION" />
          </el-select>
        </el-form-item>
        <el-form-item label="异动前状态" prop="previousStatus">
          <el-select v-model="correctForm.previousStatus" placeholder="请选择" style="width: 100%">
            <el-option label="在读" value="在读" />
            <el-option label="休学" value="休学" />
            <el-option label="退学" value="退学" />
            <el-option label="毕业" value="毕业" />
          </el-select>
        </el-form-item>
        <el-form-item label="异动后状态" prop="newStatus">
          <el-select v-model="correctForm.newStatus" placeholder="请选择" style="width: 100%">
            <el-option label="在读" value="在读" />
            <el-option label="休学" value="休学" />
            <el-option label="退学" value="退学" />
            <el-option label="毕业" value="毕业" />
          </el-select>
        </el-form-item>
        <el-form-item label="异动原因" prop="reason">
          <el-input v-model="correctForm.reason" type="textarea" :rows="3" placeholder="请输入纠正后的异动原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="correctDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCorrect" :loading="correctLoading">确认更正</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  pageEnrollmentChanges,
  createEnrollmentChange,
  correctEnrollmentChange,
  type EnrollmentChange
} from '@/api/enrollmentChange'

const router = useRouter()

const loading = ref(false)
const tableData = ref<EnrollmentChange[]>([])

const searchForm = reactive({
  studentId: '',
  changeType: ''
})

const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})

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

function changeTypeLabel(type: string) {
  return changeTypeMap[type] || type
}

function changeTypeTag(type: string) {
  return changeTypeTagMap[type] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await pageEnrollmentChanges({
      page: pagination.page,
      pageSize: pagination.pageSize,
      studentId: searchForm.studentId ? Number(searchForm.studentId) : undefined,
      changeType: searchForm.changeType || undefined
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.studentId = ''
  searchForm.changeType = ''
  pagination.page = 1
  fetchData()
}

function viewTimeline(studentId: number) {
  router.push(`/enrollment-changes/timeline/${studentId}`)
}

// ---- 创建异动 ----
const createDialogVisible = ref(false)
const createLoading = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = reactive({
  studentId: null as number | null,
  changeType: '',
  previousStatus: '',
  newStatus: '',
  previousClassId: undefined as number | undefined,
  newClassId: undefined as number | undefined,
  changeDate: '',
  reason: ''
})

const createRules: FormRules = {
  studentId: [{ required: true, message: '请输入学生ID', trigger: 'blur' }],
  changeType: [{ required: true, message: '请选择异动类型', trigger: 'change' }],
  previousStatus: [{ required: true, message: '请选择异动前状态', trigger: 'change' }],
  newStatus: [{ required: true, message: '请选择异动后状态', trigger: 'change' }],
  changeDate: [{ required: true, message: '请选择异动日期', trigger: 'change' }],
  reason: [{ required: true, message: '请输入异动原因', trigger: 'blur' }]
}

function onChangeType() {
  if (createForm.changeType !== 'TRANSFER') {
    createForm.previousClassId = undefined
    createForm.newClassId = undefined
  }
}

function showCreateDialog() {
  createDialogVisible.value = true
}

function resetCreateForm() {
  createFormRef.value?.resetFields()
  createForm.studentId = null
  createForm.changeType = ''
  createForm.previousStatus = ''
  createForm.newStatus = ''
  createForm.previousClassId = undefined
  createForm.newClassId = undefined
  createForm.changeDate = ''
  createForm.reason = ''
}

async function handleCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return

  createLoading.value = true
  try {
    await createEnrollmentChange({
      studentId: createForm.studentId!,
      changeType: createForm.changeType,
      previousStatus: createForm.previousStatus,
      newStatus: createForm.newStatus,
      previousClassId: createForm.previousClassId,
      newClassId: createForm.newClassId,
      changeDate: createForm.changeDate,
      reason: createForm.reason
    })
    ElMessage.success('异动记录创建成功')
    createDialogVisible.value = false
    fetchData()
  } catch {
    // error handled by interceptor
  } finally {
    createLoading.value = false
  }
}

// ---- 更正记录 ----
const correctDialogVisible = ref(false)
const correctLoading = ref(false)
const correctTarget = ref<EnrollmentChange | null>(null)
const correctFormRef = ref<FormInstance>()
const correctForm = reactive({
  correctedRecordId: null as number | null,
  correctionReason: '',
  changeType: '',
  previousStatus: '',
  newStatus: '',
  previousClassId: undefined as number | undefined,
  newClassId: undefined as number | undefined,
  reason: ''
})

const correctRules: FormRules = {
  correctionReason: [{ required: true, message: '请输入更正原因', trigger: 'blur' }],
  changeType: [{ required: true, message: '请选择异动类型', trigger: 'change' }],
  previousStatus: [{ required: true, message: '请选择异动前状态', trigger: 'change' }],
  newStatus: [{ required: true, message: '请选择异动后状态', trigger: 'change' }],
  reason: [{ required: true, message: '请输入异动原因', trigger: 'blur' }]
}

function showCorrectDialog(row: EnrollmentChange) {
  correctTarget.value = row
  correctForm.correctedRecordId = row.id
  correctForm.correctionReason = ''
  correctForm.changeType = row.changeType
  correctForm.previousStatus = row.previousStatus
  correctForm.newStatus = row.newStatus
  correctForm.previousClassId = row.previousClassId ?? undefined
  correctForm.newClassId = row.newClassId ?? undefined
  correctForm.reason = row.reason
  correctDialogVisible.value = true
}

function resetCorrectForm() {
  correctFormRef.value?.resetFields()
  correctTarget.value = null
}

async function handleCorrect() {
  const valid = await correctFormRef.value?.validate().catch(() => false)
  if (!valid) return

  correctLoading.value = true
  try {
    await correctEnrollmentChange({
      correctedRecordId: correctForm.correctedRecordId!,
      correctionReason: correctForm.correctionReason,
      changeType: correctForm.changeType,
      previousStatus: correctForm.previousStatus,
      newStatus: correctForm.newStatus,
      previousClassId: correctForm.previousClassId,
      newClassId: correctForm.newClassId,
      reason: correctForm.reason
    })
    ElMessage.success('更正记录已追加')
    correctDialogVisible.value = false
    fetchData()
  } catch {
    // error handled by interceptor
  } finally {
    correctLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.enrollment-change-container {
  padding: 20px;
}
.header-card {
  margin-bottom: 16px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header h2 {
  margin: 0;
}
.search-card {
  margin-bottom: 16px;
}
</style>
