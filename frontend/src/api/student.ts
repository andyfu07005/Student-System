import http from './index'

export interface Student {
  id?: number
  studentNo: string
  name: string
  gender: string
  birthDate?: string
  idCard?: string
  phone?: string
  address?: string
  enrollmentDate?: string
  classId?: number
  status?: string
  createdAt?: string
  updatedAt?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export function listStudents(params: {
  keyword?: string
  classId?: number
  status?: string
  page?: number
  size?: number
}) {
  return http.get<Result<PageResult<Student>>>('/students', { params }).then(r => r.data)
}

export function getStudent(id: number) {
  return http.get<Result<Student>>(`/students/${id}`).then(r => r.data)
}

export function createStudent(data: Student) {
  return http.post<Result<Student>>('/students', data).then(r => r.data)
}

export function updateStudent(id: number, data: Student) {
  return http.put<Result<Student>>(`/students/${id}`, data).then(r => r.data)
}

export function deleteStudent(id: number) {
  return http.delete<Result<void>>(`/students/${id}`).then(r => r.data)
}

interface Result<T> {
  code: number
  message: string
  data: T
}
