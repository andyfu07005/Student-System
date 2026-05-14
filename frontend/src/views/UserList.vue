<template>
  <div class="page-container">
    <el-header class="page-header">
      <h3>用户管理</h3>
      <el-button type="primary" @click="openCreate">新建用户</el-button>
    </el-header>

    <!-- 搜索/筛选 -->
    <el-card class="filter-bar">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-input v-model="query.keyword" placeholder="搜索用户名/姓名" clearable @clear="fetchData" @keyup.enter="fetchData" />
        </el-col>
        <el-col :span="4">
          <el-select v-model="query.roleCode" placeholder="角色筛选" clearable @change="fetchData">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="fetchData">查询</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.roleCode)">{{ roleLabel(row.roleCode) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              active-text="启用"
              inactive-text="禁用"
              inline-prompt
              @change="(val: boolean) => toggleStatus(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="lastLogin" label="最后登录" width="180" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="openResetPwd(row)">重置密码</el-button>
            <el-popconfirm title="确定删除该用户？" @confirm="doDelete(row.id)">
              <template #reference>
                <el-button link type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </div>
    </el-card>

    <!-- 新建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑用户' : '新建用户'" width="520px" @closed="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!editingId" />
        </el-form-item>
        <el-form-item v-if="!editingId" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="留空则默认 123456" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="角色" prop="roleCode">
          <el-select v-model="form.roleCode" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="doSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="pwdVisible" title="重置密码" width="400px">
      <el-form :model="pwdForm" label-width="80px">
        <el-form-item label="新密码">
          <el-input v-model="pwdForm.password" type="password" show-password placeholder="至少6位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdLoading" @click="doResetPwd">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import {
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  updateUserStatus,
  resetPassword,
  type User,
} from '../api/user'

// ------- 列表 -------
const loading = ref(false)
const tableData = ref<User[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 10, keyword: '', roleCode: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserList({ ...query })
    const d = res.data.data
    tableData.value = d.records
    total.value = d.total
  } finally {
    loading.value = false
  }
}
onMounted(fetchData)

// ------- 启用/禁用 -------
async function toggleStatus(row: User, val: boolean) {
  try {
    await updateUserStatus(row.id, val ? 1 : 0)
    row.status = val ? 1 : 0
    ElMessage.success(val ? '已启用' : '已禁用')
  } catch {
    // revert is handled naturally since :model-value is bound
  }
}

// ------- 新建/编辑 -------
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: '',
  roleCode: '' as string,
})
const rules: FormRules = {
  username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

function resetForm() {
  editingId.value = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.email = ''
  form.phone = ''
  form.roleCode = ''
  formRef.value?.resetFields()
}

function openCreate() {
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: User) {
  resetForm()
  editingId.value = row.id
  form.username = row.username
  form.realName = row.realName || ''
  form.email = row.email || ''
  form.phone = row.phone || ''
  form.roleCode = row.roleCode
  dialogVisible.value = true
}

async function doSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (editingId.value) {
      await updateUser(editingId.value, { ...form })
      ElMessage.success('修改成功')
    } else {
      await createUser({
        username: form.username,
        password: form.password || undefined,
        realName: form.realName,
        email: form.email,
        phone: form.phone,
        roleCode: form.roleCode,
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

// ------- 删除 -------
async function doDelete(id: number) {
  await deleteUser(id)
  ElMessage.success('已删除')
  fetchData()
}

// ------- 重置密码 -------
const pwdVisible = ref(false)
const pwdLoading = ref(false)
const pwdForm = reactive({ userId: 0, password: '' })

function openResetPwd(row: User) {
  pwdForm.userId = row.id
  pwdForm.password = ''
  pwdVisible.value = true
}

async function doResetPwd() {
  if (pwdForm.password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  pwdLoading.value = true
  try {
    await resetPassword(pwdForm.userId, pwdForm.password)
    ElMessage.success('密码已重置')
    pwdVisible.value = false
  } finally {
    pwdLoading.value = false
  }
}

// ------- 辅助 -------
function roleLabel(code: string) {
  const map: Record<string, string> = { ADMIN: '管理员', TEACHER: '教师', STUDENT: '学生' }
  return map[code] || code
}
function roleTagType(code: string) {
  const map: Record<string, string> = { ADMIN: 'danger', TEACHER: 'warning', STUDENT: 'info' }
  return map[code] || ''
}
</script>

<style scoped>
.page-container {
  padding: 16px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0;
}
.filter-bar {
  margin-bottom: 16px;
}
.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
