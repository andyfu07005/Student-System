import http from './index'

export interface ClassInfo {
  id?: number
  name: string
  grade: string
  major: string
  headTeacher?: string
  createdAt?: string
  updatedAt?: string
}

export function listClasses(params?: { keyword?: string; page?: number; size?: number }) {
  return http.get('/classes', { params }).then(r => r.data)
}

export function getClass(id: number) {
  return http.get(`/classes/${id}`).then(r => r.data)
}

export function createClass(data: ClassInfo) {
  return http.post('/classes', data).then(r => r.data)
}

export function updateClass(id: number, data: ClassInfo) {
  return http.put(`/classes/${id}`, data).then(r => r.data)
}

export function deleteClass(id: number) {
  return http.delete(`/classes/${id}`).then(r => r.data)
}
