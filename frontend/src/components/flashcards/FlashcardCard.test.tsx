import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { FlashcardCard } from "./FlashcardCard";
import type { FlashcardSummary } from "@/lib/api/types";

describe("FlashcardCard", () => {
  const mockFlashcard: FlashcardSummary = {
    flashcardId: "test-id-123",
    frontContent: "What is TypeScript?",
    backContent: "A typed superset of JavaScript",
    source: "AI",
    createdAt: "2024-01-01T00:00:00Z",
    updatedAt: "2024-01-01T00:00:00Z",
  };

  it("renders flashcard front content", () => {
    render(<FlashcardCard flashcard={mockFlashcard} />);

    expect(screen.getByText("What is TypeScript?")).toBeInTheDocument();
  });

  it("renders flashcard back content", () => {
    render(<FlashcardCard flashcard={mockFlashcard} />);

    expect(screen.getByText("A typed superset of JavaScript")).toBeInTheDocument();
  });

  it("renders Question and Answer labels", () => {
    render(<FlashcardCard flashcard={mockFlashcard} />);

    expect(screen.getByText("Question (Front)")).toBeInTheDocument();
    expect(screen.getByText("Answer (Back)")).toBeInTheDocument();
  });

  it("displays AI Generated label for AI source", () => {
    render(<FlashcardCard flashcard={mockFlashcard} />);

    expect(screen.getByText("AI Generated")).toBeInTheDocument();
  });

  it("displays AI Generated (Edited) label for AI_USER source", () => {
    const editedFlashcard: FlashcardSummary = {
      ...mockFlashcard,
      source: "AI_USER",
    };

    render(<FlashcardCard flashcard={editedFlashcard} />);

    expect(screen.getByText("AI Generated (Edited)")).toBeInTheDocument();
  });

  it("displays User Created label for USER source", () => {
    const userFlashcard: FlashcardSummary = {
      ...mockFlashcard,
      source: "USER",
    };

    render(<FlashcardCard flashcard={userFlashcard} />);

    expect(screen.getByText("User Created")).toBeInTheDocument();
  });

  it("renders Flashcard title", () => {
    render(<FlashcardCard flashcard={mockFlashcard} />);

    expect(screen.getByText("Flashcard")).toBeInTheDocument();
  });
});
