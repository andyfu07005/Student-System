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
assert.match(routerSource, /path:\s*['"]students['"]/)
assert.match(routerSource, /path:\s*['"]classes['"]/)
assert.match(routerSource, /path:\s*['"]courses['"]/)
assert.match(routerSource, /path:\s*['"]grades['"]/)
assert.match(routerSource, /path:\s*['"]transcripts['"]/)
assert.match(routerSource, /path:\s*['"]enrollment-changes['"]/)
assert.match(routerSource, /path:\s*['"]users['"]/)
assert.match(routerSource, /redirect:\s*['"]\/students['"]/)
assert.match(loginSource, /router\.push\(['"]\/['"]\)/)

for (const label of ['学生管理', '班级管理', '课程管理', '成绩查询', '成绩单', '学籍变更', '用户管理']) {
  assert.match(layoutSource, new RegExp(label))
}
