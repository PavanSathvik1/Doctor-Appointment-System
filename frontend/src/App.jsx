import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

// Auth Pages
import Login from './pages/auth/Login';
import RegisterPatient from './pages/auth/RegisterPatient';
import RegisterDoctor from './pages/auth/RegisterDoctor';
import OtpVerify from './pages/auth/OtpVerify';
import ForgotPassword from './pages/auth/ForgotPassword';
import ResetPassword from './pages/auth/ResetPassword';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import PendingDoctors from './pages/admin/PendingDoctors';

// Patient Pages
import PatientDashboard from './pages/patient/PatientDashboard';
import SearchDoctors from './pages/patient/SearchDoctors';
import BookAppointment from './pages/patient/BookAppointment';
import PatientPrescriptions from './pages/patient/PatientPrescriptions';
import PatientAppointments from './pages/patient/PatientAppointments';

// Doctor Pages
import DoctorDashboard, { DoctorSchedule } from './pages/doctor/DoctorDashboard';
import IssuePrescription from './pages/doctor/IssuePrescription';
import DoctorAvailability from './pages/doctor/DoctorAvailability';

// Components
import ChatbotWidget from './components/ChatbotWidget';

// Admin Components
import AdminOverview from './pages/admin/AdminOverview';
import UserManagement from './pages/admin/UserManagement';
import AllAppointmentsAdmin from './pages/admin/AllAppointmentsAdmin';

// Doctor Components
import ProfileSettings from './pages/doctor/ProfileSettings';

// Patient Components
import PatientOverview from './pages/patient/PatientOverview';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <ToastContainer position="top-right" autoClose={4000} hideProgressBar={false} />
        <Routes>
          {/* Public Auth Routes */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<RegisterPatient />} />
          <Route path="/register-doctor" element={<RegisterDoctor />} />
          <Route path="/verify-otp" element={<OtpVerify />} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />

          {/* Protected Routes */}
          {/* Patient Routes */}
          <Route path="/dashboard" element={<ProtectedRoute allowedRoles={['PATIENT']}><PatientDashboard /></ProtectedRoute>}>
            <Route index element={<PatientOverview />} />
            <Route path="search" element={<SearchDoctors />} />
            <Route path="book/:doctorId" element={<BookAppointment />} />
            <Route path="appointments" element={<PatientAppointments />} />
            <Route path="prescriptions" element={<PatientPrescriptions />} />
          </Route>

          {/* Doctor Routes */}
          <Route path="/doctor/dashboard" element={<ProtectedRoute allowedRoles={['DOCTOR']}><DoctorDashboard /></ProtectedRoute>}>
            <Route index element={<DoctorSchedule />} />
            <Route path="availability" element={<DoctorAvailability />} />
            <Route path="prescriptions" element={<IssuePrescription />} />
            <Route path="settings" element={<ProfileSettings />} />
          </Route>

          {/* Admin Routes */}
          <Route path="/admin" element={<ProtectedRoute allowedRoles={['ADMIN']}><AdminDashboard /></ProtectedRoute>}>
            <Route index element={<AdminOverview />} />
            <Route path="pending-doctors" element={<PendingDoctors />} />
            <Route path="users" element={<UserManagement />} />
            <Route path="appointments" element={<AllAppointmentsAdmin />} />
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>

        {/* Global Floating Components */}
        <ChatbotWidget />
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
