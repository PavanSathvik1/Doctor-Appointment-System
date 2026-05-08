import React, { useContext } from 'react';
import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import { Search, CalendarDays, ClipboardList, LogOut, Activity } from 'lucide-react';

const PatientDashboard = () => {
    const { user, logout } = useContext(AuthContext);
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    const navItems = [
        { path: '/dashboard', label: 'My Health Overview', icon: <Activity size={20} /> },
        { path: '/dashboard/search', label: 'Find a Doctor', icon: <Search size={20} /> },
        { path: '/dashboard/appointments', label: 'My Appointments', icon: <CalendarDays size={20} /> },
        { path: '/dashboard/prescriptions', label: 'Prescriptions', icon: <ClipboardList size={20} /> },
    ];

    return (
        <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: 'var(--bg-gradient-start)' }}>
            {/* Sidebar */}
            <aside style={{ width: '260px', backgroundColor: 'var(--surface)', borderRight: '1px solid var(--border)', display: 'flex', flexDirection: 'column' }}>
                <div style={{ padding: '2rem 1.5rem', borderBottom: '1px solid var(--border)' }}>
                    <h2 style={{ color: 'var(--primary)', fontWeight: 700, margin: 0 }}>HMS Patient</h2>
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>{user?.email}</p>
                </div>

                <nav style={{ flex: 1, padding: '1.5rem 1rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    {navItems.map(item => {
                        const isActive = location.pathname === item.path;
                        return (
                            <Link
                                key={item.path}
                                to={item.path}
                                style={{
                                    display: 'flex', alignItems: 'center', gap: '1rem', padding: '0.75rem 1rem',
                                    textDecoration: 'none', borderRadius: 'var(--radius-sm)',
                                    backgroundColor: isActive ? 'var(--primary)' : 'transparent',
                                    color: isActive ? 'white' : 'var(--text-main)',
                                    fontWeight: isActive ? 600 : 500,
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

export default PatientDashboard;
