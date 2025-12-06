/**
 * US-001: Account Registration - Happy Path Tests
 * TC-001-001 to TC-001-005
 */

import { test, expect } from "@playwright/test";
import { getValidRegistrationData, generateUniqueShortUsername, generateUniqueLongUsername } from "./helpers/test-data";
import { registerUser, waitForRedirect } from "./helpers/form-helpers";

test.describe("US-001: Registration Happy Path", () => {
  test("TC-001-001: Successful registration with valid data", async ({ page }) => {
    const userData = getValidRegistrationData();

    await page.goto("/register");

    // Fill form
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    // Submit
    await page.click('button[type="submit"]');

    // Verify success message
    await expect(page.locator("text=Registration successful")).toBeVisible();

    // Verify redirect to /generate
    await waitForRedirect(page, /\/generate/);

    // Verify JWT token is stored in localStorage
    const token = await page.evaluate(() => localStorage.getItem("auth_token"));
    expect(token).toBeTruthy();
  });

  test("TC-001-002: Registration form is accessible", async ({ page }) => {
    await page.goto("/register");

    // Verify page loaded
    await expect(page).toHaveURL("/register");
    await expect(page.locator("h1")).toContainText("Create an account");

    // Verify all fields are visible
    await expect(page.locator("#username")).toBeVisible();
    await expect(page.locator("#email")).toBeVisible();
    await expect(page.locator("#firstName")).toBeVisible();
    await expect(page.locator("#lastName")).toBeVisible();
    await expect(page.locator("#password")).toBeVisible();

    // Verify submit button
    await expect(page.locator('button[type="submit"]')).toBeVisible();
    await expect(page.locator('button[type="submit"]')).toHaveText("Register");

    // Verify link to login
    await expect(page.locator('a[href="/login"]')).toBeVisible();
  });

  test("TC-001-003: Registration with minimum valid username (3 chars)", async ({ page }) => {
    const userData = getValidRegistrationData({
      username: generateUniqueShortUsername(), // 3 character minimum
      firstName: "Al", // Backend requires at least 2 chars
      lastName: "Bo",
    });

    await registerUser(page, userData);

    // Verify redirect to /generate
    await waitForRedirect(page, /\/generate/);
  });

  test("TC-001-004: Registration with maximum valid username (50 chars)", async ({ page }) => {
    const userData = getValidRegistrationData({
      username: generateUniqueLongUsername(), // 50 character maximum
    });

    await registerUser(page, userData);

    // Verify redirect to /generate
    await waitForRedirect(page, /\/generate/);
  });

  test("TC-001-005: Registration with special characters in name", async ({ page }) => {
    const userData = getValidRegistrationData({
      firstName: "Mary-Jane",
      lastName: "O'Connor",
    });

    await registerUser(page, userData);

    // Verify redirect to /generate
    await waitForRedirect(page, /\/generate/);
  });
});
