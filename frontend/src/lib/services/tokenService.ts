/**
 * Token Service
 * Centralized JWT token storage and retrieval
 * Uses localStorage for persistence across browser sessions
 */

const TOKEN_KEY = "auth_token";
const TOKEN_EXPIRY_KEY = "auth_token_expiry";

export interface ITokenService {
  setToken(token: string, expiresIn: number): void;
  getToken(): string | null;
  isTokenValid(): boolean;
  clearToken(): void;
  getTimeUntilExpiration(): number | null;
}

/**
 * Store JWT token and expiration timestamp
 * @param token - JWT token string
 * @param expiresIn - Seconds until token expires
 */
export function setToken(token: string, expiresIn: number): void {
  const expiryTimestamp = Date.now() + expiresIn * 1000;
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(TOKEN_EXPIRY_KEY, expiryTimestamp.toString());
}

/**
 * Retrieve current token if valid
 * @returns Token string or null if expired/missing
 */
export function getToken(): string | null {
  const token = localStorage.getItem(TOKEN_KEY);
  const expiry = localStorage.getItem(TOKEN_EXPIRY_KEY);

  if (!token || !expiry) {
    return null;
  }

  const expiryTimestamp = parseInt(expiry, 10);
  if (Date.now() >= expiryTimestamp) {
    // Token expired, clear it
    clearToken();
    return null;
  }

  return token;
}

/**
 * Check if token is valid and not expired
 * @returns boolean indicating token validity
 */
export function isTokenValid(): boolean {
  return getToken() !== null;
}

/**
 * Clear token from storage (on logout or expiration)
 */
export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(TOKEN_EXPIRY_KEY);
}

/**
 * Get time until token expires
 * @returns Milliseconds until expiration, or null if no valid token
 */
export function getTimeUntilExpiration(): number | null {
  const expiry = localStorage.getItem(TOKEN_EXPIRY_KEY);

  if (!expiry) {
    return null;
  }

  const expiryTimestamp = parseInt(expiry, 10);
  const timeUntilExpiry = expiryTimestamp - Date.now();

  return timeUntilExpiry > 0 ? timeUntilExpiry : null;
}

const tokenService = {
  setToken,
  getToken,
  isTokenValid,
  clearToken,
  getTimeUntilExpiration,
};

export default tokenService;
