import { describe, it, expect, beforeEach, vi } from "vitest";
import { setToken, getToken, isTokenValid, clearToken, getTimeUntilExpiration } from "./tokenService";

describe("tokenService", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  describe("setToken", () => {
    it("stores token in localStorage", () => {
      setToken("test-token", 3600);
      expect(localStorage.getItem("auth_token")).toBe("test-token");
    });

    it("stores expiry timestamp in localStorage", () => {
      vi.setSystemTime(new Date("2024-01-01T00:00:00.000Z"));
      setToken("test-token", 3600);

      const expiry = localStorage.getItem("auth_token_expiry");
      expect(expiry).toBe((Date.now() + 3600 * 1000).toString());
    });

    it("calculates correct expiry for different durations", () => {
      vi.setSystemTime(new Date("2024-01-01T00:00:00.000Z"));
      const baseTime = Date.now();

      setToken("token1", 60);
      expect(localStorage.getItem("auth_token_expiry")).toBe((baseTime + 60000).toString());

      setToken("token2", 7200);
      expect(localStorage.getItem("auth_token_expiry")).toBe((baseTime + 7200000).toString());
    });
  });

  describe("getToken", () => {
    it("returns token when valid and not expired", () => {
      vi.setSystemTime(new Date("2024-01-01T00:00:00.000Z"));
      setToken("valid-token", 3600);

      const token = getToken();
      expect(token).toBe("valid-token");
    });

    it("returns null when token does not exist", () => {
      const token = getToken();
      expect(token).toBeNull();
    });

    it("returns null when expiry does not exist", () => {
      localStorage.setItem("auth_token", "token-without-expiry");
      const token = getToken();
      expect(token).toBeNull();
    });

    it("returns null and clears storage when token is expired", () => {
      vi.setSystemTime(new Date("2024-01-01T00:00:00.000Z"));
      setToken("expired-token", 3600);

      vi.advanceTimersByTime(3600 * 1000 + 1);

      const token = getToken();
      expect(token).toBeNull();
      expect(localStorage.getItem("auth_token")).toBeNull();
      expect(localStorage.getItem("auth_token_expiry")).toBeNull();
    });

    it("returns token when exactly at expiry time minus 1ms", () => {
      vi.setSystemTime(new Date("2024-01-01T00:00:00.000Z"));
      setToken("valid-token", 3600);

      vi.advanceTimersByTime(3600 * 1000 - 1);

      const token = getToken();
      expect(token).toBe("valid-token");
    });
  });

  describe("isTokenValid", () => {
    it("returns true when token exists and is not expired", () => {
      vi.setSystemTime(new Date("2024-01-01T00:00:00.000Z"));
      setToken("valid-token", 3600);

      expect(isTokenValid()).toBe(true);
    });

    it("returns false when no token exists", () => {
      expect(isTokenValid()).toBe(false);
    });

    it("returns false when token is expired", () => {
      vi.setSystemTime(new Date("2024-01-01T00:00:00.000Z"));
      setToken("expired-token", 3600);

      vi.advanceTimersByTime(3600 * 1000 + 1);

      expect(isTokenValid()).toBe(false);
    });
  });

  describe("clearToken", () => {
    it("removes token from localStorage", () => {
      setToken("token-to-clear", 3600);
      clearToken();
      expect(localStorage.getItem("auth_token")).toBeNull();
    });

    it("removes expiry from localStorage", () => {
      setToken("token-to-clear", 3600);
      clearToken();
      expect(localStorage.getItem("auth_token_expiry")).toBeNull();
    });

    it("does not throw when storage is already empty", () => {
      expect(() => clearToken()).not.toThrow();
    });
  });

  describe("getTimeUntilExpiration", () => {
    it("returns correct time until expiration", () => {
      vi.setSystemTime(new Date("2024-01-01T00:00:00.000Z"));
      setToken("token", 3600);

      vi.advanceTimersByTime(1000 * 1000);

      const timeLeft = getTimeUntilExpiration();
      expect(timeLeft).toBe(2600 * 1000);
    });

    it("returns null when no expiry exists", () => {
      const timeLeft = getTimeUntilExpiration();
      expect(timeLeft).toBeNull();
    });

    it("returns null when token is already expired", () => {
      vi.setSystemTime(new Date("2024-01-01T00:00:00.000Z"));
      setToken("token", 3600);

      vi.advanceTimersByTime(3600 * 1000 + 1);

      const timeLeft = getTimeUntilExpiration();
      expect(timeLeft).toBeNull();
    });

    it("returns exact remaining time at boundary", () => {
      vi.setSystemTime(new Date("2024-01-01T00:00:00.000Z"));
      setToken("token", 60);

      vi.advanceTimersByTime(30 * 1000);

      const timeLeft = getTimeUntilExpiration();
      expect(timeLeft).toBe(30 * 1000);
    });
  });
});
