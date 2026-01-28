// ============================================================================
// API DTO Types (zgodne z backend)
// ============================================================================

/**
 * Request body dla POST /ai/sessions
 */
export interface CreateSessionRequest {
  inputText: string; // 1000-10000 chars, required
}

/**
 * Response z POST /ai/sessions
 */
export interface CreateSessionResponse {
  sessionId: string; // UUID
  status: AISessionStatus;
  createdAt: string; // ISO 8601 timestamp
}

/**
 * Response z GET /ai/sessions/{sessionId}
 */
export interface AISession {
  sessionId: string; // UUID
  status: AISessionStatus;
  generatedCount: number;
  acceptedCount: number;
  aiModel: string;
  apiCost: number;
  createdAt: string; // ISO 8601
}

/**
 * Response z GET /ai/sessions/{sessionId}/suggestions
 */
export interface GetSuggestionsResponse {
  sessionId: string; // UUID
  status: AISessionStatus; // Should be COMPLETED
  suggestions: Suggestion[];
}

/**
 * Pojedyncza sugestia fiszki z API
 */
export interface Suggestion {
  suggestionId: string; // UUID
  frontContent: string; // Max 1000 chars
  backContent: string; // Max 1000 chars
}

/**
 * Request body dla POST /ai/sessions/{sessionId}/approve
 */
export interface ApproveSessionRequest {
  approvedSuggestions: ApprovedSuggestion[];
}

export interface ApprovedSuggestion {
  suggestionId: string; // UUID - always required
  frontContent?: string; // Optional - only include if edited, max 1000 chars
  backContent?: string; // Optional - only include if edited, max 1000 chars
}

/**
 * Status sesji AI
 */
export type AISessionStatus = "PENDING" | "COMPLETED" | "FAILED";

// ============================================================================
// Frontend ViewModel Types
// ============================================================================

/**
 * Stan edycji sugestii w frontencie
 * Przechowuje zmodyfikowaną treść przed zatwierdzeniem
 */
export interface EditedContent {
  front: string;
  back: string;
}

/**
 * Mapa edytowanych treści
 * Key: suggestionId (UUID)
 * Value: EditedContent (zmodyfikowane front/back)
 */
export type EditedContentMap = Map<string, EditedContent>;

/**
 * Set zaznaczonych sugestii
 * Przechowuje suggestionId (UUID) zaznaczonych kart
 */
export type SelectedSuggestionsSet = Set<string>;

/**
 * Context type dla GenerationProvider
 */
export interface GenerationContextType {
  // Input state
  inputText: string;
  setInputText: (text: string) => void;

  // Session state
  sessionId: string | null;
  sessionStatus: AISessionStatus | "idle";

  // Suggestions state
  suggestions: Suggestion[];
  selectedSuggestions: SelectedSuggestionsSet;
  editedContent: EditedContentMap;

  // Actions
  generateFlashcards: () => Promise<void>;
  toggleSelection: (suggestionId: string) => void;
  toggleAllSelections: () => void;
  editSuggestion: (suggestionId: string, field: "front" | "back", value: string) => void;
  saveApproved: () => Promise<boolean>; // Returns true on success

  // UI state
  isGenerating: boolean;
  isSaving: boolean;
  error: string | null;

  // Helpers
  isAllSelected: boolean;
  selectedCount: number;
}

/**
 * Validation error type
 */
export interface ValidationError {
  field: string;
  message: string;
}

/**
 * API Error Response (z backend)
 */
export interface APIErrorResponse {
  timestamp: string; // ISO 8601
  status: number;
  error: string;
  message: string;
  path: string;
}

// ============================================================================
// Component Props Types
// ============================================================================

export interface SuggestionCardProps {
  suggestion: Suggestion;
  isSelected: boolean;
}

export interface LoadingStateProps {
  status?: string;
}

export interface ErrorStateProps {
  error: string;
  onRetry: () => void;
}

export interface CharacterCounterProps {
  current: number;
  max: number;
  min?: number;
}

// ============================================================================
// Flashcard Types
// ============================================================================

/**
 * Flashcard source enum
 */
export type FlashcardSource = "AI" | "AI_USER" | "USER";

/**
 * Individual flashcard summary
 */
export interface FlashcardSummary {
  flashcardId: string; // UUID
  frontContent: string;
  backContent: string;
  source: FlashcardSource;
  createdAt: string; // ISO 8601
  updatedAt: string; // ISO 8601
}

/**
 * Pagination info
 */
export interface PageInfo {
  number: number; // Current page (0-based)
  size: number; // Items per page
  totalElements: number; // Total number of elements
  totalPages: number; // Total number of pages
}

/**
 * Response from GET /flashcards
 */
export interface GetFlashcardsResponse {
  content: FlashcardSummary[];
  page: PageInfo;
}

/**
 * Query parameters for GET /flashcards
 */
export interface GetFlashcardsParams {
  page?: number; // 0-based
  size?: number; // 1-100
  sort?: string; // e.g., "createdAt,desc"
  source?: FlashcardSource;
}

// ============================================================================
// Authentication Types
// ============================================================================

/**
 * Request body for POST /auth/register
 */
export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  firstName: string;
  lastName: string;
}

/**
 * Response from POST /auth/register
 */
export interface RegisterResponse {
  userId: string; // UUID of created user
}

/**
 * Request body for POST /auth/login
 */
export interface LoginRequest {
  username: string;
  password: string;
}

/**
 * Response from POST /auth/login
 */
export interface LoginResponse {
  username: string;
  accessToken: string;
  expiresIn: number; // seconds until expiration
}

/**
 * User profile data
 */
export interface User {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
}

/**
 * Validation result for individual field
 */
export interface ValidationResult {
  isValid: boolean;
  errors: string[];
}

/**
 * Form-level validation result
 */
export interface FormValidationResult {
  isValid: boolean;
  fieldErrors: Record<string, string[]>;
}

/**
 * Auth check result for server-side validation
 */
export interface AuthCheckResult {
  isAuthenticated: boolean;
  user?: User;
  error?: string;
}
