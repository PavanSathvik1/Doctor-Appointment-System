import React, { createContext, useState, useEffect } from 'react';
import api from '../api/axiosConfig';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    const initAuth = async () => {
        try {
            // Upon app load, attempt to silently refresh the token using the httpOnly cookie
            const res = await api.post('/auth/refresh');
            window.MEMORY_ACCESS_TOKEN = res.data.accessToken;

            // If we don't return user details in the refresh response, 
            // we would decode the JWT here or fetch /users/me. 
            // Assuming a simple role decode for Phase 1:
            const payload = JSON.parse(atob(res.data.accessToken.split('.')[1]));
            setUser({
                id: payload.userId,
                email: payload.sub,
                role: payload.role
            });
            setIsAuthenticated(true);
        } catch (err) {
            // Refresh failed (no valid cookie), not authenticated
            window.MEMORY_ACCESS_TOKEN = null;
            setUser(null);
            setIsAuthenticated(false);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        initAuth();

        // Register the logout callback for axios interceptor
        window.AUTH_LOGOUT_CALLBACK = () => {
            setUser(null);
            setIsAuthenticated(false);
        };
    }, []);

    const loginSuccess = (data) => {
        window.MEMORY_ACCESS_TOKEN = data.accessToken;
        setUser({
            id: data.userId,
            email: data.email,
            role: data.role
        });
        setIsAuthenticated(true);
    };

    const logout = async () => {
        try {
            await api.post('/auth/logout');
        } catch (e) {
            console.error('Logout API failed', e);
        } finally {
            window.MEMORY_ACCESS_TOKEN = null;
            setUser(null);
            setIsAuthenticated(false);
        }
    };

    return (
        <AuthContext.Provider value={{ user, isAuthenticated, isLoading, loginSuccess, logout }}>
            {children}
        </AuthContext.Provider>
    );
};
