import http from './index'

export interface EnrollmentChange {
  id: number
  studentId: number
  changeType: string
  previousStatus: string
  newStatus: string
  previousClassId: number | null
  newClassId: number | null
  changeDate: string
  reason: string
  operatorId: number
  correctedRecordId: number | null
  correctionReason: string | null
  createdAt: string
  updatedAt: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface CreateEnrollmentChangeDTO {
  studentId: number
  changeType: string
  previousStatus: string
  newStatus: string
  previousClassId?: number
  newClassId?: number
  changeDate: string
  reason: string
}

export interface CorrectEnrollmentChangeDTO {
  correctedRecordId: number
  correctionReason: string
  changeType: string
  previousStatus: string
  newStatus: string
  previousClassId?: number
  newClassId?: number
  reason: string
}

export function createEnrollmentChange(data: CreateEnrollmentChangeDTO) {
  return http.post<any, { data: EnrollmentChange }>('/enrollment-changes', data)
}

export function correctEnrollmentChange(data: CorrectEnrollmentChangeDTO) {
  return http.post<any, { data: EnrollmentChange }>('/enrollment-changes/correct', data)
}

export function getEnrollmentChangeById(id: number) {
  return http.get<any, { data: EnrollmentChange }>(`/enrollment-changes/${id}`)
}

export function getTimelineByStudentId(studentId: number) {
  return http.get<any, { data: EnrollmentChange[] }>(`/enrollment-changes/student/${studentId}`)
}

export function pageEnrollmentChanges(params: {
  page: number
  pageSize: number
  studentId?: number
  changeType?: string
}) {
  return http.get<any, { data: PageResult<EnrollmentChange> }>('/enrollment-changes', { params })
}
