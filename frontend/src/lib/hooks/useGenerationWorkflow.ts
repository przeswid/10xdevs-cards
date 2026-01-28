import { useState, useEffect, useCallback, useRef } from "react";
import { createSession, getSession, getSuggestions, approveSession } from "@/lib/api/ai";
import type {
  AISessionStatus,
  Suggestion,
  EditedContentMap,
  SelectedSuggestionsSet,
  ApproveSessionRequest,
} from "@/lib/api/types";
import { validateInputText, validateFlashcardContent } from "@/lib/validation/generation";

const POLLING_INTERVAL = 2500; // 2.5 seconds
const MAX_POLLING_ATTEMPTS = 48; // 48 * 2.5s = 2 minutes

interface UseGenerationWorkflowReturn {
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
 * Custom hook zarządzający workflow generowania fiszek AI
 * Enkapsuluje całą logikę biznesową: sesje, polling, sugestie, selekcję, edycję
 */
export const useGenerationWorkflow = (): UseGenerationWorkflowReturn => {
  // Input state
  const [inputText, setInputText] = useState<string>("");

  // Session state
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [sessionStatus, setSessionStatus] = useState<AISessionStatus | "idle">("idle");

  // Suggestions state
  const [suggestions, setSuggestions] = useState<Suggestion[]>([]);
  const [selectedSuggestions, setSelectedSuggestions] = useState<SelectedSuggestionsSet>(new Set());
  const [editedContent, setEditedContent] = useState<EditedContentMap>(new Map());

  // UI state
  const [isGenerating, setIsGenerating] = useState<boolean>(false);
  const [isSaving, setIsSaving] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Polling refs
  const pollingIntervalRef = useRef<NodeJS.Timeout | null>(null);
  const pollingAttemptsRef = useRef<number>(0);

  /**
   * Cleanup polling interval
   */
  const cleanupPolling = useCallback(() => {
    if (pollingIntervalRef.current) {
      clearInterval(pollingIntervalRef.current);
      pollingIntervalRef.current = null;
    }
    pollingAttemptsRef.current = 0;
  }, []);

  /**
   * Polling logic - sprawdza status sesji co 2.5s
   */
  const startPolling = useCallback(
    (currentSessionId: string) => {
      cleanupPolling();

      const pollSessionStatus = async () => {
        try {
          pollingAttemptsRef.current += 1;

          // Timeout check
          if (pollingAttemptsRef.current > MAX_POLLING_ATTEMPTS) {
            cleanupPolling();
            setIsGenerating(false);
            setError("Generation is taking too long. Please try again.");
            setSessionStatus("idle");
            return;
          }

          const session = await getSession(currentSessionId);
          setSessionStatus(session.status);

          if (session.status === "COMPLETED") {
            // Pobierz sugestie
            const suggestionsResponse = await getSuggestions(currentSessionId);
            setSuggestions(suggestionsResponse.suggestions);
            setIsGenerating(false);
            cleanupPolling();

            // Check if no suggestions
            if (suggestionsResponse.suggestions.length === 0) {
              setError("AI generated no suggestions. Try with different text.");
            }
          } else if (session.status === "FAILED") {
            cleanupPolling();
            setIsGenerating(false);
            setError("Generation failed. Please try again.");
          }
          // If PENDING - continue polling (interval will call this function again)
        } catch (err) {
          console.error("Polling error:", err);
          cleanupPolling();
          setIsGenerating(false);
          setError("Error checking session status. Please try again.");
        }
      };

      // Initial poll
      pollSessionStatus();

      // Setup interval
      pollingIntervalRef.current = setInterval(pollSessionStatus, POLLING_INTERVAL);
    },
    [cleanupPolling]
  );

  /**
   * Generuje fiszki - tworzy sesję AI i rozpoczyna polling
   */
  const generateFlashcards = useCallback(async () => {
    // Validation
    const validation = validateInputText(inputText);
    if (!validation.isValid) {
      setError(validation.error || "Invalid input text");
      return;
    }

    try {
      setIsGenerating(true);
      setError(null);
      setSessionStatus("idle");
      setSuggestions([]);
      setSelectedSuggestions(new Set());
      setEditedContent(new Map());

      // Create session
      const response = await createSession(inputText);
      setSessionId(response.sessionId);
      setSessionStatus(response.status);

      // Start polling
      startPolling(response.sessionId);
    } catch (err: any) {
      console.error("Error creating session:", err);
      setIsGenerating(false);

      // Error handling
      if (err.response?.status === 400) {
        setError("Text must be between 1000 and 10000 characters");
      } else if (err.response?.status === 422) {
        setError("AI service is currently unavailable. Please try again later.");
      } else if (err.request && !err.response) {
        setError("No connection to server. Check your internet connection.");
      } else {
        setError("An error occurred. Please try again.");
      }
    }
  }, [inputText, startPolling]);

  /**
   * Toggle selekcji pojedynczej sugestii
   */
  const toggleSelection = useCallback((suggestionId: string) => {
    setSelectedSuggestions((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(suggestionId)) {
        newSet.delete(suggestionId);
      } else {
        newSet.add(suggestionId);
      }
      return newSet;
    });
  }, []);

  /**
   * Toggle selekcji wszystkich sugestii
   */
  const toggleAllSelections = useCallback(() => {
    setSelectedSuggestions((prev) => {
      // If all selected - deselect all
      if (prev.size === suggestions.length) {
        return new Set();
      }
      // Otherwise - select all
      return new Set(suggestions.map((s) => s.suggestionId));
    });
  }, [suggestions]);

