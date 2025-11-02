import { apiClient } from './client';
import type {
  CreateSessionRequest,
  CreateSessionResponse,
  AISession,
  GetSuggestionsResponse,
  ApproveSessionRequest,
} from './types';

/**
 * Tworzy nową sesję generowania AI
 * @param inputText - tekst wejściowy (1000-10000 chars)
 * @returns CreateSessionResponse z sessionId i statusem
 * @throws APIError dla 400, 401, 422
 */
export const createSession = async (inputText: string): Promise<CreateSessionResponse> => {
  const response = await apiClient.post<CreateSessionResponse>('/ai/sessions', {
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
  const response = await apiClient.get<GetSuggestionsResponse>(
    `/ai/sessions/${sessionId}/suggestions`
  );
  return response.data;
};

/**
 * Zatwierdza i zapisuje wybrane sugestie jako fiszki
 * @param sessionId - UUID sesji
 * @param request - ApproveSessionRequest z listą zatwierdzonych sugestii
 * @throws APIError dla 400, 401, 403, 404
 */
export const approveSession = async (
  sessionId: string,
  request: ApproveSessionRequest
): Promise<void> => {
  await apiClient.post(`/ai/sessions/${sessionId}/approve`, request);
};
