import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { Calendar, Clock, User, ArrowLeft } from 'lucide-react';

const BookAppointment = () => {
    const { doctorId } = useParams();
    const navigate = useNavigate();

    const [doctor, setDoctor] = useState(null);
    const [date, setDate] = useState('');
    const [slots, setSlots] = useState([]);
    const [selectedSlot, setSelectedSlot] = useState(null);
    const [reason, setReason] = useState('');
    const [loadingConfig, setLoadingConfig] = useState({ fetch: true, slots: false, book: false });

    // Load Doctor Details
    useEffect(() => {
        const fetchDoc = async () => {
            try {
                const res = await api.get(`/doctors/${doctorId}`);
                setDoctor(res.data);
            } catch (e) {
                toast.error('Failed to load doctor profile');
            } finally {
                setLoadingConfig(prev => ({ ...prev, fetch: false }));
            }
        };
        fetchDoc();
    }, [doctorId]);

    // Load Slots when date changes
    useEffect(() => {
        if (!date) return;

        const fetchSlots = async () => {
            setSelectedSlot(null);
            setLoadingConfig(prev => ({ ...prev, slots: true }));
            try {
                const res = await api.get(`/appointments/slots?doctorId=${doctorId}&date=${date}`);
                setSlots(res.data || []);
            } catch (e) {
                toast.error('Failed to load slots for this date');
            } finally {
                setLoadingConfig(prev => ({ ...prev, slots: false }));
            }
        };

        fetchSlots();
    }, [date, doctorId]);

    const handleBooking = async (e) => {
        e.preventDefault();
        if (!selectedSlot) {
            toast.error('Please select a time slot.');
            return;
        }

        setLoadingConfig(prev => ({ ...prev, book: true }));
        try {
            await api.post('/appointments/book', {
                doctorId: doctor.id,
                appointmentDate: date,
                startTime: selectedSlot.startTime,
                reasonForVisit: reason
            });

            toast.success('Appointment booked successfully!');
            navigate('/dashboard/appointments');
        } catch (error) {
            toast.error(error.response?.data?.detail || 'Failed to book appointment. Time slot may be taken.');
            // Refresh slots on collision
            setDate('');
        } finally {
            setLoadingConfig(prev => ({ ...prev, book: false }));
        }
    };

    if (loadingConfig.fetch) return <div>Loading doctor profile...</div>;
    if (!doctor) return <div>Doctor not found.</div>;

    // Calculate minimum selectable date (tomorrow)
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const minDate = tomorrow.toISOString().split('T')[0];

    return (
        <div>
            <button
                onClick={() => navigate(-1)}
                style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer', marginBottom: '1.5rem', fontWeight: 500 }}
            >
                <ArrowLeft size={20} /> Back to Search
            </button>

            <div style={{ display: 'grid', gridTemplateColumns: 'minmax(300px, 1fr) 2fr', gap: '2rem', alignItems: 'start' }}>
                {/* Doctor Profile Summary side panel */}
                <div className="glass-panel" style={{ padding: '2rem' }}>
                    <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
                        <div style={{ width: '80px', height: '80px', borderRadius: '50%', backgroundColor: 'var(--primary)', color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 1rem auto' }}>
                            <User size={40} />
                        </div>
                        <h2 style={{ color: 'var(--primary)', margin: '0 0 0.5rem 0' }}>Dr. {doctor.firstName} {doctor.lastName}</h2>
                        <p style={{ color: 'var(--text-muted)', margin: 0, fontWeight: 500 }}>{doctor.specialisation}</p>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', color: 'var(--text-main)', fontSize: '0.95rem' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border)', paddingBottom: '0.5rem' }}>
                            <span style={{ color: 'var(--text-muted)' }}>Experience</span>
                            <span style={{ fontWeight: 600 }}>{doctor.experienceYears} Years</span>
                        </div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border)', paddingBottom: '0.5rem' }}>
                            <span style={{ color: 'var(--text-muted)' }}>Consultation Fee</span>
                            <span style={{ fontWeight: 600 }}>${doctor.consultationFee}</span>
                        </div>
                    </div>

                    {doctor.bio && (
                        <div style={{ marginTop: '1.5rem' }}>
                            <h4 style={{ marginBottom: '0.5rem', color: 'var(--text-muted)' }}>About Doctor</h4>
                            <p style={{ fontSize: '0.9rem', lineHeight: 1.6 }}>{doctor.bio}</p>
                        </div>
                    )}
                </div>

                {/* Booking Form Interface */}
                <div className="glass-panel" style={{ padding: '2rem' }}>
                    <h2 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <Calendar size={24} color="var(--primary)" /> Schedule Appointment
                    </h2>

                    <form onSubmit={handleBooking}>
                        <div className="form-group" style={{ marginBottom: '2rem' }}>
                            <label>Select Date</label>
                            <input
                                type="date"
                                className="form-control"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                                min={minDate}
                                required
                            />
                        </div>

                        {/* Slots Grid */}
                        {date && (
                            <div style={{ marginBottom: '2rem' }}>
                                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', fontWeight: 500 }}>
                                    <Clock size={16} /> Available Times
                                </label>

                                {loadingConfig.slots ? (
                                    <p style={{ color: 'var(--text-muted)' }}>Loading slots...</p>
                                ) : slots.length === 0 ? (
                                    <div className="alert alert-error">Doctor doesn't have availability on this date. Please select another date.</div>
                                ) : (
                                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(100px, 1fr))', gap: '0.75rem' }}>
                                        {slots.map((slot, idx) => {
                                            const isSelected = selectedSlot === slot;
                                            return (
                                                <button
                                                    key={idx}
                                                    type="button"
                                                    disabled={!slot.available}
                                                    onClick={() => setSelectedSlot(slot)}
                                                    style={{
                                                        padding: '0.75rem 0.5rem', border: isSelected ? '2px solid var(--primary)' : '1px solid var(--border)',
                                                        borderRadius: 'var(--radius-sm)', background: isSelected ? '#e0f2fe' : slot.available ? 'var(--surface)' : '#f1f5f9',
                                                        color: slot.available ? (isSelected ? 'var(--primary)' : 'var(--text-main)') : '#94a3b8',
                                                        cursor: slot.available ? 'pointer' : 'not-allowed', fontWeight: 500, transition: 'all 0.2s',
                                                    }}
                                                >
                                                    {slot.startTime}
                                                </button>
                                            );
                                        })}
                                    </div>
                                )}
                            </div>
                        )}

                        <div className="form-group">
                            <label>Reason for Visit (Optional)</label>
                            <textarea
                                className="form-control"
                                rows="3"
                                placeholder="Briefly describe your symptoms or reason for visit..."
                                value={reason} onChange={(e) => setReason(e.target.value)}
                            ></textarea>
                        </div>

                        <button type="submit" className="btn btn-primary" disabled={!date || !selectedSlot || loadingConfig.book}>
                            {loadingConfig.book ? 'Confirming Booking...' : 'Confirm Appointment'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default BookAppointment;
