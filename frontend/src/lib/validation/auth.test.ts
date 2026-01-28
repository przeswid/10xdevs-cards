import { describe, it, expect } from "vitest";
import {
  validateUsername,
  validatePassword,
  validateEmail,
  validateName,
  validateRegistrationForm,
  validateLoginForm,
} from "./auth";

describe("validateUsername", () => {
  it("returns valid for correct username", () => {
    const result = validateUsername("validUser123");
    expect(result.isValid).toBe(true);
    expect(result.errors).toHaveLength(0);
  });

  it("returns valid for username with underscore", () => {
    const result = validateUsername("valid_user");
    expect(result.isValid).toBe(true);
  });

  it("returns error for empty username", () => {
    const result = validateUsername("");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Username is required");
  });

  it("returns error for whitespace-only username", () => {
    const result = validateUsername("   ");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Username is required");
  });

  it("returns error for username shorter than 3 characters", () => {
    const result = validateUsername("ab");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Username must be at least 3 characters");
  });

  it("returns error for username longer than 50 characters", () => {
    const result = validateUsername("a".repeat(51));
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Username must not exceed 50 characters");
  });

  it("returns error for username with special characters", () => {
    const result = validateUsername("user@name");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Username must contain only letters, numbers, and underscore");
  });

  it("returns error for username with spaces", () => {
    const result = validateUsername("user name");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Username must contain only letters, numbers, and underscore");
  });

  it("accepts boundary value of 3 characters", () => {
    const result = validateUsername("abc");
    expect(result.isValid).toBe(true);
  });

  it("accepts boundary value of 50 characters", () => {
    const result = validateUsername("a".repeat(50));
    expect(result.isValid).toBe(true);
  });
});

describe("validatePassword", () => {
  it("returns valid for strong password", () => {
    const result = validatePassword("SecurePass1!");
    expect(result.isValid).toBe(true);
    expect(result.errors).toHaveLength(0);
  });

  it("returns error for empty password", () => {
    const result = validatePassword("");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Password is required");
  });

  it("returns error for password shorter than 8 characters", () => {
    const result = validatePassword("Short1!");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Password must be at least 8 characters");
  });

  it("returns error for password longer than 100 characters", () => {
    const result = validatePassword("Aa1!" + "a".repeat(97));
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Password must not exceed 100 characters");
  });

  it("returns error for password without uppercase letter", () => {
    const result = validatePassword("password123!");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Password must contain at least one uppercase letter");
  });

  it("returns error for password without lowercase letter", () => {
    const result = validatePassword("PASSWORD123!");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Password must contain at least one lowercase letter");
  });

  it("returns error for password without number", () => {
    const result = validatePassword("SecurePassword!");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Password must contain at least one number");
  });

  it("returns error for password without special character", () => {
    const result = validatePassword("SecurePassword1");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Password must contain at least one special character");
  });

  it("returns multiple errors for password missing multiple requirements", () => {
    const result = validatePassword("password");
    expect(result.isValid).toBe(false);
    expect(result.errors.length).toBeGreaterThan(1);
  });

  it("accepts boundary value of 8 characters", () => {
    const result = validatePassword("Passwo1!");
    expect(result.isValid).toBe(true);
  });

  it("accepts boundary value of 100 characters", () => {
    const result = validatePassword("Aa1!" + "a".repeat(96));
    expect(result.isValid).toBe(true);
  });
});

describe("validateEmail", () => {
  it("returns valid for correct email", () => {
    const result = validateEmail("user@example.com");
    expect(result.isValid).toBe(true);
    expect(result.errors).toHaveLength(0);
  });

  it("returns valid for email with subdomain", () => {
    const result = validateEmail("user@mail.example.com");
    expect(result.isValid).toBe(true);
  });

  it("returns error for empty email", () => {
    const result = validateEmail("");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Email is required");
  });

  it("returns error for whitespace-only email", () => {
    const result = validateEmail("   ");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Email is required");
  });

  it("returns error for email without @", () => {
    const result = validateEmail("userexample.com");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Please enter a valid email address");
  });

  it("returns error for email without domain", () => {
    const result = validateEmail("user@");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Please enter a valid email address");
  });

  it("returns error for email without TLD", () => {
    const result = validateEmail("user@example");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Please enter a valid email address");
  });

  it("returns error for email longer than 100 characters", () => {
    const result = validateEmail("a".repeat(92) + "@test.com");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Email must not exceed 100 characters");
  });

  it("accepts email at boundary of 100 characters", () => {
    const result = validateEmail("a".repeat(90) + "@test.com");
    expect(result.isValid).toBe(true);
  });
});

