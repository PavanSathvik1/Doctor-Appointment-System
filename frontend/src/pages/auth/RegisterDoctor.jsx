import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../../api/axiosConfig';
import { Stethoscope } from 'lucide-react';

const RegisterDoctor = () => {
    const [formData, setFormData] = useState({
        firstName: '', lastName: '', email: '', password: '', phone: '',
        specialisation: '', licenceNumber: '', experienceYears: '', consultationFee: '', bio: ''
    });
    const [document, setDocument] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleFileChange = (e) => {
        setDocument(e.target.files[0]);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!document) {
            toast.error('Please upload your medical licence document.');
            return;
        }

        setLoading(true);

        // Construct FormData for multipart/form-data
        const data = new FormData();
        Object.keys(formData).forEach(key => {
            data.append(key, formData[key]);
        });
        data.append('document', document);

        try {
            const response = await api.post('/doctors/register', data, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            });
            toast.success(response.data.message || 'Application submitted successfully!');
            navigate('/login');
        } catch (error) {
            if (error.response?.data?.errors) {
                toast.error(`${error.response.data.errors[0].field}: ${error.response.data.errors[0].message}`);
            } else {
                toast.error(error.response?.data?.detail || 'Registration failed');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-layout animate-entrance" style={{ padding: '2rem 1rem' }}>
            <div className="glass-panel auth-card" style={{ maxWidth: '600px' }}>
                <div className="auth-header">
                    <Stethoscope size={48} color="var(--primary)" style={{ marginBottom: '1rem' }} />
                    <h1>Doctor Application</h1>
                    <p>Apply to join our medical team. Approvals take up to 48 hours.</p>
                </div>

                <form onSubmit={handleSubmit}>
                    {/* Personal Info */}
                    <h3 style={{ marginBottom: '1rem', color: 'var(--text-main)' }}>Personal Information</h3>
                    <div style={{ display: 'flex', gap: '1rem' }}>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>First Name</label>
                            <input type="text" name="firstName" className="form-control" value={formData.firstName} onChange={handleChange} required />
                        </div>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>Last Name</label>
                            <input type="text" name="lastName" className="form-control" value={formData.lastName} onChange={handleChange} required />
                        </div>
                    </div>

                    <div style={{ display: 'flex', gap: '1rem' }}>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>Email Address</label>
                            <input type="email" name="email" className="form-control" value={formData.email} onChange={handleChange} required />
                        </div>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>Phone Number</label>
                            <input type="tel" name="phone" className="form-control" value={formData.phone} onChange={handleChange} required />
                        </div>
                    </div>

                    <div className="form-group">
                        <label>Password</label>
                        <input type="password" name="password" className="form-control" value={formData.password} onChange={handleChange} required minLength="8" />
                    </div>

                    {/* Professional Info */}
                    <h3 style={{ margin: '1.5rem 0 1rem 0', color: 'var(--text-main)' }}>Professional Details</h3>

                    <div style={{ display: 'flex', gap: '1rem' }}>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>Specialisation</label>
                            <input type="text" name="specialisation" className="form-control" placeholder="e.g. Cardiologist" value={formData.specialisation} onChange={handleChange} required />
                        </div>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>Licence Number</label>
                            <input type="text" name="licenceNumber" className="form-control" value={formData.licenceNumber} onChange={handleChange} required />
                        </div>
                    </div>

                    <div style={{ display: 'flex', gap: '1rem' }}>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>Experience (Years)</label>
                            <input type="number" name="experienceYears" className="form-control" value={formData.experienceYears} onChange={handleChange} required min="1" />
                        </div>
                        <div className="form-group" style={{ flex: 1 }}>
                            <label>Consultation Fee ($)</label>
                            <input type="number" step="0.01" name="consultationFee" className="form-control" value={formData.consultationFee} onChange={handleChange} required min="0" />
                        </div>
                    </div>

                    <div className="form-group">
                        <label>Short Bio</label>
                        <textarea name="bio" className="form-control" rows="3" value={formData.bio} onChange={handleChange}></textarea>
                    </div>

                    <div className="form-group">
                        <label>Medical Licence Document (PDF/Image)</label>
                        <input type="file" accept=".pdf,image/*" className="form-control" onChange={handleFileChange} required style={{ padding: '0.6rem' }} />
                        <small style={{ color: 'var(--text-muted)' }}>Upload a clear scan of your medical board licence.</small>
                    </div>

                    <button type="submit" className="btn btn-primary" disabled={loading} style={{ marginTop: '1rem' }}>
                        {loading ? 'Submitting Application...' : 'Submit Application'}
                    </button>
                </form>

                <div className="auth-footer">
                    <p>Are you a patient? <Link to="/register" className="text-link">Register as patient</Link></p>
                </div>
            </div>
        </div>
    );
};

export default RegisterDoctor;
