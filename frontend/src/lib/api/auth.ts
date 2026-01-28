/**
 * Authentication API Service
 * Handles all authentication-related API calls
 */

import { apiClient } from "./client";
import type { RegisterRequest, RegisterResponse, LoginRequest, LoginResponse } from "./types";

export interface IAuthService {
  register(data: RegisterRequest): Promise<RegisterResponse>;
  login(credentials: LoginRequest): Promise<LoginResponse>;
  logout(): void;
}

/**
 * Register new user
 * POST /auth/register
 */
export async function register(data: RegisterRequest): Promise<RegisterResponse> {
  try {
    const response = await apiClient.post<string>("/auth/register", data);

    // Backend returns UUID string directly
    return {
      userId: response.data,
    };
  } catch (error: any) {
    // Re-throw with more context
    if (error.response) {
      throw new Error(error.response.data?.message || "Registration failed. Please try again.");
    } else if (error.request) {
      throw new Error("Unable to connect to server. Please check your connection.");
    } else {
      throw new Error("Registration failed. Please try again later.");
    }
  }
}

/**
 * Authenticate user and receive JWT token
 * POST /auth/login
 */
export async function login(credentials: LoginRequest): Promise<LoginResponse> {
  try {
    const response = await apiClient.post<LoginResponse>("/auth/login", credentials);
    return response.data;
  } catch (error: any) {
    // Re-throw with more context
    if (error.response) {
      const status = error.response.status;

      if (status === 401) {
        throw new Error("Invalid username or password. Please try again.");
      } else if (status === 400) {
        throw new Error("Invalid login data. Please check your inputs.");
      } else {
        throw new Error(error.response.data?.message || "Login failed. Please try again.");
      }
    } else if (error.request) {
      throw new Error("Unable to connect to server. Please check your connection.");
    } else {
      throw new Error("Login failed. Please try again later.");
    }
  }
}

/**
 * Logout (client-side only)
 * Clears token from storage
 */
export function logout(): void {
  // Token clearing is handled by tokenService and AuthContext
  // This function exists for API consistency
}

const authService = {
  register,
  login,
  logout,
};

export default authService;
