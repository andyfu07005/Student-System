import http from './index'

export interface CourseGrade {
  courseNo: string
  courseName: string
  credit: number
  courseType: string
  score: number
  examType: string
  gradePoint: number
}

export interface SemesterGroup {
  semester: string
  courses: CourseGrade[]
  semesterGpa: number
  semesterCredits: number
}

export interface Transcript {
  schoolName: string
  academicYear: string
  semester: string
  studentNo: string
  studentName: string
  gender: string
  className: string
  grade: string
  major: string
  enrollmentDate: string
  semesters: SemesterGroup[]
  totalGpa: number
  totalCredits: number
}

export function getTranscript(studentId: number, semester?: string) {
  return http.get<any, Result<Transcript>>(`/transcripts/${studentId}`, { params: { semester } })
}

export function getTranscriptPdfUrl(studentId: number, semester?: string): string {
  const params = new URLSearchParams()
  if (semester) params.set('semester', semester)
  const qs = params.toString()
  return `/api/transcripts/${studentId}/pdf${qs ? '?' + qs : ''}`
}

export function getBatchPdfUrl(classId: number, semester?: string): string {
  const params = new URLSearchParams()
  if (semester) params.set('semester', semester)
  const qs = params.toString()
  return `/api/transcripts/batch/class/${classId}/pdf${qs ? '?' + qs : ''}`
}

interface Result<T> {
  code: number
  message: string
  data: T
}
