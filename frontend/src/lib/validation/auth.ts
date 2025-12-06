/**
 * Authentication Validation Service
 * Centralized validation logic for authentication forms
 */

import type { RegisterRequest, LoginRequest, ValidationResult, FormValidationResult } from "@/lib/api/types";

export interface IAuthValidation {
  validateUsername(username: string): ValidationResult;
  validatePassword(password: string): ValidationResult;
  validateEmail(email: string): ValidationResult;
  validateName(name: string, fieldName: string): ValidationResult;
  validateRegistrationForm(data: RegisterRequest): FormValidationResult;
  validateLoginForm(data: LoginRequest): FormValidationResult;
}

/**
 * Validate username
 * Rules: 3-50 chars, alphanumeric and underscore only
 */
export function validateUsername(username: string): ValidationResult {
  const errors: string[] = [];

  if (!username || username.trim().length === 0) {
    errors.push("Username is required");
  } else if (username.length < 3) {
    errors.push("Username must be at least 3 characters");
  } else if (username.length > 50) {
    errors.push("Username must not exceed 50 characters");
  } else if (!/^[a-zA-Z0-9_]+$/.test(username)) {
    errors.push("Username must contain only letters, numbers, and underscore");
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
}

/**
 * Validate password
 * Rules: 8-100 chars, must contain uppercase, lowercase, number, and special char
 */
export function validatePassword(password: string): ValidationResult {
  const errors: string[] = [];

  if (!password || password.length === 0) {
    errors.push("Password is required");
  } else if (password.length < 8) {
    errors.push("Password must be at least 8 characters");
  } else if (password.length > 100) {
    errors.push("Password must not exceed 100 characters");
  } else {
    if (!/[A-Z]/.test(password)) {
      errors.push("Password must contain at least one uppercase letter");
    }
    if (!/[a-z]/.test(password)) {
      errors.push("Password must contain at least one lowercase letter");
    }
    if (!/[0-9]/.test(password)) {
      errors.push("Password must contain at least one number");
    }
    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
      errors.push("Password must contain at least one special character");
    }
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
}

/**
 * Validate email
 * Rules: Valid email format, max 100 chars
 */
export function validateEmail(email: string): ValidationResult {
  const errors: string[] = [];

  if (!email || email.trim().length === 0) {
    errors.push("Email is required");
  } else if (email.length > 100) {
    errors.push("Email must not exceed 100 characters");
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    errors.push("Please enter a valid email address");
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
}

/**
 * Validate name fields (firstName, lastName)
 * Rules: 1-50 chars, required
 */
export function validateName(name: string, fieldName: string): ValidationResult {
  const errors: string[] = [];

  if (!name || name.trim().length === 0) {
    errors.push(`${fieldName} is required`);
  } else if (name.length > 50) {
    errors.push(`${fieldName} must not exceed 50 characters`);
  }

  return {
    isValid: errors.length === 0,
    errors,
  };
}

/**
 * Validate entire registration form
 */
export function validateRegistrationForm(data: RegisterRequest): FormValidationResult {
  const fieldErrors: Record<string, string[]> = {};

  const usernameResult = validateUsername(data.username);
  if (!usernameResult.isValid) {
    fieldErrors.username = usernameResult.errors;
  }

  const passwordResult = validatePassword(data.password);
  if (!passwordResult.isValid) {
    fieldErrors.password = passwordResult.errors;
  }

  const emailResult = validateEmail(data.email);
  if (!emailResult.isValid) {
    fieldErrors.email = emailResult.errors;
  }

  const firstNameResult = validateName(data.firstName, "First name");
  if (!firstNameResult.isValid) {
    fieldErrors.firstName = firstNameResult.errors;
  }

  const lastNameResult = validateName(data.lastName, "Last name");
  if (!lastNameResult.isValid) {
    fieldErrors.lastName = lastNameResult.errors;
  }

  return {
    isValid: Object.keys(fieldErrors).length === 0,
    fieldErrors,
  };
}

/**
 * Validate login form
 */
export function validateLoginForm(data: LoginRequest): FormValidationResult {
  const fieldErrors: Record<string, string[]> = {};

  if (!data.username || data.username.trim().length === 0) {
    fieldErrors.username = ["Username is required"];
  }

  if (!data.password || data.password.length === 0) {
    fieldErrors.password = ["Password is required"];
  }

  return {
    isValid: Object.keys(fieldErrors).length === 0,
    fieldErrors,
  };
}

const authValidation = {
  validateUsername,
  validatePassword,
  validateEmail,
  validateName,
  validateRegistrationForm,
  validateLoginForm,
};

export default authValidation;
