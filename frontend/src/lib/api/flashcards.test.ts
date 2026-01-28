import { describe, it, expect, vi, beforeEach } from "vitest";
import { getFlashcards } from "./flashcards";
import { apiClient } from "./client";
import type { GetFlashcardsResponse, FlashcardSummary } from "./types";

vi.mock("./client", () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

describe("flashcards API", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("getFlashcards", () => {
    it("fetches flashcards with default parameters", async () => {
      const mockResponse: GetFlashcardsResponse = {
        content: [],
        page: {
          number: 0,
          size: 20,
          totalElements: 0,
          totalPages: 0,
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse });

      const result = await getFlashcards();

      expect(apiClient.get).toHaveBeenCalledWith("/flashcards", {
        params: {
          page: 0,
          size: 20,
          sort: "createdAt,desc",
        },
      });
      expect(result).toEqual(mockResponse);
    });

    it("fetches flashcards with custom page and size", async () => {
      const mockResponse: GetFlashcardsResponse = {
        content: [],
        page: {
          number: 2,
          size: 10,
          totalElements: 0,
          totalPages: 0,
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse });

      await getFlashcards({ page: 2, size: 10 });

      expect(apiClient.get).toHaveBeenCalledWith("/flashcards", {
        params: {
          page: 2,
          size: 10,
          sort: "createdAt,desc",
        },
      });
    });

    it("fetches flashcards with custom sort parameter", async () => {
      const mockResponse: GetFlashcardsResponse = {
        content: [],
        page: {
          number: 0,
          size: 20,
          totalElements: 0,
          totalPages: 0,
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse });

      await getFlashcards({ sort: "updatedAt,asc" });

      expect(apiClient.get).toHaveBeenCalledWith("/flashcards", {
        params: {
          page: 0,
          size: 20,
          sort: "updatedAt,asc",
        },
      });
    });

    it("fetches flashcards filtered by source", async () => {
      const mockResponse: GetFlashcardsResponse = {
        content: [],
        page: {
          number: 0,
          size: 20,
          totalElements: 0,
          totalPages: 0,
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse });

      await getFlashcards({ source: "AI" });

      expect(apiClient.get).toHaveBeenCalledWith("/flashcards", {
        params: {
          page: 0,
          size: 20,
          sort: "createdAt,desc",
          source: "AI",
        },
      });
    });

    it("returns flashcards content array when data exists", async () => {
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
          source: "AI_USER",
          createdAt: "2024-01-02T00:00:00Z",
          updatedAt: "2024-01-02T00:00:00Z",
        },
      ];

      const mockResponse: GetFlashcardsResponse = {
        content: mockFlashcards,
        page: {
          number: 0,
          size: 20,
          totalElements: 2,
          totalPages: 1,
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse });

      const result = await getFlashcards();

      expect(result.content).toHaveLength(2);
      expect(result.content[0].flashcardId).toBe("id-1");
      expect(result.content[1].flashcardId).toBe("id-2");
      expect(result.page.totalElements).toBe(2);
    });

    it("throws error when API request fails", async () => {
      const mockError = new Error("Network error");
      vi.mocked(apiClient.get).mockRejectedValue(mockError);

      await expect(getFlashcards()).rejects.toThrow("Network error");
    });

    it("fetches flashcards with all parameters combined", async () => {
      const mockResponse: GetFlashcardsResponse = {
        content: [],
        page: {
          number: 1,
          size: 50,
          totalElements: 0,
          totalPages: 0,
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse });

      await getFlashcards({
        page: 1,
        size: 50,
        sort: "frontContent,asc",
        source: "USER",
      });

      expect(apiClient.get).toHaveBeenCalledWith("/flashcards", {
        params: {
          page: 1,
          size: 50,
          sort: "frontContent,asc",
          source: "USER",
        },
      });
    });

    it("omits source parameter when not provided", async () => {
      const mockResponse: GetFlashcardsResponse = {
        content: [],
        page: {
          number: 0,
          size: 20,
          totalElements: 0,
          totalPages: 0,
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockResponse });

      await getFlashcards({ page: 0, size: 20 });

      expect(apiClient.get).toHaveBeenCalledWith("/flashcards", {
        params: {
          page: 0,
          size: 20,
          sort: "createdAt,desc",
        },
      });
    });
  });
});
