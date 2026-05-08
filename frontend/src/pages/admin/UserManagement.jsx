import React, { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import { UserCheck, UserMinus, Shield, Search } from 'lucide-react';
import { toast } from 'react-toastify';

const UserManagement = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const fetchUsers = async () => {
        try {
            const response = await api.get(`/admin/users?page=${page}&size=10`);
            setUsers(response.data.content);
            setTotalPages(response.data.totalPages);
        } catch (error) {
            toast.error('Failed to fetch user list');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, [page]);

    const toggleStatus = async (userId) => {
        try {
            await api.patch(`/admin/users/${userId}/status`);
            toast.success('User status updated');
            fetchUsers();
        } catch (error) {
            toast.error('Failed to toggle status');
        }
    };

    if (loading) return <div className="loading-spinner">Loading users...</div>;

    return (
        <div style={{ padding: '1rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <h2 style={{ margin: 0, color: 'var(--text-main)' }}>Manage System Users</h2>
            </div>

            <div className="glass-panel" style={{ overflow: 'hidden' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead style={{ backgroundColor: 'rgba(255,255,255,0.05)' }}>
                        <tr>
                            <th style={{ padding: '1rem 1.5rem', fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)' }}>Name</th>
                            <th style={{ padding: '1rem 1.5rem', fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)' }}>Email</th>
                            <th style={{ padding: '1rem 1.5rem', fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)' }}>Role</th>
                            <th style={{ padding: '1rem 1.5rem', fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)' }}>Status</th>
                            <th style={{ padding: '1rem 1.5rem', fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-muted)', textAlign: 'right' }}>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        {users.map((user) => (
                            <tr key={user.id} style={{ borderBottom: '1px solid var(--border)' }}>
                                <td style={{ padding: '1rem 1.5rem' }}>{user.firstName} {user.lastName}</td>
                                <td style={{ padding: '1rem 1.5rem' }}>{user.email}</td>
                                <td style={{ padding: '1rem 1.5rem' }}>
                                    <span style={{
                                        padding: '0.25rem 0.75rem', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 600,
                                        backgroundColor: user.role === 'ADMIN' ? 'rgba(255, 75, 75, 0.1)' : 'rgba(75, 255, 150, 0.1)',
                                        color: user.role === 'ADMIN' ? '#ff4b4b' : '#4bff96'
                                    }}>
                                        {user.role}
                                    </span>
                                </td>
                                <td style={{ padding: '1rem 1.5rem' }}>
                                    <span style={{
                                        padding: '0.25rem 0.75rem', borderRadius: '12px', fontSize: '0.75rem', fontWeight: 600,
                                        backgroundColor: user.status === 'ACTIVE' ? 'rgba(75, 255, 150, 0.1)' : 'rgba(255, 150, 75, 0.1)',
                                        color: user.status === 'ACTIVE' ? '#4bff96' : '#ff964b'
                                    }}>
                                        {user.status}
                                    </span>
                                </td>
                                <td style={{ padding: '1rem 1.5rem', textAlign: 'right' }}>
                                    {user.role !== 'ADMIN' && (
                                        <button
                                            onClick={() => toggleStatus(user.id)}
                                            style={{
                                                background: 'none', border: 'none', cursor: 'pointer',
                                                color: user.status === 'ACTIVE' ? 'var(--error)' : 'var(--success)',
                                                padding: '0.5rem', borderRadius: '8px', transition: 'background 0.2s'
                                            }}
                                            title={user.status === 'ACTIVE' ? 'Suspend User' : 'Activate User'}
                                        >
                                            {user.status === 'ACTIVE' ? <UserMinus size={20} /> : <UserCheck size={20} />}
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>

                {/* Pagination */}
                <div style={{ padding: '1.5rem', display: 'flex', justifyContent: 'center', gap: '1rem' }}>
                    <button
                        disabled={page === 0}
                        onClick={() => setPage(page - 1)}
                        className="btn-secondary" style={{ padding: '0.5rem 1rem' }}
                    >
                        Prev
                    </button>
                    <span style={{ alignSelf: 'center', color: 'var(--text-muted)' }}>Page {page + 1} of {totalPages}</span>
                    <button
                        disabled={page >= totalPages - 1}
                        onClick={() => setPage(page + 1)}
                        className="btn-secondary" style={{ padding: '0.5rem 1rem' }}
                    >
                        Next
                    </button>
                </div>
            </div>
        </div>
    );
};

export default UserManagement;
