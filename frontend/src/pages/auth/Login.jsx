import React, { useState, useContext } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { AuthContext } from '../../context/AuthContext';
import { HeartPulse } from 'lucide-react';

const Login = () => {
    const [formData, setFormData] = useState({ email: '', password: '' });
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();
    const { loginSuccess } = useContext(AuthContext);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const response = await api.post('/auth/login', formData);
            loginSuccess(response.data);
            toast.success('Successfully logged in!');

            // Redirect based on role
            const role = response.data.role;
            if (role === 'ADMIN') navigate('/admin');
            else if (role === 'DOCTOR') navigate('/doctor/dashboard');
            else navigate('/dashboard');

        } catch (error) {
            if (error.response && error.response.data) {
                toast.error(error.response.data.detail || 'Login failed');
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
                    <HeartPulse size={48} color="var(--primary)" style={{ marginBottom: '1rem' }} />
                    <h1>Welcome Back</h1>
                    <p>Login to manage your health schedule safely.</p>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="email">Email Address</label>
                        <input
                            type="email" id="email" name="email"
                            className="form-control" placeholder="you@example.com"
                            value={formData.email} onChange={handleChange} required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <input
                            type="password" id="password" name="password"
                            className="form-control" placeholder="Enter your password"
                            value={formData.password} onChange={handleChange} required
                        />
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={loading}>
                        {loading ? 'Logging in...' : 'Log In'}
                    </button>
                </form>

                <div className="auth-footer">
                    <p style={{ marginBottom: '0.5rem' }}>
                        <Link to="/forgot-password" className="text-link">Forgot password?</Link>
                    </p>
                    <p>Don't have an account? <Link to="/register" className="text-link">Register here</Link></p>
                </div>
            </div>
        </div>
    );
};

export default Login;