describe("validateName", () => {
  it("returns valid for correct name", () => {
    const result = validateName("John", "First name");
    expect(result.isValid).toBe(true);
    expect(result.errors).toHaveLength(0);
  });

  it("returns error for empty name", () => {
    const result = validateName("", "First name");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("First name is required");
  });

  it("returns error for whitespace-only name", () => {
    const result = validateName("   ", "Last name");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("Last name is required");
  });

  it("returns error for name longer than 50 characters", () => {
    const result = validateName("a".repeat(51), "First name");
    expect(result.isValid).toBe(false);
    expect(result.errors).toContain("First name must not exceed 50 characters");
  });

  it("accepts name at boundary of 50 characters", () => {
    const result = validateName("a".repeat(50), "First name");
    expect(result.isValid).toBe(true);
  });

  it("accepts single character name", () => {
    const result = validateName("J", "First name");
    expect(result.isValid).toBe(true);
  });
});

describe("validateRegistrationForm", () => {
  const validData = {
    username: "validuser",
    password: "SecurePass1!",
    email: "user@example.com",
    firstName: "John",
    lastName: "Doe",
  };

  it("returns valid for correct registration data", () => {
    const result = validateRegistrationForm(validData);
    expect(result.isValid).toBe(true);
    expect(Object.keys(result.fieldErrors)).toHaveLength(0);
  });

  it("returns field errors for invalid username", () => {
    const result = validateRegistrationForm({ ...validData, username: "ab" });
    expect(result.isValid).toBe(false);
    expect(result.fieldErrors.username).toBeDefined();
    expect(result.fieldErrors.username.length).toBeGreaterThan(0);
  });

  it("returns field errors for invalid password", () => {
    const result = validateRegistrationForm({ ...validData, password: "weak" });
    expect(result.isValid).toBe(false);
    expect(result.fieldErrors.password).toBeDefined();
  });

  it("returns field errors for invalid email", () => {
    const result = validateRegistrationForm({ ...validData, email: "invalid" });
    expect(result.isValid).toBe(false);
    expect(result.fieldErrors.email).toBeDefined();
  });

  it("returns field errors for invalid firstName", () => {
    const result = validateRegistrationForm({ ...validData, firstName: "" });
    expect(result.isValid).toBe(false);
    expect(result.fieldErrors.firstName).toBeDefined();
  });

  it("returns field errors for invalid lastName", () => {
    const result = validateRegistrationForm({ ...validData, lastName: "" });
    expect(result.isValid).toBe(false);
    expect(result.fieldErrors.lastName).toBeDefined();
  });

  it("returns multiple field errors for multiple invalid fields", () => {
    const result = validateRegistrationForm({
      username: "",
      password: "",
      email: "",
      firstName: "",
      lastName: "",
    });
    expect(result.isValid).toBe(false);
    expect(Object.keys(result.fieldErrors).length).toBe(5);
  });
});

describe("validateLoginForm", () => {
  it("returns valid for correct login data", () => {
    const result = validateLoginForm({ username: "user", password: "pass" });
    expect(result.isValid).toBe(true);
    expect(Object.keys(result.fieldErrors)).toHaveLength(0);
  });

  it("returns error for empty username", () => {
    const result = validateLoginForm({ username: "", password: "pass" });
    expect(result.isValid).toBe(false);
    expect(result.fieldErrors.username).toContain("Username is required");
  });

  it("returns error for whitespace-only username", () => {
    const result = validateLoginForm({ username: "   ", password: "pass" });
    expect(result.isValid).toBe(false);
    expect(result.fieldErrors.username).toContain("Username is required");
  });

  it("returns error for empty password", () => {
    const result = validateLoginForm({ username: "user", password: "" });
    expect(result.isValid).toBe(false);
    expect(result.fieldErrors.password).toContain("Password is required");
  });

  it("returns errors for both empty fields", () => {
    const result = validateLoginForm({ username: "", password: "" });
    expect(result.isValid).toBe(false);
    expect(result.fieldErrors.username).toBeDefined();
    expect(result.fieldErrors.password).toBeDefined();
  });
});
