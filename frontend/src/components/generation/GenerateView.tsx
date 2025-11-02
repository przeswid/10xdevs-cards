import { useGeneration } from '@/lib/context/GenerationContext';
import { GenerationForm } from './GenerationForm';
import { LoadingState } from './LoadingState';
import { ErrorState } from './ErrorState';
import { SuggestionsList } from './SuggestionsList';

/**
 * Główny widok generowania fiszek AI
 * Conditional rendering na podstawie sessionStatus
 */
export const GenerateView = () => {
  const { sessionStatus, error, generateFlashcards } = useGeneration();

  /**
   * Handler dla retry po błędzie
   */
  const handleRetry = async () => {
    await generateFlashcards();
  };

  return (
    <div className="container mx-auto max-w-7xl px-4 py-8">
      <div className="mb-8 space-y-2">
        <h1 className="text-3xl font-bold tracking-tight">Generate Flashcards with AI</h1>
        <p className="text-muted-foreground">
          Paste your text and let AI generate flashcards for you
        </p>
      </div>

      <div className="space-y-8">
        {/* Form - zawsze widoczny w idle i completed */}
        {(sessionStatus === 'idle' || sessionStatus === 'COMPLETED') && (
          <div className="rounded-lg border bg-card p-6 shadow-sm">
            <GenerationForm />
          </div>
        )}

        {/* Loading state - podczas pollingu */}
        {sessionStatus === 'PENDING' && <LoadingState />}

        {/* Error state - gdy sesja failed lub inny błąd */}
        {(sessionStatus === 'FAILED' || (error && sessionStatus === 'idle')) && error && (
          <ErrorState error={error} onRetry={handleRetry} />
        )}

        {/* Suggestions list - po zakończeniu generowania */}
        {sessionStatus === 'COMPLETED' && (
          <div className="space-y-4">
            <div className="border-t pt-8">
              <h2 className="mb-6 text-2xl font-semibold">Generated Suggestions</h2>
              <SuggestionsList />
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
