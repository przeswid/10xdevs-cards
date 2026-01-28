/**
 * Authentication Context
 * Global authentication state management for React components
 */

import React, { createContext, useContext, useState, useEffect, useCallback } from "react";
import type { User, RegisterRequest, LoginRequest } from "@/lib/api/types";
import * as authService from "@/lib/api/auth";
import * as tokenService from "@/lib/services/tokenService";
import { decodeToken } from "@/lib/utils/jwt";

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (username: string, password: string) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => void;

  // Token helpers
  getAuthToken: () => string | null;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

interface AuthProviderProps {
  children: React.ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Initialize auth state from stored token
  useEffect(() => {
    const initAuth = () => {
      const token = tokenService.getToken();

      if (token) {
        const decodedUser = decodeToken(token);
        if (decodedUser) {
          setUser(decodedUser);
        } else {
          // Invalid token, clear it
          tokenService.clearToken();
        }
      }

      setIsLoading(false);
    };

    initAuth();
  }, []);

  /**
   * Login user with username and password
   */
  const login = useCallback(async (username: string, password: string) => {
    try {
      setError(null);
      setIsLoading(true);

      const response = await authService.login({ username, password });

      // Store token in localStorage
      tokenService.setToken(response.accessToken, response.expiresIn);

      // Set server-side cookie
      await fetch("/api/auth/set-cookie", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          token: response.accessToken,
          expiresIn: response.expiresIn,
        }),
      });

      // Decode token to get user data
      const decodedUser = decodeToken(response.accessToken);
      if (decodedUser) {
        setUser(decodedUser);
      }

      setIsLoading(false);
    } catch (err: any) {
      setError(err.message || "Login failed");
      setIsLoading(false);
      throw err;
    }
  }, []);

  /**
   * Register new user and auto-login
   */
  const register = useCallback(
    async (userData: RegisterRequest) => {
      try {
        setError(null);
        setIsLoading(true);

        // Register user
        await authService.register(userData);

        // Auto-login after successful registration
        await login(userData.username, userData.password);

        setIsLoading(false);
      } catch (err: any) {
        setError(err.message || "Registration failed");
        setIsLoading(false);
        throw err;
      }
    },
    [login]
  );

  /**
   * Logout user
   */
  const logout = useCallback(async () => {
    // Clear localStorage token
    tokenService.clearToken();

    // Clear server-side cookie
    await fetch("/api/auth/logout", {
      method: "POST",
    });

    setUser(null);
    setError(null);
  }, []);

  /**
   * Get current auth token
   */
  const getAuthToken = useCallback(() => {
    return tokenService.getToken();
  }, []);

  const value: AuthContextValue = {
    user,
    isAuthenticated: user !== null,
    isLoading,
    error,
    login,
    register,
    logout,
    getAuthToken,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

/**
 * Hook to use auth context
 */
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
}
