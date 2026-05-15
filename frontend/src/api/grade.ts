import http from './index'

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
