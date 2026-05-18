import http from './index'

export interface Schedule {
  id?: number
  courseId: number
  teacherId: number
  classroom: string
  dayOfWeek: number
  startTime: string
  endTime: string
  capacity?: number
  courseName?: string
  teacherName?: string
  createdAt?: string
  updatedAt?: string
}

export interface ScheduleDTO {
  courseId: number
  teacherId: number
  classroom: string
  dayOfWeek: number
  startTime: string
  endTime: string
  capacity?: number
}

export function listSchedules(params: {
  courseId?: number; teacherId?: number; classroom?: string; dayOfWeek?: number; page?: number; size?: number
}) {
  return http.get('/schedules', { params }).then(r => r.data)
}

export function getSchedule(id: number) {
  return http.get(`/schedules/${id}`).then(r => r.data)
}

export function createSchedule(data: ScheduleDTO) {
  return http.post('/schedules', data).then(r => r.data)
}

export function updateSchedule(id: number, data: ScheduleDTO) {
  return http.put(`/schedules/${id}`, data).then(r => r.data)
}

export function deleteSchedule(id: number) {
  return http.delete(`/schedules/${id}`).then(r => r.data)
}
