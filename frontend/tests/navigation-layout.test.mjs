import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import assert from 'node:assert/strict'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const routerSource = readFileSync(resolve(root, 'src/router/index.ts'), 'utf8')
const loginSource = readFileSync(resolve(root, 'src/views/Login.vue'), 'utf8')
const layoutSource = readFileSync(resolve(root, 'src/components/MainLayout.vue'), 'utf8')

assert.match(routerSource, /component:\s*\(\)\s*=>\s*import\(['"]@\/components\/MainLayout\.vue['"]\)/)
assert.match(routerSource, /children:\s*\[/)
assert.doesNotMatch(routerSource, /\{\s*\{/)
for (const path of [
  'students',
  'classes',
  'courses',
  'schedule',
  'schedules',
  'grades',
  'grade-management',
  'transcripts',
  'enrollment-changes',
  'users',
  'course-selection',
  'course-roster',
]) {
  assert.match(routerSource, new RegExp(`path:\\s*['"]${path}['"]`))
}
assert.match(routerSource, /redirect:\s*['"]\/students['"]/)
assert.match(loginSource, /router\.push\(['"]\/['"]\)/)

for (const label of [
  '学生管理',
  '班级管理',
  '课程管理',
  '课表查询',
  '排课管理',
  '成绩查询',
  '成绩管理',
  '成绩单',
  '学籍变更',
  '用户管理',
  '学生选课',
  '选课名单',
]) {
  assert.match(layoutSource, new RegExp(label))
}
