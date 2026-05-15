import http from './index'

export interface Grade {
  id?: number
  studentId: number
  courseId: number
  score: number
  semester: string
  academicYear: string
  examType: string
  operatorId?: number
  createdAt?: string
  updatedAt?: string
  studentName?: string
  studentNo?: string
  courseName?: string
  courseNo?: string
}

export interface GradeStats {
  totalCount?: number
  avgScore?: number
  maxScore?: number
  minScore?: number
  excellentCount?: number
  goodCount?: number
  passCount?: number
  failCount?: number
  passRate?: string
  excellentRate?: string
  distribution?: { scoreRange: string; count: number }[]
}

export function myGrades(params: {
  semester?: string; academicYear?: string; page?: number; size?: number
}) {
  return http.get('/grades/my', { params }).then(r => r.data)
}

export function myStatistics(academicYear?: string) {
  return http.get('/grades/my/statistics', { params: { academicYear } }).then(r => r.data)
}

export function courseGrades(courseId: number, params: {
  semester?: string; page?: number; size?: number
}) {
  return http.get(`/grades/course/${courseId}`, { params }).then(r => r.data)
}

export function courseStatistics(courseId: number, semester?: string) {
  return http.get(`/grades/course/${courseId}/statistics`, { params: { semester } }).then(r => r.data)
}

export function courseDistribution(courseId: number, semester?: string) {
  return http.get(`/grades/course/${courseId}/distribution`, { params: { semester } }).then(r => r.data)
}

export function adminStatistics(academicYear?: string) {
  return http.get('/grades/admin/statistics', { params: { academicYear } }).then(r => r.data)
}

export function studentGrades(studentId: number, params: { page?: number; size?: number }) {
  return http.get(`/grades/student/${studentId}`, { params }).then(r => r.data)
}

export function createGrade(data: Grade) {
  return http.post('/grades', data).then(r => r.data)
}

export function updateGrade(id: number, data: Partial<Grade>) {
  return http.put(`/grades/${id}`, data).then(r => r.data)
}

export function deleteGrade(id: number) {
  return http.delete(`/grades/${id}`).then(r => r.data)
}
