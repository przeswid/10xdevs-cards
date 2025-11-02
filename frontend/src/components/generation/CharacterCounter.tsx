import { cn } from '@/lib/utils';
import type { CharacterCounterProps } from '@/lib/api/types';

/**
 * Komponent licznika znaków
 * Wyświetla licznik w formacie "X/Y" z czerwonym kolorem gdy przekroczony limit
 */
export const CharacterCounter = ({ current, max, min }: CharacterCounterProps) => {
  const isOverLimit = current > max;
  const isUnderMin = min !== undefined && current < min;
  const hasError = isOverLimit || isUnderMin;

  return (
    <span
      className={cn('text-sm font-medium', hasError ? 'text-destructive' : 'text-muted-foreground')}
      aria-live="polite"
      aria-atomic="true"
    >
      {current}/{max}
      {min !== undefined && current < min && ` (min: ${min})`}
    </span>
  );
};
