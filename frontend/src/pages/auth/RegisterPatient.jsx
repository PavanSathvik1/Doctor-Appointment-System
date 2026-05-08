import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { UserPlus } from 'lucide-react';

const RegisterPatient = () => {
    const [formData, setFormData] = useState({
        firstName: '', lastName: '', email: '', password: '', phone: ''
    });
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const response = await api.post('/auth/register', formData);
            toast.success(response.data.message || 'Registration successful. Check your email for the OTP.');

            // Navigate to OTP verification page and pass the email in state
            navigate('/verify-otp', { state: { email: formData.email } });

        } catch (error) {
            if (error.response && error.response.data) {
                if (error.response.data.errors) {
                    // Handle validation errors specifically
                    const firstError = error.response.data.errors[0];
                    toast.error(`${firstError.field}: ${firstError.message}`);
                } else {
                    toast.error(error.response.data.detail || 'Registration failed');
                }
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
                    <UserPlus size={48} color="var(--primary)" style={{ marginBottom: '1rem' }} />
                    <h1>Create an Account</h1>
                    <p>Join HMS to book and manage your medical appointments.</p>
                </div>

                <form onSubmit={handleSubmit}>
                    <div style={{ display: 'flex', gap: '1rem' }}>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label htmlFor="firstName">First Name</label>
                            <input
                                type="text" id="firstName" name="firstName" className="form-control"
                                value={formData.firstName} onChange={handleChange} required
                            />
                        </div>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label htmlFor="lastName">Last Name</label>
                            <input
                                type="text" id="lastName" name="lastName" className="form-control"
                                value={formData.lastName} onChange={handleChange} required
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">Email Address</label>
                        <input
                            type="email" id="email" name="email" className="form-control"
                            value={formData.email} onChange={handleChange} required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="phone">Phone Number (Optional)</label>
                        <input
                            type="tel" id="phone" name="phone" className="form-control"
                            value={formData.phone} onChange={handleChange}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <input
                            type="password" id="password" name="password" className="form-control"
                            value={formData.password} onChange={handleChange} required minLength="8"
                            placeholder="At least 8 characters"
                        />
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={loading}>
                        {loading ? 'Registering...' : 'Register'}
                    </button>
                </form>

                <div className="auth-footer">
                    <p>Already have an account? <Link to="/login" className="text-link">Log In here</Link></p>
                    <hr style={{ margin: '1rem 0', borderColor: 'rgba(255,255,255,0.1)' }} />
                    <p>Are you a healthcare professional? <Link to="/register-doctor" className="text-link">Apply here</Link></p>
                </div>
            </div>
        </div>
    );
};

export default RegisterPatient;
