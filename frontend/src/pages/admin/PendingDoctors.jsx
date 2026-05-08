import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { CheckCircle, XCircle, ExternalLink } from 'lucide-react';

const PendingDoctors = () => {
    const [doctors, setDoctors] = useState([]);
    const [loading, setLoading] = useState(true);
    const [rejectingId, setRejectingId] = useState(null);
    const [reason, setReason] = useState('');

    const fetchPending = async () => {
        try {
            const res = await api.get('/doctors/admin/pending');
            setDoctors(res.data.content || []);
        } catch (e) {
            toast.error('Failed to load pending applications');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPending();
    }, []);

    const handleDecision = async (id, approve) => {
        if (!approve && !reason) {
            setRejectingId(id); // Open rejection reason modal/input
            return;
        }

        try {
            await api.put(`/doctors/admin/${id}/approve`, {
                approve,
                rejectionReason: approve ? null : reason
            });
            toast.success(`Application ${approve ? 'approved' : 'rejected'}`);
            setRejectingId(null);
            setReason('');
            fetchPending(); // Refresh list
        } catch (e) {
            toast.error(e.response?.data?.detail || 'Failed to process application');
        }
    };

    if (loading) return <div>Loading applications...</div>;

    return (
        <div>
            <h1 style={{ marginBottom: '2rem' }}>Pending Doctor Applications</h1>

            {doctors.length === 0 ? (
                <div className="glass-panel" style={{ padding: '2rem', textAlign: 'center' }}>
                    <p style={{ color: 'var(--text-muted)' }}>No pending applications to review.</p>
                </div>
            ) : (
                <div style={{ display: 'grid', gap: '1.5rem' }}>
                    {doctors.map(doc => (
                        <div key={doc.id} className="glass-panel" style={{ padding: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                            <div>
                                <h3 style={{ margin: '0 0 0.5rem 0' }}>Dr. {doc.firstName} {doc.lastName}</h3>
                                <p style={{ margin: '0 0 1rem 0', color: 'var(--text-muted)' }}>
                                    {doc.specialisation} | Exp: {doc.experienceYears}y | Fee: ${doc.consultationFee}
                                </p>
                                <div style={{ display: 'flex', gap: '2rem', fontSize: '0.9rem' }}>
                                    <div><strong>Licence:</strong> {doc.licenceNumber}</div>
                                    <div><strong>Phone:</strong> {doc.phone}</div>
                                </div>

                                {doc.documentUrl && (
                                    <div style={{ marginTop: '1rem' }}>
                                        <a href={doc.documentUrl} target="_blank" rel="noreferrer"
                                            style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem', color: 'var(--primary)', textDecoration: 'none', fontWeight: 500 }}>
                                            <ExternalLink size={16} /> View Verification Document
                                        </a>
                                    </div>
                                )}
                            </div>

                            <div style={{ display: 'flex', gap: '0.75rem', flexDirection: 'column' }}>
                                {rejectingId === doc.id ? (
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                                        <input
                                            type="text"
                                            placeholder="Reason for rejection"
                                            className="form-control"
                                            value={reason}
                                            onChange={e => setReason(e.target.value)}
                                        />
                                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                                            <button className="btn" style={{ background: 'var(--error)', color: 'white', padding: '0.5rem' }} onClick={() => handleDecision(doc.id, false)}>Confirm Reject</button>
                                            <button className="btn" style={{ background: '#e2e8f0', color: 'var(--text-main)', padding: '0.5rem' }} onClick={() => setRejectingId(null)}>Cancel</button>
                                        </div>
                                    </div>
                                ) : (
                                    <div style={{ display: 'flex', gap: '0.75rem' }}>
                                        <button
                                            onClick={() => handleDecision(doc.id, true)}
                                            style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.5rem 1rem', background: '#ecfdf5', color: '#059669', border: '1px solid #34d399', borderRadius: 'var(--radius-sm)', cursor: 'pointer', fontWeight: 500 }}
                                        >
                                            <CheckCircle size={18} /> Approve
                                        </button>
                                        <button
                                            onClick={() => setRejectingId(doc.id)}
                                            style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.5rem 1rem', background: '#fef2f2', color: '#dc2626', border: '1px solid #f87171', borderRadius: 'var(--radius-sm)', cursor: 'pointer', fontWeight: 500 }}
                                        >
                                            <XCircle size={18} /> Reject
                                        </button>
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default PendingDoctors;
