import http from './index'

export interface ScheduleItem {
  id: number
  courseId: number
  courseNo: string
  courseName: string
  teacherName: string
  classroom: string
  dayOfWeek: number
  startTime: string
  endTime: string
  startWeek: number
  endWeek: number
}

export interface WeekSchedule {
  weekLabel: string
  weekStart: string
  weekEnd: string
  items: ScheduleItem[]
}

export function getWeekSchedule(date: string) {
  return http.get('/schedule', { params: { date } }).then(r => r.data)
}