  /**
   * Edycja pola sugestii
   */
  const editSuggestion = useCallback(
    (suggestionId: string, field: "front" | "back", value: string) => {
      setEditedContent((prev) => {
        const newMap = new Map(prev);
        const existing = newMap.get(suggestionId) || {
          front: suggestions.find((s) => s.suggestionId === suggestionId)?.frontContent || "",
          back: suggestions.find((s) => s.suggestionId === suggestionId)?.backContent || "",
        };

        newMap.set(suggestionId, {
          ...existing,
          [field]: value,
        });

        return newMap;
      });
    },
    [suggestions]
  );

  /**
   * Zapisuje zatwierdzone sugestie jako fiszki
   */
  const saveApproved = useCallback(async () => {
    console.log("[saveApproved] Starting save process");
    console.log("[saveApproved] sessionId:", sessionId);
    console.log("[saveApproved] selectedSuggestions:", selectedSuggestions.size);

    // Validation
    if (selectedSuggestions.size === 0) {
      console.log("[saveApproved] Validation failed: No suggestions selected");
      setError("Select at least one flashcard");
      return false;
    }

    if (!sessionId) {
      console.log("[saveApproved] Validation failed: No sessionId");
      setError("Session not initialized");
      return false;
    }

    // Validate edited content
    for (const [suggestionId, content] of editedContent.entries()) {
      const frontValidation = validateFlashcardContent(content.front, "Question");
      const backValidation = validateFlashcardContent(content.back, "Answer");

      if (!frontValidation.isValid || !backValidation.isValid) {
        setError("All edited fields must be 1-1000 characters");
        return false;
      }
    }

    // Set saving state first, before try block
    setIsSaving(true);
    setError(null);

    // Build request payload before try block to avoid any state-related issues
    const approvedSuggestions = Array.from(selectedSuggestions).map((suggestionId) => {
      const edited = editedContent.get(suggestionId);

      // Only include frontContent and backContent if the suggestion was actually edited
      // The backend expects these fields to be omitted for unedited suggestions
      if (edited) {
        return {
          suggestionId,
          frontContent: edited.front,
          backContent: edited.back,
        };
      } else {
        return {
          suggestionId,
        };
      }
    });

    const request: ApproveSessionRequest = {
      approvedSuggestions,
    };

    console.log("[saveApproved] Request payload:", JSON.stringify(request, null, 2));
    console.log("[saveApproved] Calling approveSession API...");

    try {
      // POST approve - use a local copy of sessionId to prevent race conditions
      const currentSessionId = sessionId;
      console.log("[saveApproved] About to call API with sessionId:", currentSessionId);

      // TEMPORARY: Direct inline fetch to bypass all abstractions
      const baseURL = "http://localhost:8080";
      const url = `${baseURL}/ai/sessions/${currentSessionId}/approve`;
      const token = typeof window !== 'undefined' ? localStorage.getItem('auth_token') : null;

      console.log("[saveApproved] Making DIRECT inline fetch to:", url);
      console.log("[saveApproved] Token exists:", !!token);
      console.log("[saveApproved] Request body:", JSON.stringify(request));

      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
        },
        body: JSON.stringify(request),
        keepalive: true, // Keep connection alive even if page navigation starts
      });

      console.log("[saveApproved] Fetch completed, status:", response.status);

      if (!response.ok) {
        const errorText = await response.text();
        console.error("[saveApproved] Error response:", errorText);
        throw new Error(`HTTP ${response.status}: ${errorText}`);
      }

      console.log("[saveApproved] API call successful!");
      // Don't call setIsSaving(false) here - we're navigating away anyway
      // and the state change could cause issues with response handling
      return true; // Indicate success
    } catch (err: any) {
      console.error("[saveApproved] Error saving approved suggestions:", err);
      console.error("[saveApproved] Error details:", {
        message: err.message,
        name: err.name,
        stack: err.stack,
      });

      setIsSaving(false);

      if (err.message?.includes('HTTP 400')) {
        setError("Invalid request. Check your selections.");
      } else if (err.message?.includes('HTTP 404')) {
        setError("Session not found.");
      } else if (err.name === 'TypeError' || err.message?.includes('fetch')) {
        setError("No connection to server. Check your internet connection.");
      } else {
        setError("Error saving flashcards. Please try again.");
      }

      return false; // Indicate failure
    }
    // Note: No finally block - don't reset isSaving on success since we navigate away
  }, [sessionId, selectedSuggestions, editedContent, suggestions]);

  /**
   * Computed helpers
   */
  const isAllSelected = suggestions.length > 0 && selectedSuggestions.size === suggestions.length;
  const selectedCount = selectedSuggestions.size;

  /**
   * Cleanup on unmount
   */
  useEffect(() => {
    return () => {
      cleanupPolling();
    };
  }, [cleanupPolling]);

  return {
    // Input state
    inputText,
    setInputText,

    // Session state
    sessionId,
    sessionStatus,

    // Suggestions state
    suggestions,
    selectedSuggestions,
    editedContent,

    // Actions
    generateFlashcards,
    toggleSelection,
    toggleAllSelections,
    editSuggestion,
    saveApproved,

    // UI state
    isGenerating,
    isSaving,
    error,

    // Helpers
    isAllSelected,
    selectedCount,
  };
};
