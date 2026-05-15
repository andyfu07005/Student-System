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
  return http.get<Result<PageResult<Grade>>>('/grades', { params }).then(r => r.data)
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
