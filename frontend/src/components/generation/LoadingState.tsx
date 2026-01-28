import { Loader2 } from "lucide-react";
import { Progress } from "@/components/ui/progress";
import type { LoadingStateProps } from "@/lib/api/types";

/**
 * Komponent stanu ładowania podczas pollingu sesji AI
 * Wyświetla spinner, progress bar i status text
 */
export const LoadingState = ({ status = "Generating flashcards..." }: LoadingStateProps) => {
  return (
    <div
      className="flex flex-col items-center justify-center space-y-6 py-12"
      role="status"
      aria-busy="true"
      aria-live="polite"
    >
      <div className="flex flex-col items-center space-y-4">
        <Loader2 className="h-12 w-12 animate-spin text-primary" aria-hidden="true" />
        <p className="text-lg font-medium text-foreground">{status}</p>
      </div>

      <div className="w-full max-w-md">
        <Progress value={undefined} className="h-2" aria-label="Loading progress" />
      </div>

      <p className="text-sm text-muted-foreground">This may take a moment...</p>
    </div>
  );
};
