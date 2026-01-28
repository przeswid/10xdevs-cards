import { describe, it, expect } from "vitest";
import { cn } from "./utils";

describe("cn", () => {
  it("merges single class name", () => {
    expect(cn("text-red-500")).toBe("text-red-500");
  });

  it("merges multiple class names", () => {
    expect(cn("text-red-500", "bg-blue-500")).toBe("text-red-500 bg-blue-500");
  });

  it("handles empty strings", () => {
    expect(cn("", "text-red-500", "")).toBe("text-red-500");
  });

  it("handles undefined values", () => {
    expect(cn("text-red-500", undefined, "bg-blue-500")).toBe("text-red-500 bg-blue-500");
  });

  it("handles null values", () => {
    expect(cn("text-red-500", null, "bg-blue-500")).toBe("text-red-500 bg-blue-500");
  });

  it("handles false values", () => {
    expect(cn("text-red-500", false, "bg-blue-500")).toBe("text-red-500 bg-blue-500");
  });

  it("handles conditional classes with boolean", () => {
    const isActive = true;
    expect(cn("base-class", isActive && "active-class")).toBe("base-class active-class");
  });

  it("excludes conditional classes when false", () => {
    const isActive = false;
    expect(cn("base-class", isActive && "active-class")).toBe("base-class");
  });

  it("merges conflicting Tailwind classes correctly", () => {
    expect(cn("p-4", "p-2")).toBe("p-2");
  });

  it("merges conflicting text colors correctly", () => {
    expect(cn("text-red-500", "text-blue-500")).toBe("text-blue-500");
  });

  it("handles object syntax", () => {
    expect(cn({ "text-red-500": true, "bg-blue-500": false })).toBe("text-red-500");
  });

  it("handles array syntax", () => {
    expect(cn(["text-red-500", "bg-blue-500"])).toBe("text-red-500 bg-blue-500");
  });

  it("handles mixed syntax", () => {
    expect(cn("base", ["arr1", "arr2"], { conditional: true })).toBe("base arr1 arr2 conditional");
  });

  it("returns empty string for no arguments", () => {
    expect(cn()).toBe("");
  });

  it("returns empty string for only falsy arguments", () => {
    expect(cn(null, undefined, false, "")).toBe("");
  });

  it("preserves non-conflicting classes", () => {
    expect(cn("text-red-500", "font-bold", "p-4")).toBe("text-red-500 font-bold p-4");
  });

  it("handles responsive prefixes correctly", () => {
    expect(cn("md:p-4", "md:p-2")).toBe("md:p-2");
  });

  it("handles hover states correctly", () => {
    expect(cn("hover:bg-red-500", "hover:bg-blue-500")).toBe("hover:bg-blue-500");
  });
});
