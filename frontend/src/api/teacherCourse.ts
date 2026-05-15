import http from './index'

export interface TeacherCourse {
  id?: number
  teacherId: number
  courseId: number
  semester: string
  academicYear: string
  createdAt?: string
  updatedAt?: string
}

export function myTeacherCourses() {
  return http.get('/teacher-courses/my').then(r => r.data)
}

export function listTeacherCourses(params?: { teacherId?: number; semester?: string }) {
  return http.get('/teacher-courses', { params }).then(r => r.data)
}

export function createTeacherCourse(data: TeacherCourse) {
  return http.post('/teacher-courses', data).then(r => r.data)
}

export function deleteTeacherCourse(id: number) {
  return http.delete(`/teacher-courses/${id}`).then(r => r.data)
}
