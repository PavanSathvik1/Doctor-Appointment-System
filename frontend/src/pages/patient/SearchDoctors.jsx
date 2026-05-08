import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { Search, MapPin, Star, CalendarPlus } from 'lucide-react';

const SearchDoctors = () => {
    const [query, setQuery] = useState('');
    const [doctors, setDoctors] = useState([]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    // Initial load without query
    useEffect(() => {
        handleSearch('');
    }, []);

    const handleSearch = async (searchQuery) => {
        setLoading(true);
        try {
            const res = await api.get(`/doctors/search?query=${encodeURIComponent(searchQuery)}`);
            setDoctors(res.data || []);
        } catch (e) {
            toast.error('Failed to search doctors.');
        } finally {
            setLoading(false);
        }
    };

    const onSubmit = (e) => {
        e.preventDefault();
        handleSearch(query);
    };

    return (
        <div>
            <h1 style={{ marginBottom: '1.5rem' }}>Find a Doctor</h1>

            {/* Search Bar */}
            <form onSubmit={onSubmit} style={{ display: 'flex', gap: '1rem', marginBottom: '2rem' }}>
                <div style={{ position: 'relative', flex: 1 }}>
                    <Search size={20} color="var(--text-muted)" style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)' }} />
                    <input
                        type="text"
                        className="form-control"
                        placeholder="Search by name or specialisation... (e.g. Cardiologist)"
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        style={{ paddingLeft: '3rem', fontSize: '1.1rem', padding: '1rem 1rem 1rem 3rem', borderRadius: 'var(--radius-lg)', boxShadow: 'var(--shadow-sm)' }}
                    />
                </div>
                <button type="submit" className="btn btn-primary" style={{ width: 'auto', borderRadius: 'var(--radius-lg)' }} disabled={loading}>
                    {loading ? 'Searching...' : 'Search'}
                </button>
            </form>

            {/* Results Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '1.5rem' }}>
                {doctors.length === 0 && !loading ? (
                    <p style={{ color: 'var(--text-muted)' }}>No doctors found matching your search.</p>
                ) : (
                    doctors.map(doc => (
                        <div key={doc.id} className="glass-panel" style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                                <div>
                                    <h3 style={{ margin: '0 0 0.25rem 0', color: 'var(--primary)' }}>Dr. {doc.firstName} {doc.lastName}</h3>
                                    <p style={{ margin: 0, fontWeight: 500, color: 'var(--text-muted)' }}>{doc.specialisation}</p>
                                </div>
                                <div style={{ background: '#fef3c7', color: '#b45309', padding: '0.25rem 0.5rem', borderRadius: 'var(--radius-sm)', display: 'flex', alignItems: 'center', gap: '0.25rem', fontSize: '0.85rem', fontWeight: 600 }}>
                                    <Star size={14} fill="currentColor" /> {doc.experienceYears}y Exp
                                </div>
                            </div>

                            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1.5rem' }}>
                                <MapPin size={16} /> Consultation Fee: ${doc.consultationFee}
                            </div>

                            <button
                                className="btn btn-primary"
                                style={{ marginTop: 'auto', display: 'flex', gap: '0.5rem', justifyContent: 'center' }}
                                onClick={() => navigate(`/dashboard/book/${doc.id}`)}
                            >
                                <CalendarPlus size={18} /> Book Appointment
                            </button>
                        </div>
                    ))
                )}
            </div>
        </div>
    );
};

export default SearchDoctors;
