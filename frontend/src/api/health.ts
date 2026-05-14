import request from './request'

export interface HealthData {
  status: string
  timestamp: string
  service: string
}

export function getHealth() {
  return request.get<unknown, HealthData>('/health')
}
