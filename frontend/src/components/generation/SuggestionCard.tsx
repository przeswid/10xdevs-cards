import { useState } from 'react';
import { Pencil, Save, X } from 'lucide-react';
import { useGeneration } from '@/lib/context/GenerationContext';
import { validateFlashcardContent } from '@/lib/validation/generation';
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import { Button } from '@/components/ui/button';
import { Textarea } from '@/components/ui/textarea';
import { CharacterCounter } from './CharacterCounter';
import type { SuggestionCardProps } from '@/lib/api/types';
import { cn } from '@/lib/utils';

const MAX_CHARS = 1000;

/**
 * Karta pojedynczej sugestii fiszki
 * Obsługuje selekcję, wyświetlanie i inline edycję treści
 */
export const SuggestionCard = ({ suggestion, isSelected }: SuggestionCardProps) => {
  const { toggleSelection, editSuggestion, editedContent } = useGeneration();

  // Local state dla edycji
  const [isEditing, setIsEditing] = useState(false);
  const [frontEdit, setFrontEdit] = useState(suggestion.frontContent);
  const [backEdit, setBackEdit] = useState(suggestion.backContent);
  const [frontError, setFrontError] = useState<string | null>(null);
  const [backError, setBackError] = useState<string | null>(null);

  // Get edited content if exists
  const edited = editedContent.get(suggestion.suggestionId);
  const displayFront = edited?.front || suggestion.frontContent;
  const displayBack = edited?.back || suggestion.backContent;
  const isEdited = !!edited;

  /**
   * Enter edit mode
   */
  const handleEdit = () => {
    setFrontEdit(displayFront);
    setBackEdit(displayBack);
    setIsEditing(true);
    setFrontError(null);
    setBackError(null);
  };

  /**
   * Cancel edit
   */
  const handleCancel = () => {
    setFrontEdit(displayFront);
    setBackEdit(displayBack);
    setIsEditing(false);
    setFrontError(null);
    setBackError(null);
  };

  /**
   * Save edit
   */
  const handleSave = () => {
    // Validate both fields
    const frontValidation = validateFlashcardContent(frontEdit, 'Question');
    const backValidation = validateFlashcardContent(backEdit, 'Answer');

    if (!frontValidation.isValid) {
      setFrontError(frontValidation.error || 'Invalid question');
      return;
    }

    if (!backValidation.isValid) {
      setBackError(backValidation.error || 'Invalid answer');
      return;
    }

    // Save to context
    editSuggestion(suggestion.suggestionId, 'front', frontEdit);
    editSuggestion(suggestion.suggestionId, 'back', backEdit);

    // Exit edit mode
    setIsEditing(false);
    setFrontError(null);
    setBackError(null);
  };

  /**
   * Real-time validation for front field
   */
  const handleFrontChange = (value: string) => {
    setFrontEdit(value);
    const validation = validateFlashcardContent(value, 'Question');
    setFrontError(validation.isValid ? null : validation.error || null);
  };

  /**
   * Real-time validation for back field
   */
  const handleBackChange = (value: string) => {
    setBackEdit(value);
    const validation = validateFlashcardContent(value, 'Answer');
    setBackError(validation.isValid ? null : validation.error || null);
  };

  const canSave = !frontError && !backError && frontEdit.length > 0 && backEdit.length > 0;

  return (
    <Card className={cn('relative', isSelected && 'ring-2 ring-primary')}>
      <CardHeader className="flex flex-row items-start justify-between space-y-0 pb-4">
        <div className="flex items-center space-x-2">
          <Checkbox
            id={`select-${suggestion.suggestionId}`}
            checked={isSelected}
            onCheckedChange={() => toggleSelection(suggestion.suggestionId)}
            aria-label="Select this flashcard"
          />
          <CardTitle className="text-base font-medium">
            Flashcard {isEdited && <span className="ml-2 text-xs text-muted-foreground">(Edited)</span>}
          </CardTitle>
        </div>
      </CardHeader>

      <CardContent className="space-y-4">
        {/* Front field */}
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <label className="text-sm font-medium text-muted-foreground">Question (Front)</label>
            {isEditing && <CharacterCounter current={frontEdit.length} max={MAX_CHARS} min={1} />}
          </div>

          {isEditing ? (
            <>
              <Textarea
                value={frontEdit}
                onChange={(e) => handleFrontChange(e.target.value)}
                className={cn('min-h-[100px]', frontError && 'border-destructive')}
                aria-invalid={!!frontError}
                aria-describedby={frontError ? 'front-error' : undefined}
              />
              {frontError && (
                <p id="front-error" className="text-sm text-destructive" role="alert">
                  {frontError}
                </p>
              )}
            </>
          ) : (
            <p className="rounded-md border bg-muted/50 p-3 text-sm">{displayFront}</p>
          )}
        </div>

        {/* Back field */}
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <label className="text-sm font-medium text-muted-foreground">Answer (Back)</label>
            {isEditing && <CharacterCounter current={backEdit.length} max={MAX_CHARS} min={1} />}
          </div>

          {isEditing ? (
            <>
              <Textarea
                value={backEdit}
                onChange={(e) => handleBackChange(e.target.value)}
                className={cn('min-h-[100px]', backError && 'border-destructive')}
                aria-invalid={!!backError}
                aria-describedby={backError ? 'back-error' : undefined}
              />
              {backError && (
                <p id="back-error" className="text-sm text-destructive" role="alert">
                  {backError}
                </p>
              )}
            </>
          ) : (
            <p className="rounded-md border bg-muted/50 p-3 text-sm">{displayBack}</p>
          )}
        </div>
      </CardContent>

      <CardFooter className="flex justify-end space-x-2">
        {isEditing ? (
          <>
            <Button variant="outline" size="sm" onClick={handleCancel}>
              <X className="mr-1 h-4 w-4" />
              Cancel
            </Button>
            <Button size="sm" onClick={handleSave} disabled={!canSave}>
              <Save className="mr-1 h-4 w-4" />
              Save
            </Button>
          </>
        ) : (
          <Button variant="outline" size="sm" onClick={handleEdit}>
            <Pencil className="mr-1 h-4 w-4" />
            Edit
          </Button>
        )}
      </CardFooter>
    </Card>
  );
};
