import React, { useContext } from 'react';
import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { AuthContext } from '../../context/AuthContext';
import { Users, FileCheck, LogOut, LayoutDashboard, Calendar } from 'lucide-react';

const AdminDashboard = () => {
    const { user, logout } = useContext(AuthContext);
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    const navItems = [
        { path: '/admin', label: 'Overview', icon: <LayoutDashboard size={20} /> },
        { path: '/admin/pending-doctors', label: 'Doctor Approvals', icon: <FileCheck size={20} /> },
        { path: '/admin/users', label: 'Manage Users', icon: <Users size={20} /> },
        { path: '/admin/appointments', label: 'Appointments', icon: <Calendar size={20} /> },
    ];

    return (
        <div style={{ display: 'flex', minHeight: '100vh', backgroundColor: 'var(--bg-gradient-start)' }}>
            {/* Sidebar */}
            <aside style={{ width: '260px', backgroundColor: 'var(--surface)', borderRight: '1px solid var(--border)', display: 'flex', flexDirection: 'column' }}>
                <div style={{ padding: '2rem 1.5rem', borderBottom: '1px solid var(--border)' }}>
                    <h2 style={{ color: 'var(--primary)', fontWeight: 700, margin: 0 }}>HMS Admin</h2>
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

export default AdminDashboard;
