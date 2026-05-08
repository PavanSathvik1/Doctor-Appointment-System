import React, { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const ProtectedRoute = ({ children, allowedRoles }) => {
    const { isAuthenticated, isLoading, user } = useContext(AuthContext);

    if (isLoading) {
        return <div className="auth-layout">Loading...</div>; // Could be a nicer spinner
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(user?.role)) {
        // If authenticated but unauthorized for this route, go to a generic safe page
        return <Navigate to="/" replace />;
    }

    return children;
};

export default ProtectedRoute;
