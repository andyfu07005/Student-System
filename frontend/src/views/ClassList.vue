<template>
  <div class="class-list">
    <el-card>
      <div class="toolbar">
        <el-input v-model="searchKeyword" placeholder="搜索班级名称/年级/专业" clearable style="width: 260px" @keyup.enter="fetchData" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button type="success" @click="openCreate">新增班级</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top: 16px">
        <el-table-column prop="name" label="班级名称" width="150" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="major" label="所属专业" width="150" />
        <el-table-column prop="headTeacher" label="班主任" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该班级?" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑班级' : '新增班级'" width="500px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="班级名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" placeholder="如 2024级" />
        </el-form-item>
        <el-form-item label="所属专业" prop="major">
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item label="班主任">
          <el-input v-model="form.headTeacher" />
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
import { listClasses, createClass, updateClass, deleteClass, type ClassInfo } from '@/api/classInfo'

const searchKeyword = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref<ClassInfo[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref()
const form = ref<ClassInfo>({ name: '', grade: '', major: '', headTeacher: '' })

const rules = {
  name: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }],
  major: [{ required: true, message: '请输入所属专业', trigger: 'blur' }],
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listClasses({ keyword: searchKeyword.value, page: page.value, size: size.value })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: ClassInfo) {
  editingId.value = row.id!
  form.value = { ...row }
  dialogVisible.value = true
}

function resetForm() {
  form.value = { name: '', grade: '', major: '', headTeacher: '' }
  editingId.value = null
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await updateClass(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createClass(form.value)
      ElMessage.success('创建成功')
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
    await deleteClass(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '删除失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
}
.toolbar .el-button--success {
  margin-left: auto;
}
</style>
