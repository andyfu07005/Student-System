import http from './request'

export interface User {
  id: number
  username: string
  realName: string
  email: string
  phone: string
  status: number
  roleCode: string
  lastLogin: string
  createdAt: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

export function getUserList(params: {
  page: number
  size: number
  keyword?: string
  roleCode?: string
}) {
  return http.get<{ data: PageResult<User> }>('/users', { params })
}

export function getUser(id: number) {
  return http.get<{ data: User }>(`/users/${id}`)
}

export function createUser(data: {
  username: string
  password?: string
  realName?: string
  email?: string
  phone?: string
  roleCode: string
}) {
  return http.post('/users', data)
}

export function updateUser(id: number, data: {
  username?: string
  realName?: string
  email?: string
  phone?: string
  roleCode?: string
}) {
  return http.put(`/users/${id}`, data)
}

export function deleteUser(id: number) {
  return http.delete(`/users/${id}`)
}

export function updateUserStatus(id: number, status: number) {
  return http.put(`/users/${id}/status`, { status })
}

export function resetPassword(id: number, password: string) {
  return http.put(`/users/${id}/reset-password`, { password })
}
