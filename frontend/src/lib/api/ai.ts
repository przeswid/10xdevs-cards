import { apiClient } from "./client";
import { getToken } from "@/lib/services/tokenService";
import type {
  CreateSessionRequest,
  CreateSessionResponse,
  AISession,
  GetSuggestionsResponse,
  ApproveSessionRequest,
} from "./types";

/**
 * Tworzy nową sesję generowania AI
 * @param inputText - tekst wejściowy (1000-10000 chars)
 * @returns CreateSessionResponse z sessionId i statusem
 * @throws APIError dla 400, 401, 422
 */
export const createSession = async (inputText: string): Promise<CreateSessionResponse> => {
  const response = await apiClient.post<CreateSessionResponse>("/ai/sessions", {
    inputText,
  } as CreateSessionRequest);
  return response.data;
};

/**
 * Pobiera status sesji AI
 * @param sessionId - UUID sesji
 * @returns AISession z aktualnym statusem
 * @throws APIError dla 401, 403, 404
 */
export const getSession = async (sessionId: string): Promise<AISession> => {
  const response = await apiClient.get<AISession>(`/ai/sessions/${sessionId}`);
  return response.data;
};

/**
 * Pobiera sugestie fiszek z zakończonej sesji
 * @param sessionId - UUID sesji
 * @returns GetSuggestionsResponse z listą sugestii
 * @throws APIError dla 401, 403, 404, 409 (session not completed)
 */
export const getSuggestions = async (sessionId: string): Promise<GetSuggestionsResponse> => {
  const response = await apiClient.get<GetSuggestionsResponse>(`/ai/sessions/${sessionId}/suggestions`);
  return response.data;
};

/**
 * Zatwierdza i zapisuje wybrane sugestie jako fiszki
 * @param sessionId - UUID sesji
 * @param request - ApproveSessionRequest z listą zatwierdzonych sugestii
 * @throws APIError dla 400, 401, 403, 404
 */
export const approveSession = async (sessionId: string, request: ApproveSessionRequest): Promise<void> => {
  console.log(`[API] POST /ai/sessions/${sessionId}/approve - START`);
  console.log(`[API] Request payload:`, JSON.stringify(request, null, 2));

  const baseURL = apiClient.defaults.baseURL || "http://localhost:8080";
  const url = `${baseURL}/ai/sessions/${sessionId}/approve`;
  console.log(`[API] Full URL:`, url);

  // Get token using the service
  const token = getToken();
  console.log(`[API] Token exists:`, !!token);
  if (token) {
    console.log(`[API] Token preview:`, token.substring(0, 20) + '...');
  } else {
    console.error(`[API] NO TOKEN FOUND - Request will fail with 401`);
  }

  try {
    // Use fetch instead of axios to isolate the issue
    const fetchResponse = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      },
      body: JSON.stringify(request),
    });

    console.log(`[API] Fetch completed`);
    console.log(`[API] Response status:`, fetchResponse.status);
    console.log(`[API] Response ok:`, fetchResponse.ok);

    if (!fetchResponse.ok) {
      const errorText = await fetchResponse.text();
      console.error(`[API] Error response:`, errorText);
      throw new Error(`HTTP ${fetchResponse.status}: ${errorText}`);
    }

    // Try to parse response
    const responseText = await fetchResponse.text();
    console.log(`[API] Response text:`, responseText);

    if (responseText) {
      try {
        const responseData = JSON.parse(responseText);
        console.log(`[API] Response data:`, responseData);
      } catch (e) {
        console.log(`[API] Response is not JSON`);
      }
    }

    console.log(`[API] POST /ai/sessions/${sessionId}/approve - SUCCESS`);
  } catch (error: any) {
    console.error(`[API] POST /ai/sessions/${sessionId}/approve - FAILED`);
    console.error(`[API] Error:`, error);
    throw error;
  }
};
