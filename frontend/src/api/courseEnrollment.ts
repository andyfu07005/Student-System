import http from './request'

export interface AvailableCourse {
  id: number
  courseNo: string
  name: string
  credit: number
  hours: number
  type: string
  major?: string
  description?: string
  capacity: number
  enrolledCount: number
  teacherName?: string
  startDate?: string
  endDate?: string
  semester?: string
}

export interface EnrolledCourse {
  enrollmentId: number
  courseId: number
  courseNo: string
  courseName: string
  type: string
  teacherName?: string
  capacity: number
  enrolledCount: number
  semester?: string
  enrolledAt: string
}

export interface EnrolledStudent {
  studentId: number
  studentNo: string
  studentName: string
  gender: string
  phone: string
  className?: string
  enrolledAt: string
}

interface Result<T> {
  code: number
  message: string
  data: T
}

interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export function listAvailableCourses(params: {
  keyword?: string; type?: string; page?: number; size?: number
}) {
  return http.get('/enrollment/available-courses', { params }).then(r => r.data)
}

export function listMyCourses(params: { page?: number; size?: number }) {
  return http.get('/enrollment/my-courses', { params }).then(r => r.data)
}

export function enroll(courseId: number) {
  return http.post('/enrollment/enroll', { courseId }).then(r => r.data)
}

export function dropCourse(courseId: number) {
  return http.post('/enrollment/drop', { courseId }).then(r => r.data)
}

export function listCourseStudents(courseId: number, params: { page?: number; size?: number }) {
  return http.get(`/enrollment/course-students/${courseId}`, { params }).then(r => r.data)
}

export function listTeachingCourses(params: { page?: number; size?: number }) {
  return http.get('/enrollment/teaching-courses', { params }).then(r => r.data)
}
