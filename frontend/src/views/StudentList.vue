<template>
  <div class="student-list">
    <el-card>
      <div class="toolbar">
        <el-input v-model="searchKeyword" placeholder="搜索姓名/学号" clearable style="width: 240px" @keyup.enter="fetchData" />
        <el-select v-model="filterClassId" placeholder="全部班级" clearable style="width: 160px" @change="fetchData">
          <el-option v-for="c in classList" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-select v-model="filterStatus" placeholder="全部状态" clearable style="width: 120px" @change="fetchData">
          <el-option label="在读" value="在读" />
          <el-option label="休学" value="休学" />
          <el-option label="退学" value="退学" />
          <el-option label="毕业" value="毕业" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button type="success" @click="openCreate">新增学生</el-button>
        <el-button @click="downloadTemplate">下载模板</el-button>
        <el-upload :show-file-list="false" :before-upload="handleImport" accept=".xlsx,.xls">
          <el-button type="warning">导入Excel</el-button>
        </el-upload>
        <el-button type="info" @click="handleExport">导出Excel</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top: 16px">
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="60" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="status" label="学籍状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enrollmentDate" label="入学日期" width="110" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" @click="$router.push(`/enrollment-changes/timeline/${row.id}`)">
              异动
            </el-button>
            <el-popconfirm title="确定删除该学生?" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑学生' : '新增学生'" width="600px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" :disabled="!!editingId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" style="width: 100%">
                <el-option label="男" value="男" />
                <el-option label="女" value="女" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期">
              <el-date-picker v-model="form.birthDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身份证号">
              <el-input v-model="form.idCard" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="家庭住址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="入学日期">
              <el-date-picker v-model="form.enrollmentDate" type="date" style="width: 100%" value-format="YYYY-MM-DD" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属班级">
              <el-select v-model="form.classId" style="width: 100%" clearable>
                <el-option v-for="c in classList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="学籍状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="在读" value="在读" />
            <el-option label="休学" value="休学" />
            <el-option label="退学" value="退学" />
            <el-option label="毕业" value="毕业" />
          </el-select>
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
import { listStudents, createStudent, updateStudent, deleteStudent, type Student } from '@/api/student'
import { listClasses } from '@/api/classInfo'

const searchKeyword = ref('')
const filterClassId = ref<number | undefined>()
const filterStatus = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref<Student[]>([])
const loading = ref(false)
const classList = ref<{ id: number; name: string }[]>([])

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref()
const form = ref<Student>({
  studentNo: '',
  name: '',
  gender: '',
  birthDate: '',
  idCard: '',
  phone: '',
  address: '',
  enrollmentDate: '',
  classId: undefined,
  status: '在读',
})

const rules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
}

function statusType(status: string) {
  const map: Record<string, string> = { '在读': 'success', '休学': 'warning', '退学': 'danger', '毕业': 'info' }
  return map[status] || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listStudents({
      keyword: searchKeyword.value,
      classId: filterClassId.value,
      status: filterStatus.value || undefined,
      page: page.value,
      size: size.value,
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadClasses() {
  try {
    const res = await listClasses({ size: 100 })
    classList.value = res.data?.records || []
  } catch { /* ignore */ }
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: Student) {
  editingId.value = row.id!
  form.value = { ...row, birthDate: row.birthDate || '', enrollmentDate: row.enrollmentDate || '' }
  dialogVisible.value = true
}

function resetForm() {
  form.value = {
    studentNo: '',
    name: '',
    gender: '',
    birthDate: '',
    idCard: '',
    phone: '',
    address: '',
    enrollmentDate: '',
    classId: undefined,
    status: '在读',
  }
  editingId.value = null
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (editingId.value) {
      await updateStudent(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createStudent(form.value)
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
    await deleteStudent(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '删除失败')
  }
}

function downloadTemplate() {
  window.open('/api/excel/template', '_blank')
}

async function handleImport(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  try {
    const res = await fetch('/api/excel/import-students', { method: 'POST', body: formData })
    const json = await res.json()
    if (json.code === 200) {
      const d = json.data
      ElMessage.success(`导入完成: 成功 ${d.success} 条, 失败 ${d.fail} 条`)
      if (d.fail > 0) {
        const details = d.failures.map((f: any) => `第${f.row}行: ${f.reason}`).join('\n')
        ElMessage.warning({ message: details, duration: 8000 })
      }
      fetchData()
    } else {
      ElMessage.error(json.message)
    }
  } catch {
    ElMessage.error('导入失败')
  }
  return false
}

function handleExport() {
  const params = new URLSearchParams()
  if (searchKeyword.value) params.set('keyword', searchKeyword.value)
  if (filterClassId.value) params.set('classId', String(filterClassId.value))
  if (filterStatus.value) params.set('status', filterStatus.value)
  window.open(`/api/excel/export-students?${params.toString()}`, '_blank')
}

onMounted(() => {
  loadClasses()
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
