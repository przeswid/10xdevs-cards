/**
 * US-001: Account Registration - Validation Tests
 * TC-001-006 to TC-001-021
 */

import { test, expect } from "@playwright/test";
import { getValidRegistrationData, INVALID_USERNAMES, INVALID_PASSWORDS, INVALID_EMAILS } from "./helpers/test-data";

test.describe("US-001: Registration Validation", () => {
  test("TC-001-006: Empty form submission", async ({ page }) => {
    await page.goto("/register");

    // Submit empty form
    await page.click('button[type="submit"]');

    // Verify all required field errors are shown
    await expect(page.locator("text=Username is required")).toBeVisible();
    await expect(page.locator("text=Password is required")).toBeVisible();
    await expect(page.locator("text=Email is required")).toBeVisible();
    await expect(page.locator("text=First name is required")).toBeVisible();
    await expect(page.locator("text=Last name is required")).toBeVisible();
  });

  test("TC-001-007: Username too short", async ({ page }) => {
    const userData = getValidRegistrationData({
      username: INVALID_USERNAMES.tooShort,
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Username must be at least 3 characters")).toBeVisible();
  });

  test("TC-001-008: Username too long", async ({ page }) => {
    const userData = getValidRegistrationData({
      username: INVALID_USERNAMES.tooLong,
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Username must not exceed 50 characters")).toBeVisible();
  });

  test("TC-001-009: Username with invalid characters", async ({ page }) => {
    const userData = getValidRegistrationData({
      username: INVALID_USERNAMES.invalidChars,
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Username must contain only letters, numbers, and underscore")).toBeVisible();
  });

  test("TC-001-010: Password too short", async ({ page }) => {
    const userData = getValidRegistrationData({
      password: INVALID_PASSWORDS.tooShort,
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Password must be at least 8 characters")).toBeVisible();
  });

  test("TC-001-011: Password too long", async ({ page }) => {
    const userData = getValidRegistrationData({
      password: INVALID_PASSWORDS.tooLong,
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Password must not exceed 100 characters")).toBeVisible();
  });

  test("TC-001-012: Password without uppercase letter", async ({ page }) => {
    const userData = getValidRegistrationData({
      password: INVALID_PASSWORDS.noUppercase,
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Password must contain at least one uppercase letter")).toBeVisible();
  });

  test("TC-001-013: Password without lowercase letter", async ({ page }) => {
    const userData = getValidRegistrationData({
      password: INVALID_PASSWORDS.noLowercase,
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Password must contain at least one lowercase letter")).toBeVisible();
  });

  test("TC-001-014: Password without number", async ({ page }) => {
    const userData = getValidRegistrationData({
      password: INVALID_PASSWORDS.noNumber,
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Password must contain at least one number")).toBeVisible();
  });

  test("TC-001-015: Password without special character", async ({ page }) => {
    const userData = getValidRegistrationData({
      password: INVALID_PASSWORDS.noSpecial,
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Password must contain at least one special character")).toBeVisible();
  });

  test("TC-001-016: Invalid email format", async ({ page }) => {
    const userData = getValidRegistrationData({
      email: INVALID_EMAILS.noAt,
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Please enter a valid email address")).toBeVisible();
  });

  test("TC-001-017: Email too long", async ({ page }) => {
    const userData = getValidRegistrationData({
      email: INVALID_EMAILS.tooLong,
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Email must not exceed 100 characters")).toBeVisible();
  });

  test("TC-001-018: First name too long", async ({ page }) => {
    const userData = getValidRegistrationData({
      firstName: "a".repeat(51),
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=First name must not exceed 50 characters")).toBeVisible();
  });

  test("TC-001-019: Last name too long", async ({ page }) => {
    const userData = getValidRegistrationData({
      lastName: "b".repeat(51),
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Last name must not exceed 50 characters")).toBeVisible();
  });

  test("TC-001-020: Empty first name", async ({ page }) => {
    const userData = getValidRegistrationData({
      firstName: "",
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=First name is required")).toBeVisible();
  });

  test("TC-001-021: Empty last name", async ({ page }) => {
    const userData = getValidRegistrationData({
      lastName: "",
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    await expect(page.locator("text=Last name is required")).toBeVisible();
  });
});
