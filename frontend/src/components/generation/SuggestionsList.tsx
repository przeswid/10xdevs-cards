import { toast } from "sonner";
import { useGeneration } from "@/lib/context/GenerationContext";
import { Checkbox } from "@/components/ui/checkbox";
import { Button } from "@/components/ui/button";
import { SuggestionCard } from "./SuggestionCard";

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
  } = useGeneration();

  if (suggestions.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center">
        <p className="text-lg font-medium text-muted-foreground">No suggestions generated</p>
        <p className="text-sm text-muted-foreground">Try with different text</p>
      </div>
    );
  }

  const handleSaveClick = async () => {
    const success = await saveApproved();

    if (success) {
      // Show success toast
      toast.success(`Saved ${selectedCount} flashcard${selectedCount > 1 ? "s" : ""} successfully!`);

      // Redirect to /flashcards after short delay
      setTimeout(() => {
        window.location.href = "/flashcards";
      }, 1500);
    }
  };

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
