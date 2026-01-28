import React from "react";
import { toast } from "sonner";
import { useGeneration } from "@/lib/context/GenerationContext";
import { Checkbox } from "@/components/ui/checkbox";
import { Button } from "@/components/ui/button";
import { SuggestionCard } from "./SuggestionCard";
import { getToken } from "@/lib/services/tokenService";

/**
 * Lista sugestii fiszek z możliwością selekcji i zapisu
 * Wyświetla grid kart, checkbox "Zaznacz wszystkie" i przycisk "Zapisz wybrane"
 */
export const SuggestionsList = () => {
  const {
    suggestions,
    selectedSuggestions,
    toggleAllSelections,
    saveApproved,
    isAllSelected,
    selectedCount,
    isSaving,
    error,
  } = useGeneration();

  if (suggestions.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center">
        <p className="text-lg font-medium text-muted-foreground">No suggestions generated</p>
        <p className="text-sm text-muted-foreground">Try with different text</p>
      </div>
    );
  }

  const handleSaveClick = React.useCallback(
    async (e: React.MouseEvent<HTMLButtonElement>) => {
      // Prevent any default behavior
      e.preventDefault();
      e.stopPropagation();

      console.log("[SuggestionsList] ========== SAVE BUTTON CLICKED ==========");
      console.log("[SuggestionsList] Window location before:", window.location.href);
      console.log("[SuggestionsList] Document readyState:", document.readyState);

      // Check token before attempting save
      const token = getToken();
      console.log("[SuggestionsList] Token check - exists:", !!token);
      if (!token) {
        console.error("[SuggestionsList] NO AUTH TOKEN - User should be redirected to login");
        toast.error("Your session has expired. Please log in again.");
        return;
      }

      // Double-click protection
      if (isSaving) {
        console.log("[SuggestionsList] Already saving, ignoring click");
        return;
      }

      console.log("[SuggestionsList] Calling saveApproved()");

      // Keep a reference to prevent garbage collection
      const savePromise = saveApproved();
      console.log("[SuggestionsList] Promise created:", savePromise);

      try {
        const success = await savePromise;
        console.log("[SuggestionsList] saveApproved() completed, success:", success);
        console.log("[SuggestionsList] Window location after:", window.location.href);

        if (success) {
          // Show success toast
          toast.success(`Saved ${selectedCount} flashcard${selectedCount > 1 ? "s" : ""} successfully!`);

          // Redirect to /flashcards after showing toast
          console.log("[SuggestionsList] Starting redirect countdown...");
          await new Promise((resolve) => setTimeout(resolve, 1500));

          console.log("[SuggestionsList] Executing navigation to /flashcards");
          window.location.href = "/flashcards";
        } else {
          // Show error toast with specific error message if available
          console.log("[SuggestionsList] Save failed, error:", error);
          toast.error(error || "Failed to save flashcards. Please try again.");
        }
      } catch (err) {
        console.error("[SuggestionsList] Unexpected error in handleSaveClick:", err);
        toast.error("An unexpected error occurred. Please try again.");
      }
    },
    [saveApproved, selectedCount, error, isSaving]
  );

  return (
    <div className="space-y-6">
      {/* Header - Select all checkbox */}
      <div className="flex items-center justify-between border-b pb-4">
        <div className="flex items-center space-x-3">
          <Checkbox
            id="select-all"
            checked={isAllSelected}
            onCheckedChange={toggleAllSelections}
            aria-label="Select all flashcards"
          />
          <label htmlFor="select-all" className="text-sm font-medium cursor-pointer">
            Select all
          </label>
        </div>

        <div className="text-sm text-muted-foreground">
          Selected <span className="font-medium text-foreground">{selectedCount}</span> of{" "}
          <span className="font-medium text-foreground">{suggestions.length}</span>
        </div>
      </div>

      {/* Grid of suggestion cards */}
      <div className="grid gap-6 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
        {suggestions.map((suggestion) => (
          <SuggestionCard
            key={suggestion.suggestionId}
            suggestion={suggestion}
            isSelected={selectedSuggestions.has(suggestion.suggestionId)}
          />
        ))}
      </div>

      {/* Footer - Save button */}
      <div className="flex justify-center border-t pt-6">
        <Button
          type="button"
          size="lg"
          onClick={handleSaveClick}
          disabled={selectedCount === 0 || isSaving}
          className="min-w-[200px]"
        >
          {isSaving ? "Saving..." : `Save ${selectedCount} Selected`}
        </Button>
      </div>
    </div>
  );
};
