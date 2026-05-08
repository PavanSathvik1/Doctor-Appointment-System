import React, { useState, useEffect, useContext } from 'react';
import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { AuthContext } from '../../context/AuthContext';
import { LogOut, CalendarDays, Clock, FileEdit, Settings, CheckCircle } from 'lucide-react';

const DoctorDashboard = () => {
    const { user, logout } = useContext(AuthContext);
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    const navItems = [
        { path: '/doctor/dashboard', label: 'My Schedule', icon: <CalendarDays size={20} /> },
        { path: '/doctor/dashboard/availability', label: 'Availability Rules', icon: <Clock size={20} /> },
        { path: '/doctor/dashboard/prescriptions', label: 'Prescriptions', icon: <FileEdit size={20} /> },
        { path: '/doctor/dashboard/settings', label: 'Profile Settings', icon: <Settings size={20} /> },
    ];

    return (
        <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: 'var(--bg-gradient-start)' }}>
            {/* Sidebar */}
            <aside style={{ width: '260px', backgroundColor: 'var(--surface)', borderRight: '1px solid var(--border)', display: 'flex', flexDirection: 'column' }}>
                <div style={{ padding: '2rem 1.5rem', borderBottom: '1px solid var(--border)' }}>
                    <h2 style={{ color: 'var(--primary)', fontWeight: 700, margin: 0 }}>HMS Doctor</h2>
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>{user?.email}</p>
                </div>

                <nav style={{ flex: 1, padding: '1.5rem 1rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {navItems.map(item => {
                        const isActive = location.pathname === item.path || (location.pathname.startsWith(item.path) && item.path !== '/doctor/dashboard');

                        // Special check for exact dashboard path
                        const isExactDashboard = item.path === '/doctor/dashboard' && location.pathname === '/doctor/dashboard';

                        return (
                            <Link
                                key={item.path}
                                to={item.path}
                                style={{
                                    display: 'flex', alignItems: 'center', gap: '1rem', padding: '0.75rem 1rem',
                                    textDecoration: 'none', borderRadius: 'var(--radius-sm)',
                                    backgroundColor: (isActive && item.path !== '/doctor/dashboard') || isExactDashboard ? 'var(--primary)' : 'transparent',
                                    color: (isActive && item.path !== '/doctor/dashboard') || isExactDashboard ? 'white' : 'var(--text-main)',
                                    fontWeight: (isActive && item.path !== '/doctor/dashboard') || isExactDashboard ? 600 : 500,
                                    transition: 'all 0.2s ease'
                                }}
                            >
                                {item.icon}
                                {item.label}
                            </Link>
                        );
                    })}
                </nav>

                <div style={{ padding: '1.5rem 1rem', borderTop: '1px solid var(--border)' }}>
                    <button onClick={handleLogout} style={{
                        display: 'flex', alignItems: 'center', gap: '1rem', width: '100%', padding: '0.75rem 1rem',
                        background: 'none', border: 'none', color: 'var(--error)', fontWeight: 500, cursor: 'pointer'
                    }}>
                        <LogOut size={20} />
                        Logout
                    </button>
                </div>
            </aside>

            {/* Main Content Area */}
            <main style={{ flex: 1, padding: '2rem', overflowY: 'auto' }}>
                <Outlet />
            </main>
        </div>
    );
};

// Exporting DoctorSchedule inline for simplicity in this phase
export const DoctorSchedule = () => {
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);

    const loadSchedule = async () => {
        try {
            const res = await api.get('/appointments/doctor-history');
            // Filter to only show PENDING and CONFIRMED (this is a simplified dashboard view)
            const upcoming = (res.data.content || []).filter(a => a.status === 'PENDING' || a.status === 'CONFIRMED');
            setAppointments(upcoming);
        } catch (e) {
            toast.error("Failed to load schedule");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadSchedule();
    }, []);

    const markComplete = async (id) => {
        try {
            await api.put(`/appointments/${id}/status`, { status: 'COMPLETED' });
            toast.success("Appointment completed successfully");
            loadSchedule();
        } catch (e) {
            toast.error("Failed to update status");
        }
    };

    if (loading) return <div>Loading Schedule...</div>;

    return (
        <div>
            <h1 style={{ marginBottom: '2rem' }}>My Upcoming Schedule</h1>

            {appointments.length === 0 ? (
                <div className="glass-panel" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                    No upcoming appointments.
                </div>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {appointments.map(appt => (
                        <div key={appt.id} className="glass-panel" style={{ padding: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <div>
                                <h3 style={{ margin: '0 0 0.5rem 0' }}>Patient: {appt.patientName}</h3>
                                <p style={{ margin: '0 0 0.5rem 0', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <Clock size={16} /> {appt.appointmentDate} at {appt.startTime}
                                </p>
                                <p style={{ margin: 0, fontSize: '0.9rem' }}><strong>Reason:</strong> {appt.reasonForVisit || 'None provided'}</p>
                            </div>

                            <button
                                onClick={() => markComplete(appt.id)}
                                className="btn"
                                style={{ background: '#ecfdf5', color: '#059669', border: '1px solid #34d399', width: 'auto', display: 'flex', gap: '0.5rem' }}
                            >
                                <CheckCircle size={18} /> Mark Complete
                            </button>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default DoctorDashboard;
