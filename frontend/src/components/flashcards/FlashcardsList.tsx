import { useState, useEffect } from "react";
import { getFlashcards } from "@/lib/api/flashcards";
import type { FlashcardSummary } from "@/lib/api/types";
import { FlashcardCard } from "./FlashcardCard";
import { Loader2 } from "lucide-react";

/**
 * List of user's flashcards
 * Fetches and displays flashcards in a grid layout
 */
export const FlashcardsList = () => {
  const [flashcards, setFlashcards] = useState<FlashcardSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchFlashcards = async () => {
      try {
        setIsLoading(true);
        setError(null);
        const response = await getFlashcards();
        setFlashcards(response.content);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load flashcards");
      } finally {
        setIsLoading(false);
      }
    };

    fetchFlashcards();
  }, []);

  if (isLoading) {
    return (
      <div className="flex flex-col items-center justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
        <p className="mt-4 text-sm text-muted-foreground">Loading flashcards...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center">
        <p className="text-lg font-medium text-destructive">Error loading flashcards</p>
        <p className="text-sm text-muted-foreground mt-2">{error}</p>
      </div>
    );
  }

  if (flashcards.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center">
        <p className="text-lg font-medium text-muted-foreground">No flashcards yet</p>
        <p className="text-sm text-muted-foreground mt-2">Generate some flashcards to see them here</p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header with count */}
      <div className="flex items-center justify-between border-b pb-4">
        <div className="text-sm text-muted-foreground">
          Total: <span className="font-medium text-foreground">{flashcards.length}</span> flashcard
          {flashcards.length !== 1 ? "s" : ""}
        </div>
      </div>

      {/* Grid of flashcard cards */}
      <div className="grid gap-6 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
        {flashcards.map((flashcard) => (
          <FlashcardCard key={flashcard.flashcardId} flashcard={flashcard} />
        ))}
      </div>
    </div>
  );
};
