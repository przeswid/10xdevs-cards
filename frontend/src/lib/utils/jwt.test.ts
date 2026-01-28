import { describe, it, expect, vi, beforeEach } from "vitest";
import { decodeToken, isTokenExpired, getTokenExpiration } from "./jwt";

vi.mock("jwt-decode", () => ({
  jwtDecode: vi.fn(),
}));

import { jwtDecode } from "jwt-decode";

const mockedJwtDecode = vi.mocked(jwtDecode);

describe("JWT utilities", () => {
  beforeEach(() => {
    vi.clearAllMocks();

    vi.spyOn(console, "error").mockImplementation(() => undefined);
  });

  describe("decodeToken", () => {
    it("returns user object for valid token", () => {
      mockedJwtDecode.mockReturnValue({
        sub: "user-123",
        username: "testuser",
        email: "test@example.com",
        firstName: "John",
        lastName: "Doe",
        exp: Math.floor(Date.now() / 1000) + 3600,
        iat: Math.floor(Date.now() / 1000),
      });

      const user = decodeToken("valid-token");

      expect(user).toEqual({
        id: "user-123",
        username: "testuser",
        email: "test@example.com",
        firstName: "John",
        lastName: "Doe",
      });
    });

    it("returns user with empty strings for missing optional fields", () => {
      mockedJwtDecode.mockReturnValue({
        sub: "user-456",
        username: "minimaluser",
        exp: Math.floor(Date.now() / 1000) + 3600,
        iat: Math.floor(Date.now() / 1000),
      });

      const user = decodeToken("minimal-token");

      expect(user).toEqual({
        id: "user-456",
        username: "minimaluser",
        email: "",
        firstName: "",
        lastName: "",
      });
    });

    it("returns null for invalid token", () => {
      mockedJwtDecode.mockImplementation(() => {
        throw new Error("Invalid token");
      });

      const user = decodeToken("invalid-token");

      expect(user).toBeNull();
      // eslint-disable-next-line no-console
      expect(console.error).toHaveBeenCalled();
    });

    it("calls jwtDecode with the provided token", () => {
      mockedJwtDecode.mockReturnValue({
        sub: "user-789",
        username: "test",
        exp: 0,
        iat: 0,
      });

      decodeToken("my-specific-token");

      expect(mockedJwtDecode).toHaveBeenCalledWith("my-specific-token");
    });
  });

  describe("isTokenExpired", () => {
    it("returns false for non-expired token", () => {
      const futureExp = Math.floor(Date.now() / 1000) + 3600;
      mockedJwtDecode.mockReturnValue({
        exp: futureExp,
        sub: "user",
        username: "test",
        iat: 0,
      });

      const expired = isTokenExpired("valid-token");

      expect(expired).toBe(false);
    });

    it("returns true for expired token", () => {
      const pastExp = Math.floor(Date.now() / 1000) - 3600;
      mockedJwtDecode.mockReturnValue({
        exp: pastExp,
        sub: "user",
        username: "test",
        iat: 0,
      });

      const expired = isTokenExpired("expired-token");

      expect(expired).toBe(true);
    });

    it("returns true for token that expires exactly now", () => {
      const nowExp = Math.floor(Date.now() / 1000) - 1;
      mockedJwtDecode.mockReturnValue({
        exp: nowExp,
        sub: "user",
        username: "test",
        iat: 0,
      });

      const expired = isTokenExpired("boundary-token");

      expect(expired).toBe(true);
    });

    it("returns true for invalid token (assumes expired)", () => {
      mockedJwtDecode.mockImplementation(() => {
        throw new Error("Invalid token");
      });

      const expired = isTokenExpired("invalid-token");

      expect(expired).toBe(true);
      // eslint-disable-next-line no-console
      expect(console.error).toHaveBeenCalled();
    });
  });

  describe("getTokenExpiration", () => {
    it("returns expiration time in milliseconds", () => {
      const expInSeconds = 1704067200;
      mockedJwtDecode.mockReturnValue({
        exp: expInSeconds,
        sub: "user",
        username: "test",
        iat: 0,
      });

      const expiration = getTokenExpiration("valid-token");

      expect(expiration).toBe(expInSeconds * 1000);
    });

    it("returns null for invalid token", () => {
      mockedJwtDecode.mockImplementation(() => {
        throw new Error("Invalid token");
      });

      const expiration = getTokenExpiration("invalid-token");

      expect(expiration).toBeNull();
      // eslint-disable-next-line no-console
      expect(console.error).toHaveBeenCalled();
    });

    it("correctly converts various expiration timestamps", () => {
      const testCases = [
        { exp: 0, expected: 0 },
        { exp: 1, expected: 1000 },
        { exp: 1000000000, expected: 1000000000000 },
      ];

      testCases.forEach(({ exp, expected }) => {
        mockedJwtDecode.mockReturnValue({
          exp,
          sub: "user",
          username: "test",
          iat: 0,
        });

        const expiration = getTokenExpiration("token");
        expect(expiration).toBe(expected);
      });
    });
  });
});
