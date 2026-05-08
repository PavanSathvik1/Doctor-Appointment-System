import React, { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import { Calendar, Clock, FileText, User, ArrowRight, HeartPulse, Activity, ClipboardList } from 'lucide-react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';

const PatientOverview = () => {
    const [overview, setOverview] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchOverview = async () => {
            try {
                const response = await api.get('/patients/overview');
                setOverview(response.data);
            } catch (error) {
                toast.error('Failed to load health activity summary');
            } finally {
                setLoading(false);
            }
        };
        fetchOverview();
    }, []);

    if (loading) return <div className="loading-spinner">Loading health summary...</div>;

    const stats = [
        { label: 'Upcoming Appts', value: overview?.totalAppointments || 0, icon: <Calendar size={20} />, color: '#4facfe' },
        { label: 'Prescriptions', value: overview?.totalPrescriptions || 0, icon: <ClipboardList size={20} />, color: '#00f2fe' },
        { label: 'Active Goals', value: 0, icon: <Activity size={20} />, color: '#f093fb' },
        { label: 'Health Score', value: 'N/A', icon: <HeartPulse size={20} />, color: '#f6d365' },
    ];

    return (
        <div style={{ padding: '1rem' }}>
            <h2 style={{ marginBottom: '2rem', color: 'var(--text-main)' }}>Your Health Overview</h2>

            {/* Stats Row */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem', marginBottom: '3rem' }}>
                {stats.map((stat, idx) => (
                    <div key={idx} className="glass-panel" style={{ padding: '1.25rem', display: 'flex', alignItems: 'center', gap: '1rem' }}>
                        <div style={{ padding: '0.75rem', borderRadius: '12px', backgroundColor: 'rgba(255,255,255,0.05)', color: stat.color }}>
                            {stat.icon}
                        </div>
                        <div>
                            <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--text-muted)', fontWeight: 500 }}>{stat.label}</p>
                            <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 700, color: 'var(--text-main)' }}>{stat.value}</h3>
                        </div>
                    </div>
                ))}
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(350px, 1fr))', gap: '2rem' }}>
                {/* Next Appointment Card */}
                <div className="glass-panel" style={{ padding: '2rem', display: 'flex', flexDirection: 'column' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                        <h3 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                            <Calendar className="text-secondary" />
                            Next Appointment
                        </h3>
                        <Link to="/dashboard/appointments" style={{ textDecoration: 'none', color: 'var(--primary)', fontSize: '0.875rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                            View All <ArrowRight size={16} />
                        </Link>
                    </div>

                    {overview?.nextAppointment ? (
                        <div style={{ flex: 1, padding: '1.5rem', backgroundColor: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                                <div style={{ width: '48px', height: '48px', borderRadius: '50%', backgroundColor: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontWeight: 700, fontSize: '1.25rem' }}>
                                    {overview.nextAppointment.doctorName.split(' ')[1]?.[0] || 'D'}
                                </div>
                                <div>
                                    <h4 style={{ margin: 0 }}>Dr. {overview.nextAppointment.doctorName}</h4>
                                    <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--text-muted)' }}>Cardiologist</p>
                                </div>
                            </div>
                            <div style={{ display: 'flex', gap: '2rem', fontSize: '0.875rem' }}>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <Calendar size={16} className="text-muted" />
                                    <span>{new Date(overview.nextAppointment.appointmentDate).toLocaleDateString(undefined, { month: 'short', day: 'numeric', year: 'numeric' })}</span>
                                </div>
                                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <Clock size={16} className="text-muted" />
                                    <span>{overview.nextAppointment.startTime?.slice(0, 5)}</span>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '2rem', backgroundColor: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius-sm)', border: '1px dashed var(--border)' }}>
                            <p style={{ color: 'var(--text-muted)', textAlign: 'center', marginBottom: '1.5rem' }}>You have no upcoming appointments scheduled.</p>
                            <Link to="/dashboard/search" className="btn-primary" style={{ padding: '0.5rem 1.5rem', fontSize: '0.875rem' }}>
                                Book Appointment Now
                            </Link>
                        </div>
                    )}
                </div>

                {/* Latest Prescription Card */}
                <div className="glass-panel" style={{ padding: '2rem', display: 'flex', flexDirection: 'column' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
                        <h3 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                            <FileText className="text-primary" />
                            Latest Prescription
                        </h3>
                        <Link to="/dashboard/prescriptions" style={{ textDecoration: 'none', color: 'var(--primary)', fontSize: '0.875rem', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                            History <ArrowRight size={16} />
                        </Link>
                    </div>

                    {overview?.latestPrescription ? (
                        <div style={{ flex: 1, padding: '1.5rem', backgroundColor: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)' }}>
                            <h4 style={{ margin: '0 0 1rem 0' }}>Diagnois: {overview.latestPrescription.diagnosis}</h4>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                                {(overview.latestPrescription.items || []).slice(0, 2).map((med, i) => (
                                    <div key={i} style={{ padding: '0.75rem', backgroundColor: 'rgba(255,255,255,0.03)', borderRadius: '8px', fontSize: '0.875rem', display: 'flex', justifyContent: 'space-between' }}>
                                        <span style={{ fontWeight: 600 }}>{med.medicineName}</span>
                                        <span style={{ color: 'var(--text-muted)' }}>{med.dosage}</span>
                                    </div>
                                ))}
                                {(overview.latestPrescription.items || []).length > 2 && (
                                    <p style={{ margin: 0, fontSize: '0.75rem', color: 'var(--primary)', textAlign: 'center' }}>
                                        + {(overview.latestPrescription.items || []).length - 2} more medications
                                    </p>
                                )}
                            </div>
                        </div>
                    ) : (
                        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '2rem', backgroundColor: 'rgba(255,255,255,0.02)', borderRadius: 'var(--radius-sm)', border: '1px dashed var(--border)' }}>
                            <p style={{ color: 'var(--text-muted)', textAlign: 'center' }}>No prescription history found in your account.</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default PatientOverview;
