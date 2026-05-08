import React, { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import { Calendar, Clock, User, Stethoscope, Search, Filter } from 'lucide-react';
import { toast } from 'react-toastify';

const AllAppointmentsAdmin = () => {
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const fetchAppointments = async () => {
        try {
            const response = await api.get(`/appointments/admin?page=${page}&size=10`);
            setAppointments(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            toast.error('Failed to load system-wide appointments');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAppointments();
    }, [page]);

    const getStatusColor = (status) => {
        switch (status) {
            case 'PENDING': return { bg: 'rgba(255, 150, 75, 0.1)', color: '#ff964b' };
            case 'CONFIRMED': return { bg: 'rgba(75, 150, 255, 0.1)', color: '#4b96ff' };
            case 'COMPLETED': return { bg: 'rgba(75, 255, 150, 0.1)', color: '#4bff96' };
            case 'CANCELLED': return { bg: 'rgba(255, 75, 75, 0.1)', color: '#ff4b4b' };
            default: return { bg: 'rgba(255, 255, 255, 0.1)', color: '#fff' };
        }
    };

    if (loading) return <div className="loading-spinner">Loading appointments...</div>;

    return (
        <div style={{ padding: '1rem' }}>
            <h2 style={{ marginBottom: '2rem', color: 'var(--text-main)' }}>System-wide Appointments</h2>

            <div className="glass-panel" style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', minWidth: '800px' }}>
                    <thead style={{ backgroundColor: 'rgba(255,255,255,0.05)' }}>
                        <tr>
                            <th style={{ padding: '1rem 1.5rem', fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)' }}>Date & Time</th>
                            <th style={{ padding: '1rem 1.5rem', fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)' }}>Patient</th>
                            <th style={{ padding: '1rem 1.5rem', fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)' }}>Doctor</th>
                            <th style={{ padding: '1rem 1.5rem', fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)' }}>Status</th>
                            <th style={{ padding: '1rem 1.5rem', fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)' }}>Reason</th>
                        </tr>
                    </thead>
                    <tbody>
                        {appointments.map((appt) => {
                            const statusStyle = getStatusColor(appt.status);
                            return (
                                <tr key={appt.id} style={{ borderBottom: '1px solid var(--border)' }}>
                                    <td style={{ padding: '1rem 1.5rem' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                                            <Calendar size={18} className="text-secondary" />
                                            <div>
                                                <div style={{ fontWeight: 600 }}>{new Date(appt.appointmentDate).toLocaleDateString()}</div>
                                                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{appt.startTime.slice(0, 5)} - {appt.endTime.slice(0, 5)}</div>
                                            </div>
                                        </div>
                                    </td>
                                    <td style={{ padding: '1rem 1.5rem' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                                            <User size={18} className="text-primary" />
                                            <span>{appt.patientName}</span>
                                        </div>
                                    </td>
                                    <td style={{ padding: '1rem 1.5rem' }}>
                                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                                            <Stethoscope size={18} color="#00f2fe" />
                                            <span>{appt.doctorName}</span>
                                        </div>
                                    </td>
                                    <td style={{ padding: '1rem 1.5rem' }}>
                                        <span style={{
                                            padding: '0.25rem 0.75rem', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 600,
                                            backgroundColor: statusStyle.bg, color: statusStyle.color
                                        }}>
                                            {appt.status}
                                        </span>
                                    </td>
                                    <td style={{ padding: '1rem 1.5rem', fontSize: '0.875rem', maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={appt.reasonForVisit}>
                                        {appt.reasonForVisit}
                                    </td>
                                </tr>
                            );
                        })}
                    </tbody>
                </table>

                {/* Pagination */}
                <div style={{ padding: '1.5rem', display: 'flex', justifyContent: 'center', gap: '1rem' }}>
                    <button
                        disabled={page === 0}
                        onClick={() => setPage(page - 1)}
                        className="btn-secondary" style={{ padding: '0.5rem 1rem' }}
                    >
                        Prev
                    </button>
                    <span style={{ alignSelf: 'center', color: 'var(--text-muted)' }}>Page {page + 1} of {totalPages}</span>
                    <button
                        disabled={page >= totalPages - 1}
                        onClick={() => setPage(page + 1)}
                        className="btn-secondary" style={{ padding: '0.5rem 1rem' }}
                    >
                        Next
                    </button>
                </div>
            </div>
        </div>
    );
};

export default AllAppointmentsAdmin;
