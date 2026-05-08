import React, { useState } from 'react';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { Clock, PlusCircle, Trash2 } from 'lucide-react';

const DoctorAvailability = () => {
    const [rules, setRules] = useState([
        { dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00', slotDurationMinutes: 30 }
    ]);
    const [loading, setLoading] = useState(false);

    const daysEnum = [
        { val: 'MONDAY', label: 'Monday' }, { val: 'TUESDAY', label: 'Tuesday' },
        { val: 'WEDNESDAY', label: 'Wednesday' }, { val: 'THURSDAY', label: 'Thursday' },
        { val: 'FRIDAY', label: 'Friday' }, { val: 'SATURDAY', label: 'Saturday' },
        { val: 'SUNDAY', label: 'Sunday' }
    ];

    const handleRuleChange = (index, field, value) => {
        const newRules = [...rules];
        newRules[index][field] = value;
        setRules(newRules);
    };

    const addRule = () => {
        setRules([...rules, { dayOfWeek: 'MONDAY', startTime: '09:00', endTime: '17:00', slotDurationMinutes: 30 }]);
    };

    const removeRule = (index) => {
        setRules(rules.filter((_, i) => i !== index));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (rules.length === 0) {
            toast.error('Please add at least one availability rule.');
            return;
        }

        // Map data to backend format: integer duration and HH:mm:ss for times
        const mappedRules = rules.map(r => ({
            ...r,
            slotDurationMinutes: parseInt(r.slotDurationMinutes, 10),
            // Ensure times are in HH:mm:ss format for Java LocalTime
            startTime: r.startTime.length === 5 ? `${r.startTime}:00` : r.startTime,
            endTime: r.endTime.length === 5 ? `${r.endTime}:00` : r.endTime
        }));

        setLoading(true);
        try {
            await api.post('/doctors/availability', { rules: mappedRules });
            toast.success('Working hours successfully updated!');
        } catch (error) {
            // Show concrete error if available
            const detail = error.response?.data?.message || error.response?.data?.detail;
            toast.error(detail || 'Failed to update schedule settings.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <h1 style={{ marginBottom: '2rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <Clock size={28} color="var(--primary)" /> Availability Rules
            </h1>

            <div className="glass-panel" style={{ padding: '2rem' }}>
                <p style={{ marginBottom: '2rem', color: 'var(--text-muted)' }}>
                    Configure the days and times you are available for patient appointments.
                    Note: Saving new rules will overwrite your previous configurations. It will not cancel existing booked patient appointments.
                </p>

                <form onSubmit={handleSubmit}>
                    {rules.map((rule, idx) => (
                        <div key={idx} style={{ display: 'flex', gap: '1rem', alignItems: 'flex-end', marginBottom: '1.5rem', padding: '1.5rem', background: 'var(--bg-gradient-start)', border: '1px solid var(--border)', borderRadius: 'var(--radius-lg)' }}>
                            <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
                                <label style={{ fontSize: '0.85rem' }}>Day of Week</label>
                                <select className="form-control" value={rule.dayOfWeek} onChange={e => handleRuleChange(idx, 'dayOfWeek', e.target.value)} required>
                                    {daysEnum.map(d => <option key={d.val} value={d.val}>{d.label}</option>)}
                                </select>
                            </div>

                            <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
                                <label style={{ fontSize: '0.85rem' }}>Start Time (24H)</label>
                                <input type="time" className="form-control" value={rule.startTime} onChange={e => handleRuleChange(idx, 'startTime', e.target.value)} required />
                            </div>

                            <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
                                <label style={{ fontSize: '0.85rem' }}>End Time (24H)</label>
                                <input type="time" className="form-control" value={rule.endTime} onChange={e => handleRuleChange(idx, 'endTime', e.target.value)} required />
                            </div>

                            <div className="form-group" style={{ flex: 1, marginBottom: 0 }}>
                                <label style={{ fontSize: '0.85rem' }}>Slot Duration (Mins)</label>
                                <select className="form-control" value={rule.slotDurationMinutes} onChange={e => handleRuleChange(idx, 'slotDurationMinutes', e.target.value)} required>
                                    <option value="15">15 Minutes</option>
                                    <option value="30">30 Minutes</option>
                                    <option value="45">45 Minutes</option>
                                    <option value="60">60 Minutes</option>
                                </select>
                            </div>

                            <button type="button" onClick={() => removeRule(idx)} className="btn btn-outline" style={{ background: '#fef2f2', color: '#dc2626', border: '1px solid #f87171', width: 'auto', padding: '0.75rem 1rem' }}>
                                <Trash2 size={18} />
                            </button>
                        </div>
                    ))}

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '2rem' }}>
                        <button type="button" onClick={addRule} className="btn" style={{ width: 'auto', display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'transparent', color: 'var(--primary)', border: '2px dashed var(--primary)', padding: '0.75rem 1.5rem' }}>
                            <PlusCircle size={18} /> Add Time Slot
                        </button>

                        <button type="submit" className="btn btn-primary" disabled={loading} style={{ width: 'auto', padding: '0.75rem 3rem' }}>
                            {loading ? 'Saving Rules...' : 'Save Availability'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default DoctorAvailability;
