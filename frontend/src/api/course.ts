import http from './index'

export interface Course {
  id?: number
  courseNo: string
  name: string
  credit: number
  hours: number
  type: string
  major?: string
  description?: string
  createdAt?: string
  updatedAt?: string
}

export function listCourses(params: {
  keyword?: string; type?: string; major?: string; page?: number; size?: number
}) {
  return http.get('/courses', { params }).then(r => r.data)
}

export function getCourse(id: number) {
  return http.get(`/courses/${id}`).then(r => r.data)
}

export function createCourse(data: Course) {
  return http.post('/courses', data).then(r => r.data)
}

export function updateCourse(id: number, data: Course) {
  return http.put(`/courses/${id}`, data).then(r => r.data)
}

export function deleteCourse(id: number) {
  return http.delete(`/courses/${id}`).then(r => r.data)
}
