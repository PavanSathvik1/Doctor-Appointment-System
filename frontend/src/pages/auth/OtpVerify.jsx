import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { ShieldCheck } from 'lucide-react';

const OtpVerify = () => {
    const [formData, setFormData] = useState({ email: '', otp: '' });
    const [loading, setLoading] = useState(false);
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        // Populate email from navigation state if available
        if (location.state?.email) {
            setFormData(prev => ({ ...prev, email: location.state.email }));
        }
    }, [location]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value.replace(/\D/g, '').slice(0, 6) });
    };

    const handleEmailChange = (e) => {
        setFormData({ ...formData, email: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (formData.otp.length !== 6) {
            toast.error('OTP must be exactly 6 digits');
            return;
        }

        setLoading(true);
        try {
            const response = await api.post('/auth/verify-otp', formData);
            toast.success(response.data.message || 'Account verified successfully!');
            navigate('/login');
        } catch (error) {
            if (error.response && error.response.data) {
                toast.error(error.response.data.detail || 'Verification failed');
            } else {
                toast.error('Network error. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-layout animate-entrance">
            <div className="glass-panel auth-card">
                <div className="auth-header">
                    <ShieldCheck size={48} color="var(--success)" style={{ marginBottom: '1rem' }} />
                    <h1>Verify Your Email</h1>
                    <p>We've sent a 6-digit code to your email address.</p>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="email">Email Address</label>
                        <input
                            type="email" id="email" name="email" className="form-control"
                            value={formData.email} onChange={handleEmailChange} required
                            readOnly={!!location.state?.email}
                            style={{ backgroundColor: location.state?.email ? '#f1f5f9' : 'var(--surface)' }}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="otp">6-Digit OTP</label>
                        <input
                            type="text" id="otp" name="otp" className="form-control"
                            value={formData.otp} onChange={handleChange} required
                            placeholder="000000"
                            style={{ fontSize: '1.5rem', letterSpacing: '0.25em', textAlign: 'center' }}
                        />
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={loading} style={{ backgroundColor: 'var(--success)' }}>
                        {loading ? 'Verifying...' : 'Verify & Activate'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default OtpVerify;
