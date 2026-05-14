import http from './request'

export function login(username: string, password: string) {
  return http.post('/auth/login', { username, password })
}
