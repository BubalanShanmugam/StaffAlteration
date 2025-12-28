import apiClient from './client'

export interface LoginRequest {
  username: string
  password: string
}

export interface AuthResponse {
  code: number
  message: string
  data: {
    accessToken: string
    refreshToken: string
    user: {
      id: number
      username: string
      email: string
      staffId: string  // Staff ID (e.g., "STAFF001")
      roles: string[]
    }
  }
  timestamp: number
}

export interface TimetableTemplate {
  id: number
  templateName: string
  classCode: string
  dayOrder: number
  periodNumber: number
  subjectCode: string
  subjectName: string
  staffId: string
  staffName: string
  status: 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'ARCHIVED'
  createdAt: string
  remarks?: string
  createdBy?: string
  updatedAt?: string
}

export interface CreateTimetableRequest {
  templateName: string
  classCode: string
  dayOrder: number
  periodNumber: number
  subjectCode: string
  staffId: string
  remarks?: string
}

export interface AttendanceMarkRequest {
  staffId: string
  status: 'PRESENT' | 'ABSENT' | 'LEAVE'
  dayType?: 'FULL_DAY' | 'MORNING_ONLY' | 'AFTERNOON_ONLY'
  attendanceDate: string
  attendanceDates?: string[]
  remarks?: string
}

export interface AttendanceDTO {
  id: number
  staffId: string
  attendanceDate: string
  status: string
  dayType: string
  remarks?: string
  createdAt: string
  updatedAt: string
}

export interface AlterationDTO {
  id: number
  originalStaffId: string
  originalStaffName: string
  substituteStaffId: string
  substituteStaffName: string
  classCode: string
  subjectName: string
  dayOrder: number
  periodNumber: number
  alterationDate: string
  status: string
  remarks?: string
}

// Auth APIs
export const authAPI = {
  login: (data: LoginRequest) => 
    apiClient.post<AuthResponse>('/auth/login', data),
  
  logout: () => 
    apiClient.post('/auth/logout'),
  
  refresh: (refreshToken: string) => 
    apiClient.post<AuthResponse>('/auth/refresh', { refreshToken }),
  
  getCurrentUser: () => 
    apiClient.get('/auth/user'),
}

// Timetable APIs
export const timetableAPI = {
  create: (data: CreateTimetableRequest) =>
    apiClient.post<any>('/timetable-template/create', data),
  
  getByClass: (classCode: string) =>
    apiClient.get<any>(`/timetable-template/class/${classCode}`),
  
  getActiveByClass: (classCode: string) =>
    apiClient.get<any>(`/timetable-template/class/${classCode}/active`),
  
  getByStaff: (staffId: string) =>
    apiClient.get<any>(`/timetable-template/staff/${staffId}`),
  
  getBySlot: (classCode: string, dayOrder: number, periodNumber: number) =>
    apiClient.get<any>(`/timetable-template/slot/${classCode}/${dayOrder}/${periodNumber}`),
  
  update: (id: number, data: CreateTimetableRequest) =>
    apiClient.put<any>(`/timetable-template/update/${id}`, data),
  
  delete: (id: number) =>
    apiClient.delete(`/timetable-template/${id}`),
  
  changeStatus: (id: number, status: string) =>
    apiClient.put(`/timetable-template/${id}/status?status=${status}`),
  
  getStaffWorkload: (staffId: string, dayOrder: number) =>
    apiClient.get<any>(`/timetable-template/staff/${staffId}/workload/${dayOrder}`),
}

// Attendance APIs
export const attendanceAPI = {
  markAttendance: (data: AttendanceMarkRequest) =>
    apiClient.post<any>('/attendance/mark', data),
  
  getAttendanceByDate: (date: string) =>
    apiClient.get<any>(`/attendance/date/${date}`),
  
  getStaffAttendance: (staffId: string) =>
    apiClient.get<any>(`/attendance/staff/${staffId}`),
  
  getAbsentStaffByDate: (date: string) =>
    apiClient.get<any>(`/attendance/absent/${date}`),
}

// Alteration APIs
export const alterationAPI = {
  getByDate: (date: string) =>
    apiClient.get<any>(`/alteration/date/${date}`),
  
  getByStaff: (staffId: string) =>
    apiClient.get<any>(`/alteration/staff/${staffId}`),
  
  updateStatus: (alterationId: number, status: string) =>
    apiClient.put<any>(`/alteration/${alterationId}/status`, { status }),
  
  acknowledge: (alterationId: number) =>
    apiClient.put<any>(`/alteration/${alterationId}/acknowledge`, {}),
}
