import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { PlusCircle, Trash2, FileEdit } from 'lucide-react';

const IssuePrescription = () => {
    const [appointments, setAppointments] = useState([]);
    const [selectedAppt, setSelectedAppt] = useState('');
    const [diagnosis, setDiagnosis] = useState('');
    const [notes, setNotes] = useState('');
    const [items, setItems] = useState([
        { medicineName: '', dosage: '', frequency: '', durationDays: '', instructions: '' }
    ]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    // Load completed appointments that don't have prescriptions yet
    useEffect(() => {
        const loadAppointments = async () => {
            try {
                const res = await api.get('/appointments/doctor-history?size=50'); // Pull recent history
                const completed = (res.data.content || []).filter(a => a.status === 'COMPLETED');
                setAppointments(completed);
            } catch (e) {
                toast.error('Failed to load eligible appointments');
            }
        };
        loadAppointments();
    }, []);

    const handleItemChange = (index, field, value) => {
        const newItems = [...items];
        newItems[index][field] = value;
        setItems(newItems);
    };

    const addItem = () => {
        setItems([...items, { medicineName: '', dosage: '', frequency: '', durationDays: '', instructions: '' }]);
    };

    const removeItem = (index) => {
        if (items.length === 1) return;
        const newItems = items.filter((_, i) => i !== index);
        setItems(newItems);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!selectedAppt) {
            toast.error('Please select an appointment');
            return;
        }

        // Validate items
        for (let item of items) {
            if (!item.medicineName || !item.dosage || !item.frequency || !item.durationDays) {
                toast.error('Please fill all required medicine fields');
                return;
            }
        }

        setLoading(true);
        try {
            await api.post('/prescriptions/issue', {
                appointmentId: selectedAppt,
                diagnosis,
                notes,
                items: items.map(i => ({ ...i, durationDays: parseInt(i.durationDays, 10) }))
            });

            toast.success('Prescription issued and PDF generated successfully!');
            navigate('/doctor/dashboard'); // Go back to dashboard
        } catch (error) {
            toast.error(error.response?.data?.detail || 'Failed to issue prescription');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <h1 style={{ marginBottom: '2rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <FileEdit size={28} color="var(--primary)" /> Issue Prescription
            </h1>

            <div className="glass-panel" style={{ padding: '2rem' }}>
                <form onSubmit={handleSubmit}>

                    <div className="form-group">
                        <label>Select Completed Appointment</label>
                        <select
                            className="form-control"
                            value={selectedAppt}
                            onChange={e => setSelectedAppt(e.target.value)}
                            required
                        >
                            <option value="">-- Select Patient Appointment --</option>
                            {appointments.map(appt => (
                                <option key={appt.id} value={appt.id}>
                                    {appt.patientName} - {appt.appointmentDate}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="form-group">
                        <label>Clinical Diagnosis</label>
                        <input
                            type="text"
                            className="form-control"
                            value={diagnosis}
                            onChange={e => setDiagnosis(e.target.value)}
                            placeholder="e.g. Acute Viral Pharyngitis"
                            required
                        />
                    </div>

                    {/* Dynamic Medicines Form */}
                    <div style={{ marginTop: '2.5rem', marginBottom: '1.5rem' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                            <h3 style={{ margin: 0, color: 'var(--text-main)' }}>Medicines</h3>
                            <button type="button" onClick={addItem} className="btn" style={{ width: 'auto', display: 'flex', alignItems: 'center', gap: '0.5rem', background: '#e0f2fe', color: 'var(--primary)', border: '1px solid #bae6fd', padding: '0.5rem 1rem' }}>
                                <PlusCircle size={16} /> Add Medicine
                            </button>
                        </div>

                        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                            {items.map((item, index) => (
                                <div key={index} style={{ padding: '1.5rem', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)', position: 'relative', background: 'var(--bg-gradient-start)' }}>

                                    {items.length > 1 && (
                                        <button type="button" onClick={() => removeItem(index)} style={{ position: 'absolute', top: '1rem', right: '1rem', background: 'none', border: 'none', color: 'var(--error)', cursor: 'pointer' }}>
                                            <Trash2 size={18} />
                                        </button>
                                    )}

                                    <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
                                        <div>
                                            <label style={{ fontSize: '0.85rem', marginBottom: '0.3rem', display: 'block', color: 'var(--text-muted)' }}>Medicine Name</label>
                                            <input type="text" className="form-control" value={item.medicineName} onChange={e => handleItemChange(index, 'medicineName', e.target.value)} required />
                                        </div>
                                        <div>
                                            <label style={{ fontSize: '0.85rem', marginBottom: '0.3rem', display: 'block', color: 'var(--text-muted)' }}>Dosage (e.g. 500mg)</label>
                                            <input type="text" className="form-control" value={item.dosage} onChange={e => handleItemChange(index, 'dosage', e.target.value)} required />
                                        </div>
                                        <div>
                                            <label style={{ fontSize: '0.85rem', marginBottom: '0.3rem', display: 'block', color: 'var(--text-muted)' }}>Frequency</label>
                                            <input type="text" className="form-control" value={item.frequency} onChange={e => handleItemChange(index, 'frequency', e.target.value)} placeholder="e.g. 1-0-1" required />
                                        </div>
                                        <div>
                                            <label style={{ fontSize: '0.85rem', marginBottom: '0.3rem', display: 'block', color: 'var(--text-muted)' }}>Days</label>
                                            <input type="number" className="form-control" value={item.durationDays} onChange={e => handleItemChange(index, 'durationDays', e.target.value)} required min="1" />
                                        </div>
                                    </div>

                                    <div>
                                        <label style={{ fontSize: '0.85rem', marginBottom: '0.3rem', display: 'block', color: 'var(--text-muted)' }}>Additional Instructions (Optional)</label>
                                        <input type="text" className="form-control" value={item.instructions} onChange={e => handleItemChange(index, 'instructions', e.target.value)} placeholder="e.g. Take after meals" />
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    <div className="form-group" style={{ marginBottom: '2rem' }}>
                        <label>Additional Clinical Notes (Optional)</label>
                        <textarea className="form-control" rows="3" value={notes} onChange={e => setNotes(e.target.value)}></textarea>
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={loading}>
                        {loading ? 'Generating Secure PDF...' : 'Issue Prescription'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default IssuePrescription;
