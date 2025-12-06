/**
 * JWT Utilities
 * Client-side JWT decoding and validation
 */

import { jwtDecode } from "jwt-decode";
import type { User } from "@/lib/api/types";

/**
 * JWT payload interface
 */
interface JWTPayload {
  sub: string; // User ID
  username: string;
  email?: string;
  firstName?: string;
  lastName?: string;
  exp: number; // Expiration timestamp (seconds)
  iat: number; // Issued at timestamp (seconds)
}

/**
 * Decode JWT token and extract user information
 * @param token - JWT token string
 * @returns User object or null if invalid
 */
export function decodeToken(token: string): User | null {
  try {
    const payload = jwtDecode<JWTPayload>(token);

    return {
      id: payload.sub,
      username: payload.username,
      email: payload.email || "",
      firstName: payload.firstName || "",
      lastName: payload.lastName || "",
    };
  } catch (error) {
    console.error("Failed to decode JWT:", error);
    return null;
  }
}

/**
 * Check if JWT token is expired
 * @param token - JWT token string
 * @returns true if expired, false otherwise
 */
export function isTokenExpired(token: string): boolean {
  try {
    const payload = jwtDecode<JWTPayload>(token);
    const currentTime = Date.now() / 1000; // Convert to seconds

    return payload.exp < currentTime;
  } catch (error) {
    console.error("Failed to check token expiration:", error);
    return true; // Assume expired if can't decode
  }
}

/**
 * Get expiration time from JWT token
 * @param token - JWT token string
 * @returns Expiration timestamp in milliseconds, or null if invalid
 */
export function getTokenExpiration(token: string): number | null {
  try {
    const payload = jwtDecode<JWTPayload>(token);
    return payload.exp * 1000; // Convert to milliseconds
  } catch (error) {
    console.error("Failed to get token expiration:", error);
    return null;
  }
}
