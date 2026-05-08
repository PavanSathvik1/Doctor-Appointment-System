import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { KeyRound } from 'lucide-react';

const ForgotPassword = () => {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [isSent, setIsSent] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const response = await api.post('/auth/forgot-password', { email });
            toast.success(response.data.message || 'Reset link sent to your email.');
            setIsSent(true);
        } catch (error) {
            toast.error(error.response?.data?.detail || 'Failed to send reset link. Try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-layout animate-entrance">
            <div className="glass-panel auth-card">
                <div className="auth-header">
                    <KeyRound size={48} color="var(--primary)" style={{ marginBottom: '1rem' }} />
                    <h1>Forgot Password</h1>
                    <p>Enter your email and we'll send you a link to reset your password.</p>
                </div>

                {!isSent ? (
                    <form onSubmit={handleSubmit}>
                        <div className="form-group">
                            <label htmlFor="email">Email Address</label>
                            <input
                                type="email" id="email" className="form-control"
                                value={email} onChange={(e) => setEmail(e.target.value)} required
                            />
                        </div>

                        <button type="submit" className="btn btn-primary" disabled={loading}>
                            {loading ? 'Sending...' : 'Send Reset Link'}
                        </button>
                    </form>
                ) : (
                    <div className="alert alert-success">
                        Check your email! A password reset link has been sent to <strong>{email}</strong>.
                    </div>
                )}

                <div className="auth-footer" style={{ marginTop: '1.5rem' }}>
                    <Link to="/login" className="text-link">← Back to Log In</Link>
                </div>
            </div>
        </div>
    );
};

export default ForgotPassword;
