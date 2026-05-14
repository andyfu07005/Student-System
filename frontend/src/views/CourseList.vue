<template>
  <div class="course-list">
    <el-card>
      <div class="toolbar">
        <el-input v-model="searchKeyword" placeholder="搜索课程名称/编号" clearable style="width: 220px" @keyup.enter="fetchData" />
        <el-select v-model="filterType" placeholder="全部类型" clearable style="width: 120px" @change="fetchData">
          <el-option label="必修" value="必修" />
          <el-option label="选修" value="选修" />
        </el-select>
        <el-input v-model="filterMajor" placeholder="专业筛选" clearable style="width: 150px" @keyup.enter="fetchData" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button type="success" @click="openCreate">新增课程</el-button>
      </div>

      <el-table :data="tableData" border stripe v-loading="loading" style="margin-top: 16px">
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        <el-table-column prop="name" label="课程名称" width="160" />
        <el-table-column prop="credit" label="学分" width="70" />
        <el-table-column prop="hours" label="学时" width="70" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.type === '必修' ? 'danger' : 'info'" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="major" label="所属专业" width="120" />
        <el-table-column prop="description" label="课程描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该课程?" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑课程' : '新增课程'" width="560px" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="课程编号" prop="courseNo">
              <el-input v-model="form.courseNo" :disabled="!!editingId" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学分" prop="credit">
              <el-input-number v-model="form.credit" :min="0" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学时" prop="hours">
              <el-input-number v-model="form.hours" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="课程类型" prop="type">
              <el-select v-model="form.type" style="width: 100%">
                <el-option label="必修" value="必修" />
                <el-option label="选修" value="选修" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属专业">
              <el-input v-model="form.major" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="课程描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { listCourses, createCourse, updateCourse, deleteCourse, type Course } from '@/api/course'

const searchKeyword = ref('')
const filterType = ref('')
const filterMajor = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref<Course[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref()
const form = ref<Course>({
  courseNo: '', name: '', credit: 0, hours: 0, type: '必修', major: '', description: '',
})

const rules = {
  courseNo: [{ required: true, message: '请输入课程编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'blur' }],
  hours: [{ required: true, message: '请输入学时', trigger: 'blur' }],
  type: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listCourses({
      keyword: searchKeyword.value,
      type: filterType.value || undefined,
      major: filterMajor.value || undefined,
      page: page.value, size: size.value,
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function openCreate() { editingId.value = null; resetForm(); dialogVisible.value = true }
function openEdit(row: Course) { editingId.value = row.id!; form.value = { ...row }; dialogVisible.value = true }
function resetForm() {
  form.value = { courseNo: '', name: '', credit: 0, hours: 0, type: '必修', major: '', description: '' }
  editingId.value = null
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    if (editingId.value) {
      await updateCourse(editingId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await createCourse(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false; fetchData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '操作失败')
  } finally { saving.value = false }
}

async function handleDelete(id: number) {
  try {
    await deleteCourse(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (err: any) {
    ElMessage.error(err?.response?.data?.message || '删除失败')
  }
}

onMounted(fetchData)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; align-items: center; flex-wrap: wrap; }
.toolbar .el-button--success { margin-left: auto; }
</style>
