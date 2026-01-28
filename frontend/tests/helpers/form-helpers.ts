/**
 * Form interaction helpers
 * Reusable functions for interacting with registration and login forms
 */

import { Page, expect } from "@playwright/test";
import type { ValidRegistrationData } from "./test-data";

export async function fillRegistrationForm(page: Page, data: ValidRegistrationData) {
  await page.fill("#username", data.username);
  await page.fill("#email", data.email);
  await page.fill("#firstName", data.firstName);
  await page.fill("#lastName", data.lastName);
  await page.fill("#password", data.password);
}

export async function submitRegistrationForm(page: Page) {
  await page.click('button[type="submit"]');
}

export async function registerUser(page: Page, data: ValidRegistrationData) {
  await page.goto("/register");
  await fillRegistrationForm(page, data);
  await submitRegistrationForm(page);
}

export async function expectFieldError(page: Page, fieldId: string, errorMessage: string) {
  const errorLocator = page.locator(`#${fieldId}-error`);
  await expect(errorLocator).toBeVisible();
  await expect(errorLocator).toContainText(errorMessage);
}

export async function expectNoFieldError(page: Page, fieldId: string) {
  const errorLocator = page.locator(`#${fieldId}-error`);
  await expect(errorLocator).not.toBeVisible();
}

export async function expectFormError(page: Page, errorMessage: string) {
  const alert = page.locator('[role="alert"]');
  await expect(alert).toBeVisible();
  await expect(alert).toContainText(errorMessage);
}

export async function waitForSuccessMessage(page: Page) {
  await expect(page.locator("text=Registration successful")).toBeVisible();
}

export async function waitForRedirect(page: Page, expectedUrl: string, timeout = 5000) {
  await page.waitForURL(expectedUrl, { timeout });
}
