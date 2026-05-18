import { readFileSync } from 'node:fs'
import { dirname, resolve } from 'node:path'
import assert from 'node:assert/strict'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const apiIndexSource = readFileSync(resolve(root, 'src/api/index.ts'), 'utf8')

assert.match(apiIndexSource, /interceptors\.request\.use/)
assert.match(apiIndexSource, /localStorage\.getItem\(['"]token['"]\)/)
assert.match(apiIndexSource, /headers\.Authorization\s*=\s*`Bearer \$\{token\}`/)
