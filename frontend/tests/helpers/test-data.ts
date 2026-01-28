/**
 * Test data helpers
 * Generate unique test data to avoid conflicts between test runs
 */

export function generateUniqueUsername(prefix = "testuser"): string {
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 1000);
  return `${prefix}_${timestamp}_${random}`;
}

export function generateUniqueEmail(prefix = "test"): string {
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 1000);
  return `${prefix}_${timestamp}_${random}@example.com`;
}

export function generateUniqueShortUsername(): string {
  // Generate 3-character username (minimum valid length) with uniqueness
  const timestamp = Date.now();
  const suffix = (timestamp % 1000).toString().padStart(3, "0");
  return suffix.substring(0, 3); // Last 3 digits of timestamp
}

export function generateUniqueLongUsername(): string {
  // Generate 50-character username (maximum valid length) with uniqueness
  const timestamp = Date.now();
  const random = Math.floor(Math.random() * 1000);
  const base = `user_${timestamp}_${random}`;
  return base.substring(0, 50); // Exactly 50 chars
}

export interface ValidRegistrationData {
  username: string;
  password: string;
  email: string;
  firstName: string;
  lastName: string;
}

export function getValidRegistrationData(overrides?: Partial<ValidRegistrationData>): ValidRegistrationData {
  return {
    username: generateUniqueUsername(),
    password: "Test@1234",
    email: generateUniqueEmail(),
    firstName: "John",
    lastName: "Doe",
    ...overrides,
  };
}

export const VALID_PASSWORDS = {
  standard: "Test@1234",
  secure: "SecureP@ss123",
  minimal: "ValidP@1",
};

export const INVALID_USERNAMES = {
  tooShort: "ab",
  tooLong: "a".repeat(51),
  invalidChars: "test@user!",
  withSpace: "user name",
};

export const INVALID_PASSWORDS = {
  tooShort: "Test@12",
  tooLong: "T".repeat(101) + "@1",
  noUppercase: "test@1234",
  noLowercase: "TEST@1234",
  noNumber: "Test@abcd",
  noSpecial: "Test1234",
};

export const INVALID_EMAILS = {
  noAt: "notanemail",
  noDomain: "user@",
  noLocal: "@example.com",
  noTld: "user@domain",
  tooLong: "a".repeat(100) + "@example.com",
};
