import React, { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import { Users, UserPlus, Calendar, Mail } from 'lucide-react';
import { toast } from 'react-toastify';

const AdminOverview = () => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const response = await api.get('/admin/stats');
                setStats(response.data);
            } catch (error) {
                toast.error('Failed to load system statistics');
            } finally {
                setLoading(false);
            }
        };
        fetchStats();
    }, []);

    if (loading) return <div className="loading-spinner">Loading stats...</div>;

    const statCards = [
        { label: 'Total Patients', value: stats?.totalPatients, icon: <Users size={24} />, color: '#4facfe' },
        { label: 'Total Doctors', value: stats?.totalDoctors, icon: <UserPlus size={24} />, color: '#00f2fe' },
        { label: 'Today\'s Appointments', value: stats?.todayAppointments, icon: <Calendar size={24} />, color: '#f093fb' },
        { label: 'Pending Approvals', value: stats?.pendingDoctorRegistrations, icon: <Mail size={24} />, color: '#f6d365' },
    ];

    return (
        <div style={{ padding: '1rem' }}>
            <h2 style={{ marginBottom: '2rem', color: 'var(--text-main)' }}>System Overview</h2>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.5rem' }}>
                {statCards.map((card, idx) => (
                    <div
                        key={idx}
                        className="glass-panel"
                        style={{
                            padding: '1.5rem',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '1.25rem',
                            transition: 'transform 0.2s cubic-bezier(0.4, 0, 0.2, 1)',
                            borderLeft: `5px solid ${card.color}`
                        }}
                    >
                        <div style={{ padding: '0.75rem', borderRadius: '12px', backgroundColor: 'rgba(255,255,255,0.05)', color: card.color }}>
                            {card.icon}
                        </div>
                        <div>
                            <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--text-muted)', fontWeight: 500 }}>{card.label}</p>
                            <h3 style={{ margin: 0, fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-main)' }}>{card.value}</h3>
                        </div>
                    </div>
                ))}
            </div>

            <div style={{ marginTop: '3rem' }} className="glass-panel">
                <div style={{ padding: '2rem', textAlign: 'center' }}>
                    <h3 style={{ color: 'var(--primary)' }}>Administrative Control Panel</h3>
                    <p style={{ color: 'var(--text-muted)', maxWidth: '600px', margin: '1rem auto' }}>
                        Welcome to the HMS Management Console. Use the sidebar to manage system users,
                        approve new doctor registrations, and monitor all scheduled appointments across the facility.
                    </p>
                </div>
            </div>
        </div>
    );
};

export default AdminOverview;
