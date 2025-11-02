import axios, { type AxiosInstance } from 'axios';

/**
 * Axios instance dla komunikacji z backend API
 * TODO: Dodać API URL z environment variables
 */
export const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.PUBLIC_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30 seconds
});

/**
 * Request interceptor - dodawanie auth token w przyszłości
 */
apiClient.interceptors.request.use(
  (config) => {
    // TODO: Dodać auth token gdy authentication będzie zaimplementowane
    // const token = getAuthToken();
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response interceptor - obsługa błędów
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Backend zwrócił error response
      console.error('API Error:', error.response.data);
    } else if (error.request) {
      // Request został wysłany ale brak odpowiedzi
      console.error('Network Error:', error.message);
    } else {
      // Inny błąd
      console.error('Error:', error.message);
    }
    return Promise.reject(error);
  }
);
