import axios, { type AxiosInstance } from "axios";
import { getToken, clearToken } from "@/lib/services/tokenService";

/**
 * Axios instance dla komunikacji z backend API
 */
export const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.PUBLIC_API_URL || "http://localhost:8080",
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 30000, // 30 seconds
});

/**
 * Request interceptor - Add authentication token to requests
 */
apiClient.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response interceptor - Handle errors including authentication failures
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Backend zwrócił error response
      console.error("API Error:", error.response.data);

      // Handle 401 Unauthorized - token expired or invalid
      if (error.response.status === 401) {
        clearToken();
        // Redirect to login if not already on auth pages
        if (
          typeof window !== "undefined" &&
          !window.location.pathname.startsWith("/login") &&
          !window.location.pathname.startsWith("/register")
        ) {
          const currentPath = window.location.pathname;
          window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}&session=expired`;
        }
      }
    } else if (error.request) {
      // Request został wysłany ale brak odpowiedzi
      console.error("Network Error:", error.message);
    } else {
      // Inny błąd
      console.error("Error:", error.message);
    }
    return Promise.reject(error);
  }
);
