import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { LockKeyhole } from 'lucide-react';

const ResetPassword = () => {
    const [formData, setFormData] = useState({ token: '', newPassword: '' });
    const [loading, setLoading] = useState(false);
    const location = useLocation();
    const navigate = useNavigate();

    useEffect(() => {
        // Extract token from query params
        const queryParams = new URLSearchParams(location.search);
        const tokenFromUrl = queryParams.get('token');
        if (tokenFromUrl) {
            setFormData(prev => ({ ...prev, token: tokenFromUrl }));
        }
    }, [location]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!formData.token) {
            toast.error('Reset token is missing. Please use the link from your email.');
            return;
        }

        if (formData.newPassword.length < 8) {
            toast.error('Password must be at least 8 characters long.');
            return;
        }

        setLoading(true);

        try {
            const response = await api.post('/auth/reset-password', formData);
            toast.success(response.data.message || 'Password reset successful!');
            navigate('/login');
        } catch (error) {
            toast.error(error.response?.data?.detail || 'Failed to reset password. The link may have expired.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-layout animate-entrance">
            <div className="glass-panel auth-card">
                <div className="auth-header">
                    <LockKeyhole size={48} color="var(--primary)" style={{ marginBottom: '1rem' }} />
                    <h1>Set New Password</h1>
                    <p>Please enter your new password below.</p>
                </div>

                <form onSubmit={handleSubmit}>
                    {!formData.token && (
                        <div className="alert alert-error">
                            No reset token found in the URL. Please click the exact link from your email.
                        </div>
                    )}

                    <div className="form-group">
                        <label htmlFor="newPassword">New Password</label>
                        <input
                            type="password" id="newPassword" name="newPassword" className="form-control"
                            value={formData.newPassword} onChange={handleChange} required
                            placeholder="At least 8 characters" minLength="8"
                            disabled={!formData.token}
                        />
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={loading || !formData.token}>
                        {loading ? 'Resetting...' : 'Reset Password'}
                    </button>
                </form>

                <div className="auth-footer" style={{ marginTop: '1.5rem' }}>
                    <Link to="/login" className="text-link">← Back to Log In</Link>
                </div>
            </div>
        </div>
    );
};

export default ResetPassword;
