import { useState, type FormEvent } from "react";
import { useGeneration } from "@/lib/context/GenerationContext";
import { validateInputText } from "@/lib/validation/generation";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { CharacterCounter } from "./CharacterCounter";
import { cn } from "@/lib/utils";

const MIN_CHARS = 1000;
const MAX_CHARS = 10000;

/**
 * Formularz generowania fiszek AI
 * Zbiera tekst od użytkownika, waliduje i inicjuje proces generowania
 */
export const GenerationForm = () => {
  const { inputText, setInputText, generateFlashcards, isGenerating } = useGeneration();
  const [validationError, setValidationError] = useState<string | null>(null);

  /**
   * Obsługa zmiany tekstu w textarea
   */
  const handleTextChange = (value: string) => {
    setInputText(value);

    // Real-time validation
    const validation = validateInputText(value);
    if (!validation.isValid) {
      setValidationError(validation.error || null);
    } else {
      setValidationError(null);
    }
  };

  /**
   * Obsługa submitu formularza
   */
  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    // Final validation
    const validation = validateInputText(inputText);
    if (!validation.isValid) {
      setValidationError(validation.error || "Invalid input");
      return;
    }

    setValidationError(null);
    await generateFlashcards();
  };

  const currentLength = inputText.length;
  const isValid = currentLength >= MIN_CHARS && currentLength <= MAX_CHARS;

  return (
    <form onSubmit={handleSubmit} className="w-full space-y-4">
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <label htmlFor="input-text" className="text-sm font-medium leading-none">
            Paste your text to generate flashcards
          </label>
          <CharacterCounter current={currentLength} max={MAX_CHARS} min={MIN_CHARS} />
        </div>

        <Textarea
          id="input-text"
          value={inputText}
          onChange={(e) => handleTextChange(e.target.value)}
          placeholder="Paste text here (minimum 1000 characters, maximum 10000 characters)..."
          disabled={isGenerating}
          className={cn(
            "min-h-[300px] resize-y",
            validationError && "border-destructive focus-visible:ring-destructive"
          )}
          aria-invalid={!!validationError}
          aria-describedby={validationError ? "input-error" : undefined}
        />

        {validationError && (
          <p id="input-error" className="text-sm text-destructive" role="alert">
            {validationError}
          </p>
        )}
      </div>

      <Button type="submit" disabled={!isValid || isGenerating} className="w-full">
        {isGenerating ? "Generating..." : "Generate Flashcards"}
      </Button>
    </form>
  );
};
