import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import './index.css'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { TimetablesPage } from './pages/TimetablesPage'
import { SettingsPage } from './pages/SettingsPage'
import { AttendancePage } from './pages/AttendancePage'
import { AlterationDashboardPage } from './pages/AlterationDashboardPage'
import { HodDashboardPage } from './pages/HodDashboardPage'
import { TimetableManagementPage } from './pages/TimetableManagementPage'
import { StaffManagementPage } from './pages/StaffManagementPage'
import { DayOrderSchedulePage } from './pages/DayOrderSchedulePage'
import { ClassManagementPage } from './pages/ClassManagementPage'
import { ProtectedRoute } from './components/ProtectedRoute'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <DashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/attendance"
          element={
            <ProtectedRoute>
              <AttendancePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/alterations"
          element={
            <ProtectedRoute>
              <AlterationDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/hod-dashboard"
          element={
            <ProtectedRoute requiredRoles={['HOD', 'ADMIN']}>
              <HodDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/manage-timetable"
          element={
            <ProtectedRoute requiredRoles={['HOD', 'ADMIN', 'STAFF']}>
              <TimetableManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/staff-management"
          element={
            <ProtectedRoute requiredRoles={['HOD', 'ADMIN']}>
              <StaffManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/day-order-schedule"
          element={
            <ProtectedRoute requiredRoles={['HOD', 'ADMIN']}>
              <DayOrderSchedulePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/class-management"
          element={
            <ProtectedRoute requiredRoles={['HOD', 'ADMIN']}>
              <ClassManagementPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/timetables"
          element={
            <ProtectedRoute>
              <TimetablesPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/settings"
          element={
            <ProtectedRoute>
              <SettingsPage />
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
