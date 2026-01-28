import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import { FlashcardsList } from "./FlashcardsList";
import { getFlashcards } from "@/lib/api/flashcards";
import type { FlashcardSummary } from "@/lib/api/types";

vi.mock("@/lib/api/flashcards", () => ({
  getFlashcards: vi.fn(),
}));

describe("FlashcardsList", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("shows loading state initially", () => {
    vi.mocked(getFlashcards).mockImplementation(
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      () => new Promise(() => {}) // Never resolves
    );

    render(<FlashcardsList />);

    expect(screen.getByText("Loading flashcards...")).toBeInTheDocument();
  });

  it("displays empty state when no flashcards exist", async () => {
    vi.mocked(getFlashcards).mockResolvedValue({
      content: [],
      page: {
        number: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      },
    });

    render(<FlashcardsList />);

    await waitFor(() => {
      expect(screen.getByText("No flashcards yet")).toBeInTheDocument();
    });

    expect(screen.getByText("Generate some flashcards to see them here")).toBeInTheDocument();
  });

  it("displays error state when fetch fails", async () => {
    const errorMessage = "Failed to fetch flashcards";
    vi.mocked(getFlashcards).mockRejectedValue(new Error(errorMessage));

    render(<FlashcardsList />);

    await waitFor(() => {
      expect(screen.getByText("Error loading flashcards")).toBeInTheDocument();
    });

    expect(screen.getByText(errorMessage)).toBeInTheDocument();
  });

  it("displays flashcards when data is loaded", async () => {
    const mockFlashcards: FlashcardSummary[] = [
      {
        flashcardId: "id-1",
        frontContent: "Question 1",
        backContent: "Answer 1",
        source: "AI",
        createdAt: "2024-01-01T00:00:00Z",
        updatedAt: "2024-01-01T00:00:00Z",
      },
      {
        flashcardId: "id-2",
        frontContent: "Question 2",
        backContent: "Answer 2",
        source: "USER",
        createdAt: "2024-01-02T00:00:00Z",
        updatedAt: "2024-01-02T00:00:00Z",
      },
    ];

    vi.mocked(getFlashcards).mockResolvedValue({
      content: mockFlashcards,
      page: {
        number: 0,
        size: 20,
        totalElements: 2,
        totalPages: 1,
      },
    });

    render(<FlashcardsList />);

    await waitFor(() => {
      expect(screen.getByText("Question 1")).toBeInTheDocument();
    });

    expect(screen.getByText("Question 2")).toBeInTheDocument();
    expect(screen.getByText("Answer 1")).toBeInTheDocument();
    expect(screen.getByText("Answer 2")).toBeInTheDocument();
  });

  it("displays correct flashcard count", async () => {
    const mockFlashcards: FlashcardSummary[] = [
      {
        flashcardId: "id-1",
        frontContent: "Question 1",
        backContent: "Answer 1",
        source: "AI",
        createdAt: "2024-01-01T00:00:00Z",
        updatedAt: "2024-01-01T00:00:00Z",
      },
      {
        flashcardId: "id-2",
        frontContent: "Question 2",
        backContent: "Answer 2",
        source: "AI",
        createdAt: "2024-01-02T00:00:00Z",
        updatedAt: "2024-01-02T00:00:00Z",
      },
      {
        flashcardId: "id-3",
        frontContent: "Question 3",
        backContent: "Answer 3",
        source: "AI",
        createdAt: "2024-01-03T00:00:00Z",
        updatedAt: "2024-01-03T00:00:00Z",
      },
    ];

    vi.mocked(getFlashcards).mockResolvedValue({
      content: mockFlashcards,
      page: {
        number: 0,
        size: 20,
        totalElements: 3,
        totalPages: 1,
      },
    });

    render(<FlashcardsList />);

    await waitFor(() => {
      expect(screen.getByText("3")).toBeInTheDocument();
    });

    expect(screen.getByText(/flashcards/)).toBeInTheDocument();
  });

  it("uses singular form for single flashcard", async () => {
    const mockFlashcards: FlashcardSummary[] = [
      {
        flashcardId: "id-1",
        frontContent: "Question 1",
        backContent: "Answer 1",
        source: "AI",
        createdAt: "2024-01-01T00:00:00Z",
        updatedAt: "2024-01-01T00:00:00Z",
      },
    ];

    vi.mocked(getFlashcards).mockResolvedValue({
      content: mockFlashcards,
      page: {
        number: 0,
        size: 20,
        totalElements: 1,
        totalPages: 1,
      },
    });

    render(<FlashcardsList />);

    await waitFor(() => {
      expect(screen.getByText("1")).toBeInTheDocument();
    });

    expect(screen.getByText(/flashcard$/)).toBeInTheDocument();
  });

  it("calls getFlashcards API on mount", async () => {
    vi.mocked(getFlashcards).mockResolvedValue({
      content: [],
      page: {
        number: 0,
        size: 20,
        totalElements: 0,
        totalPages: 0,
      },
    });

    render(<FlashcardsList />);

    await waitFor(() => {
      expect(getFlashcards).toHaveBeenCalledTimes(1);
    });
  });

  it("renders flashcards with different sources", async () => {
    const mockFlashcards: FlashcardSummary[] = [
      {
        flashcardId: "id-1",
        frontContent: "AI Question",
        backContent: "AI Answer",
        source: "AI",
        createdAt: "2024-01-01T00:00:00Z",
        updatedAt: "2024-01-01T00:00:00Z",
      },
      {
        flashcardId: "id-2",
        frontContent: "Edited Question",
        backContent: "Edited Answer",
        source: "AI_USER",
        createdAt: "2024-01-02T00:00:00Z",
        updatedAt: "2024-01-02T00:00:00Z",
      },
      {
        flashcardId: "id-3",
        frontContent: "User Question",
        backContent: "User Answer",
        source: "USER",
        createdAt: "2024-01-03T00:00:00Z",
        updatedAt: "2024-01-03T00:00:00Z",
      },
    ];

    vi.mocked(getFlashcards).mockResolvedValue({
      content: mockFlashcards,
      page: {
        number: 0,
        size: 20,
        totalElements: 3,
        totalPages: 1,
      },
    });

    render(<FlashcardsList />);

    await waitFor(() => {
      expect(screen.getByText("AI Question")).toBeInTheDocument();
    });

    expect(screen.getByText("Edited Question")).toBeInTheDocument();
    expect(screen.getByText("User Question")).toBeInTheDocument();
  });
});
