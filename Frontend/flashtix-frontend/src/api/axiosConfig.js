import axios from "axios";

// Point this to your API Gateway!
const BASE_URL = "http://localhost:8081";

const api = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
});

// 1. Request Interceptor: Attach the Access Token before sending
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// 2. Response Interceptor: Handle Expired Tokens & Auto-Refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // If error is 401 (Unauthorized) and we haven't already tried to refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      const refreshToken = localStorage.getItem("refreshToken");

      if (refreshToken) {
        try {
          // Call your Spring Boot refresh endpoint
          const res = await axios.post(`${BASE_URL}/api/auth/refresh`, {
            refreshToken,
          });

          // Save the new tokens
          localStorage.setItem("accessToken", res.data.accessToken);
          localStorage.setItem("refreshToken", res.data.refreshToken);

          // Update the header and retry the original failed request
          originalRequest.headers["Authorization"] =
            `Bearer ${res.data.accessToken}`;
          return api(originalRequest);
        } catch (refreshError) {
          // If the refresh token is also dead, log them out completely
          console.error("Session expired. Please log in again.");
          localStorage.removeItem("accessToken");
          localStorage.removeItem("refreshToken");
          window.location.href = "/login";
        }
      }
    }
    return Promise.reject(error);
  },
);

export default api;
