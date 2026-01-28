import { apiClient } from "./client";
import type { GetFlashcardsResponse, GetFlashcardsParams } from "./types";

/**
 * Fetch user's flashcards with optional filtering and pagination
 * GET /flashcards
 */
export const getFlashcards = async (params?: GetFlashcardsParams): Promise<GetFlashcardsResponse> => {
  const response = await apiClient.get<GetFlashcardsResponse>("/flashcards", {
    params: {
      page: params?.page ?? 0,
      size: params?.size ?? 20,
      sort: params?.sort ?? "createdAt,desc",
      ...(params?.source && { source: params.source }),
    },
  });

  return response.data;
};
