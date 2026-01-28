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
    // Validation
    if (selectedSuggestions.size === 0) {
      setError("Select at least one flashcard");
      return;
    }

    if (!sessionId) {
      setError("Session not initialized");
      return;
    }

    // Validate edited content
    for (const [suggestionId, content] of editedContent.entries()) {
      const frontValidation = validateFlashcardContent(content.front, "Question");
      const backValidation = validateFlashcardContent(content.back, "Answer");

      if (!frontValidation.isValid || !backValidation.isValid) {
        setError("All edited fields must be 1-1000 characters");
        return;
      }
    }

    try {
      setIsSaving(true);
      setError(null);

      // Build request
      const approvedSuggestions = Array.from(selectedSuggestions).map((suggestionId) => {
        const original = suggestions.find((s) => s.suggestionId === suggestionId);
        const edited = editedContent.get(suggestionId);

        return {
          suggestionId,
          frontContent: edited?.front || original?.frontContent || "",
          backContent: edited?.back || original?.backContent || "",
        };
      });

      const request: ApproveSessionRequest = {
        approvedSuggestions,
      };

      // POST approve
      await approveSession(sessionId, request);

      // Success
      setIsSaving(false);
      return true; // Indicate success
    } catch (err: any) {
      console.error("Error saving approved suggestions:", err);
      setIsSaving(false);

      if (err.response?.status === 400) {
        setError("Invalid request. Check your selections.");
      } else if (err.response?.status === 404) {
        setError("Session not found.");
      } else if (err.request && !err.response) {
        setError("No connection to server. Check your internet connection.");
      } else {
        setError("Error saving flashcards. Please try again.");
      }

      return false; // Indicate failure
    }
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
