import axios from 'axios';

// Create an Axios instance
const api = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true // Important for sending/receiving the HTTPOnly refresh token cookie
});

// Request interceptor: attach the access token to every request if available
api.interceptors.request.use(
    (config) => {
        // In a real app, you might want to use a state manager instead of storing the token here,
        // but a module-level variable managed by AuthContext works well for SPA.
        const token = window.MEMORY_ACCESS_TOKEN;
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor: handle 401s and attempt to refresh the token
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        // If the error is 401 and we haven't already retried this request
        if (error.response?.status === 401 && !originalRequest._retry) {
            // Avoid infinite loops if the refresh itself returns 401
            if (originalRequest.url.includes('/auth/refresh') || originalRequest.url.includes('/auth/login')) {
                return Promise.reject(error);
            }

            originalRequest._retry = true;
            try {
                // Call the refresh endpoint (it automatically uses the httpOnly cookie)
                const response = await axios.post('/api/auth/refresh', {}, {
                    withCredentials: true
                });

                const { accessToken } = response.data;

                // Update the module-level token
                window.MEMORY_ACCESS_TOKEN = accessToken;

                // Update the failed request's header and retry
                originalRequest.headers['Authorization'] = `Bearer ${accessToken}`;
                return api(originalRequest);

            } catch (refreshError) {
                // If refresh fails, clear token and state, then redirect to login
                window.MEMORY_ACCESS_TOKEN = null;
                if (window.AUTH_LOGOUT_CALLBACK) {
                    window.AUTH_LOGOUT_CALLBACK();
                }
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }
);

export default api;
