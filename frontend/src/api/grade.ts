import http from './index'

export interface Grade {
  id?: number
  studentId: number
  courseId: number
  score: number
  semester: string
  academicYear: string
  examType: string
  remarks?: string
  createdAt?: string
  updatedAt?: string
  studentName?: string
  studentNo?: string
  courseName?: string
  courseNo?: string
  credit?: number
  courseType?: string
}
export type GpaAlgorithm = 'FOUR_POINT' | 'FIVE_POINT'

export interface GradeRecord {
  id?: number
  studentId: number
  studentNo?: string
  studentName?: string
  courseId: number
  courseNo?: string
  courseName?: string
  credit?: number
  academicYear: string
  semester: string
  score: number
  gradePoint?: number
  createdAt?: string
  updatedAt?: string
}

export interface SemesterGpa {
  academicYear: string
  semester: string
  credits: number
  gpa: number
}

export interface GpaSummary {
  studentId: number
  studentNo: string
  studentName: string
  algorithm: GpaAlgorithm
  cumulativeCredits: number
  cumulativeGpa: number
  semesters: SemesterGpa[]
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export function listGrades(params: {
  keyword?: string
  studentId?: number
  courseId?: number
  semester?: string
  page?: number
  size?: number
}) {
  return http.get<any, Result<PageResult<Grade>>>('/grades', { params })
}

export function getGrade(id: number) {
  return http.get<Result<Grade>>(`/grades/${id}`).then(r => r.data)
}

export function createGrade(data: Grade) {
  return http.post<Result<Grade>>('/grades', data).then(r => r.data)
}

export function updateGrade(id: number, data: Grade) {
  return http.put<Result<Grade>>(`/grades/${id}`, data).then(r => r.data)
}

export function deleteGrade(id: number) {
  return http.delete<Result<void>>(`/grades/${id}`).then(r => r.data)
}

interface Result<T> {
  code: number
  message: string
  data: T
}
export function listGradeRecords(params: {
  studentId?: number
  courseId?: number
  academicYear?: string
  semester?: string
  algorithm?: GpaAlgorithm
  page?: number
  size?: number
}) {
  return http.get<PageResult<GradeRecord>>('/grade-records', { params }).then(r => r.data)
}

export function getGpaSummary(params: { studentId: number; algorithm?: GpaAlgorithm }) {
  return http.get<GpaSummary>('/grade-records/gpa', { params }).then(r => r.data)
}

export function createGradeRecord(data: GradeRecord, algorithm: GpaAlgorithm) {
  return http.post<GradeRecord>('/grade-records', data, { params: { algorithm } }).then(r => r.data)
}

export function updateGradeRecord(id: number, data: GradeRecord, algorithm: GpaAlgorithm) {
  return http.put<GradeRecord>(`/grade-records/${id}`, data, { params: { algorithm } }).then(r => r.data)
}

export function deleteGradeRecord(id: number) {
  return http.delete<void>(`/grade-records/${id}`).then(r => r.data)
}
