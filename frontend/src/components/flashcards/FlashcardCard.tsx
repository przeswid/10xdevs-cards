import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import type { FlashcardSummary } from "@/lib/api/types";

interface FlashcardCardProps {
  flashcard: FlashcardSummary;
}

/**
 * Display card for a single flashcard
 * Read-only view showing front and back content
 */
export const FlashcardCard = ({ flashcard }: FlashcardCardProps) => {
  const sourceLabel = {
    AI: "AI Generated",
    AI_USER: "AI Generated (Edited)",
    USER: "User Created",
  }[flashcard.source];

  return (
    <Card>
      <CardHeader className="pb-4">
        <CardTitle className="text-base font-medium flex items-center justify-between">
          <span>Flashcard</span>
          <span className="text-xs text-muted-foreground font-normal">{sourceLabel}</span>
        </CardTitle>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Front field */}
        <div className="space-y-2">
          <div className="text-sm font-medium text-muted-foreground">Question (Front)</div>
          <p className="rounded-md border bg-muted/50 p-3 text-sm">{flashcard.frontContent}</p>
        </div>

        {/* Back field */}
        <div className="space-y-2">
          <div className="text-sm font-medium text-muted-foreground">Answer (Back)</div>
          <p className="rounded-md border bg-muted/50 p-3 text-sm">{flashcard.backContent}</p>
        </div>
      </CardContent>
    </Card>
  );
};
