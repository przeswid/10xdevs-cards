/**
 * Server-side Authentication Utilities
 * For Astro page guards and server-side rendering
 */

import type { AstroCookies } from 'astro';
import { jwtDecode } from 'jwt-decode';
import type { AuthCheckResult, User } from '@/lib/api/types';

const AUTH_COOKIE_NAME = 'auth_token';

interface JWTPayload {
  sub: string; // User ID
  username: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  exp: number; // Expiration timestamp (seconds)
}

export interface IServerAuth {
  checkAuth(cookies: AstroCookies, request: Request): AuthCheckResult;
  setAuthCookie(cookies: AstroCookies, token: string, expiresIn: number): void;
  clearAuthCookie(cookies: AstroCookies): void;
}

/**
 * Check if user is authenticated based on cookie token
 * @param cookies - Astro cookies object
 * @param request - Request object (unused but kept for interface compatibility)
 * @returns Authentication check result
 */
export function checkAuth(cookies: AstroCookies, request: Request): AuthCheckResult {
  try {
    const token = cookies.get(AUTH_COOKIE_NAME)?.value;

    if (!token) {
      return {
        isAuthenticated: false,
        error: 'No authentication token found',
      };
    }

    // Decode and validate token
    const payload = jwtDecode<JWTPayload>(token);
    const currentTime = Date.now() / 1000;

    if (payload.exp < currentTime) {
      return {
        isAuthenticated: false,
        error: 'Token expired',
      };
    }

    // Extract user from token
    const user: User = {
      id: payload.sub,
      username: payload.username,
      email: payload.email || '',
      firstName: payload.firstName || '',
      lastName: payload.lastName || '',
    };

    return {
      isAuthenticated: true,
      user,
    };
  } catch (error) {
    console.error('Auth check failed:', error);
    return {
      isAuthenticated: false,
      error: 'Invalid token',
    };
  }
}

/**
 * Set authentication cookie after successful login
 * @param cookies - Astro cookies object
 * @param token - JWT token string
 * @param expiresIn - Seconds until expiration
 */
export function setAuthCookie(cookies: AstroCookies, token: string, expiresIn: number): void {
  const maxAge = expiresIn; // in seconds

  cookies.set(AUTH_COOKIE_NAME, token, {
    path: '/',
    httpOnly: false, // Client needs to read it for React components
    secure: import.meta.env.PROD, // HTTPS only in production
    sameSite: 'strict',
    maxAge,
  });
}

/**
 * Clear authentication cookie on logout
 * @param cookies - Astro cookies object
 */
export function clearAuthCookie(cookies: AstroCookies): void {
  cookies.delete(AUTH_COOKIE_NAME, {
    path: '/',
  });
}

const serverAuth = {
  checkAuth,
  setAuthCookie,
  clearAuthCookie,
};

export default serverAuth;
