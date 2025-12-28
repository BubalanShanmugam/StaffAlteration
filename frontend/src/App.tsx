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
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
