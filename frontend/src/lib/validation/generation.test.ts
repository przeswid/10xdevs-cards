import { describe, it, expect } from "vitest";
import { inputTextSchema, flashcardContentSchema, validateInputText, validateFlashcardContent } from "./generation";

describe("inputTextSchema", () => {
  it("parses valid text with 1000 characters", () => {
    const text = "a".repeat(1000);
    const result = inputTextSchema.safeParse(text);
    expect(result.success).toBe(true);
  });

  it("parses valid text with 10000 characters", () => {
    const text = "a".repeat(10000);
    const result = inputTextSchema.safeParse(text);
    expect(result.success).toBe(true);
  });

  it("fails for text with 999 characters", () => {
    const text = "a".repeat(999);
    const result = inputTextSchema.safeParse(text);
    expect(result.success).toBe(false);
  });

  it("fails for text with 10001 characters", () => {
    const text = "a".repeat(10001);
    const result = inputTextSchema.safeParse(text);
    expect(result.success).toBe(false);
  });

  it("trims whitespace and validates", () => {
    const text = " " + "a".repeat(1000) + " ";
    const result = inputTextSchema.safeParse(text);
    expect(result.success).toBe(true);
  });
});

describe("flashcardContentSchema", () => {
  it("parses valid content with 1 character", () => {
    const result = flashcardContentSchema.safeParse("a");
    expect(result.success).toBe(true);
  });

  it("parses valid content with 1000 characters", () => {
    const result = flashcardContentSchema.safeParse("a".repeat(1000));
    expect(result.success).toBe(true);
  });

  it("fails for empty string", () => {
    const result = flashcardContentSchema.safeParse("");
    expect(result.success).toBe(false);
  });

  it("fails for content with 1001 characters", () => {
    const result = flashcardContentSchema.safeParse("a".repeat(1001));
    expect(result.success).toBe(false);
  });
});

describe("validateInputText", () => {
  it("returns valid for text with exactly 1000 characters", () => {
    const result = validateInputText("a".repeat(1000));
    expect(result.isValid).toBe(true);
    expect(result.error).toBeUndefined();
  });

  it("returns valid for text with 5000 characters", () => {
    const result = validateInputText("a".repeat(5000));
    expect(result.isValid).toBe(true);
  });

  it("returns valid for text with exactly 10000 characters", () => {
    const result = validateInputText("a".repeat(10000));
    expect(result.isValid).toBe(true);
  });

  it("returns error for text shorter than 1000 characters", () => {
    const result = validateInputText("a".repeat(500));
    expect(result.isValid).toBe(false);
    expect(result.error).toBe("Minimum 1000 characters required");
  });

  it("returns error for text longer than 10000 characters", () => {
    const result = validateInputText("a".repeat(10001));
    expect(result.isValid).toBe(false);
    expect(result.error).toBe("Maximum 10000 characters allowed");
  });

  it("returns error for empty text", () => {
    const result = validateInputText("");
    expect(result.isValid).toBe(false);
    expect(result.error).toBeDefined();
  });

  it("trims whitespace before validation", () => {
    const result = validateInputText("   " + "a".repeat(1000) + "   ");
    expect(result.isValid).toBe(true);
  });
});

describe("validateFlashcardContent", () => {
  it("returns valid for content with 1 character", () => {
    const result = validateFlashcardContent("a");
    expect(result.isValid).toBe(true);
    expect(result.error).toBeUndefined();
  });

  it("returns valid for content with 1000 characters", () => {
    const result = validateFlashcardContent("a".repeat(1000));
    expect(result.isValid).toBe(true);
  });

  it("returns error for empty content", () => {
    const result = validateFlashcardContent("");
    expect(result.isValid).toBe(false);
    expect(result.error).toContain("at least 1 character");
  });

  it("returns error for content longer than 1000 characters", () => {
    const result = validateFlashcardContent("a".repeat(1001));
    expect(result.isValid).toBe(false);
    expect(result.error).toContain("at most 1000 characters");
  });

  it("replaces 'Content' with custom field name in error message", () => {
    const result = validateFlashcardContent("", "Front");
    expect(result.isValid).toBe(false);
    expect(result.error).toContain("Front");
  });

  it("uses default field name when not provided", () => {
    const result = validateFlashcardContent("");
    expect(result.isValid).toBe(false);
    expect(result.error).toContain("Content");
  });
});
