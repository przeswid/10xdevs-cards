/**
 * US-001: Account Registration - Edge Cases
 * TC-001-022 to TC-001-032
 */

import { test, expect } from "@playwright/test";
import { getValidRegistrationData } from "./helpers/test-data";
import { registerUser, waitForRedirect } from "./helpers/form-helpers";

test.describe("US-001: Registration Edge Cases", () => {
  test("TC-001-022: Duplicate username", async ({ page, context }) => {
    const userData = getValidRegistrationData();

    // First registration - should succeed
    await registerUser(page, userData);
    await waitForRedirect(page, /\/generate/, 10000);

    // Clear storage and try to register with same username but different email
    await context.clearCookies();
    await page.evaluate(() => localStorage.clear());

    const duplicateData = getValidRegistrationData({
      username: userData.username, // Same username
      email: getValidRegistrationData().email, // Different email
    });

    await page.goto("/register");
    await page.fill("#username", duplicateData.username);
    await page.fill("#email", duplicateData.email);
    await page.fill("#firstName", duplicateData.firstName);
    await page.fill("#lastName", duplicateData.lastName);
    await page.fill("#password", duplicateData.password);

    await page.click('button[type="submit"]');

    // Should show error (not redirect)
    await expect(page.locator("text=Registration failed")).toBeVisible();

    // Should still be on register page
    await expect(page).toHaveURL("/register");
  });

  test("TC-001-023: Duplicate email", async ({ page, context }) => {
    const userData = getValidRegistrationData();

    // First registration - should succeed
    await registerUser(page, userData);
    await waitForRedirect(page, /\/generate/, 10000);

    // Clear storage and try to register with different username but same email
    await context.clearCookies();
    await page.evaluate(() => localStorage.clear());

    const duplicateData = getValidRegistrationData({
      username: getValidRegistrationData().username, // Different username
      email: userData.email, // Same email
    });

    await page.goto("/register");
    await page.fill("#username", duplicateData.username);
    await page.fill("#email", duplicateData.email);
    await page.fill("#firstName", duplicateData.firstName);
    await page.fill("#lastName", duplicateData.lastName);
    await page.fill("#password", duplicateData.password);

    await page.click('button[type="submit"]');

    // Should show error (not redirect)
    await expect(page.locator("text=Registration failed")).toBeVisible();

    // Should still be on register page
    await expect(page).toHaveURL("/register");
  });

  test("TC-001-024: Real-time validation on blur", async ({ page }) => {
    await page.goto("/register");

    // Enter invalid username (too short)
    await page.fill("#username", "ab");

    // Blur the field by clicking elsewhere
    await page.locator("#email").click();

    // Error should appear
    await expect(page.locator("text=Username must be at least 3 characters")).toBeVisible();
  });

  test("TC-001-025: Error clears on valid input", async ({ page }) => {
    await page.goto("/register");

    // Enter invalid username
    await page.fill("#username", "ab");

    // Blur by clicking elsewhere
    await page.locator("#email").click();

    // Error appears
    await expect(page.locator("text=Username must be at least 3 characters")).toBeVisible();

    // Now enter valid username
    await page.fill("#username", "abc");

    // Error should disappear
    await expect(page.locator("text=Username must be at least 3 characters")).not.toBeVisible();
  });

  test("TC-001-026: Form disabled during submission", async ({ page }) => {
    const userData = getValidRegistrationData();

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    // Slow down network to ensure we can catch the loading state
    await page.route("**/auth/register", async (route) => {
      await new Promise((resolve) => setTimeout(resolve, 500));
      await route.continue();
    });

    // Click submit
    await page.click('button[type="submit"]');

    // Button should show loading state
    await expect(page.locator('button[type="submit"]')).toHaveText("Registering...");

    // Form fields should be disabled
    await expect(page.locator("#username")).toBeDisabled();
    await expect(page.locator("#email")).toBeDisabled();
    await expect(page.locator("#password")).toBeDisabled();
  });

  test("TC-001-029: Already authenticated user redirected", async ({ page }) => {
    const userData = getValidRegistrationData();

    // Register and login
    await registerUser(page, userData);
    await waitForRedirect(page, /\/generate/);

    // Try to navigate to register page while logged in
    await page.goto("/register");

    // Should be redirected back to /generate
    await expect(page).toHaveURL(/\/generate/);
  });

  test("TC-001-030: Link to login page", async ({ page }) => {
    await page.goto("/register");

    // Click login link
    await page.click('a[href="/login"]');

    // Should navigate to login page
    await expect(page).toHaveURL("/login");
  });

  test("TC-001-031: Password field masking", async ({ page }) => {
    await page.goto("/register");

    const passwordField = page.locator("#password");

    // Verify password field is type="password"
    await expect(passwordField).toHaveAttribute("type", "password");

    // Type password
    await passwordField.fill("Test@1234");

    // Verify the field has the password (but it's masked in UI)
    await expect(passwordField).toHaveValue("Test@1234");
  });

  test("TC-001-032: Form retains data on validation error", async ({ page }) => {
    const userData = getValidRegistrationData({
      password: "short", // Invalid password
    });

    await page.goto("/register");
    await page.fill("#username", userData.username);
    await page.fill("#email", userData.email);
    await page.fill("#firstName", userData.firstName);
    await page.fill("#lastName", userData.lastName);
    await page.fill("#password", userData.password);

    await page.click('button[type="submit"]');

    // Error appears
    await expect(page.locator("text=Password must be at least 8 characters")).toBeVisible();

    // All fields except password should retain values
    await expect(page.locator("#username")).toHaveValue(userData.username);
    await expect(page.locator("#email")).toHaveValue(userData.email);
    await expect(page.locator("#firstName")).toHaveValue(userData.firstName);
    await expect(page.locator("#lastName")).toHaveValue(userData.lastName);
  });
});
