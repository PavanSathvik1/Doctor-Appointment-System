import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { ClipboardList, Download, FileText, Pill } from 'lucide-react';

const PatientPrescriptions = () => {
    const [prescriptions, setPrescriptions] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchPrescriptions = async () => {
            try {
                const res = await api.get('/prescriptions/my-prescriptions');
                setPrescriptions(res.data.content || []);
            } catch (e) {
                toast.error('Failed to load prescriptions');
            } finally {
                setLoading(false);
            }
        };
        fetchPrescriptions();
    }, []);

    if (loading) return <div>Loading records...</div>;

    return (
        <div>
            <h1 style={{ marginBottom: '2rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <ClipboardList size={28} color="var(--primary)" /> My Prescriptions
            </h1>

            {prescriptions.length === 0 ? (
                <div className="glass-panel" style={{ padding: '3rem 2rem', textAlign: 'center' }}>
                    <FileText size={48} color="var(--text-muted)" style={{ margin: '0 auto 1rem auto', opacity: 0.5 }} />
                    <h3 style={{ color: 'var(--text-main)' }}>No Prescriptions Found</h3>
                    <p style={{ color: 'var(--text-muted)' }}>You do not have any prescriptions history yet.</p>
                </div>
            ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                    {prescriptions.map(presc => (
                        <div key={presc.id} className="glass-panel" style={{ padding: '0', overflow: 'hidden' }}>

                            {/* Header Box */}
                            <div style={{ background: 'var(--surface)', padding: '1.5rem', borderBottom: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                <div>
                                    <h3 style={{ margin: '0 0 0.25rem 0', color: 'var(--text-main)' }}>Diagnosis: {presc.diagnosis}</h3>
                                    <p style={{ margin: 0, color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                                        Issued by <strong>{presc.doctorName}</strong> on {new Date(presc.createdAt).toLocaleDateString()}
                                    </p>
                                </div>

                                {presc.pdfUrl && (
                                    <a href={presc.pdfUrl} target="_blank" rel="noreferrer" className="btn btn-primary" style={{ width: 'auto', display: 'flex', alignItems: 'center', gap: '0.5rem', padding: '0.75rem 1rem', textDecoration: 'none' }}>
                                        <Download size={18} /> Download Document
                                    </a>
                                )}
                            </div>

                            {/* Medicines List */}
                            <div style={{ padding: '1.5rem' }}>
                                <h4 style={{ margin: '0 0 1rem 0', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                    <Pill size={16} /> Prescribed Medication
                                </h4>

                                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1rem' }}>
                                    {presc.items.map(item => (
                                        <div key={item.id} style={{ padding: '1rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-sm)', background: 'var(--bg-gradient-start)' }}>
                                            <div style={{ fontWeight: 600, color: 'var(--primary)', marginBottom: '0.5rem' }}>{item.medicineName} {item.dosage}</div>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-main)', marginBottom: '0.25rem' }}>
                                                <span>Frequency:</span> <strong>{item.frequency}</strong>
                                            </div>
                                            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-main)', marginBottom: '0.5rem' }}>
                                                <span>Duration:</span> <strong>{item.durationDays} Days</strong>
                                            </div>
                                            {item.instructions && (
                                                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontStyle: 'italic', background: 'var(--surface)', padding: '0.5rem', borderRadius: 'var(--radius-sm)' }}>
                                                    "{item.instructions}"
                                                </div>
                                            )}
                                        </div>
                                    ))}
                                </div>

                                {presc.notes && (
                                    <div style={{ marginTop: '1.5rem', padding: '1rem', background: '#fffbeb', color: '#b45309', borderRadius: 'var(--radius-sm)', fontSize: '0.9rem' }}>
                                        <strong>Doctor's Notes:</strong> {presc.notes}
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

export default PatientPrescriptions;
