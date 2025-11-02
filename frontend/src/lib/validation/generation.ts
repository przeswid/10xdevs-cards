import { z } from 'zod';

/**
 * Zod schema dla walidacji input text
 * Waliduje tekst wejściowy: 1000-10000 znaków
 */
export const inputTextSchema = z
  .string()
  .min(1000, { message: 'Minimum 1000 characters required' })
  .max(10000, { message: 'Maximum 10000 characters allowed' })
  .trim()
  .refine((text) => text.length > 0, { message: 'Text is required' });

/**
 * Zod schema dla walidacji edytowanej treści fiszki (front/back)
 * Waliduje pole: 1-1000 znaków
 */
export const flashcardContentSchema = z
  .string()
  .min(1, { message: 'Content must be at least 1 character' })
  .max(1000, { message: 'Content must be at most 1000 characters' });

/**
 * Walidacja input text
 * @param text - tekst do walidacji
 * @returns obiekt z isValid i optional error message
 */
export const validateInputText = (text: string): { isValid: boolean; error?: string } => {
  const result = inputTextSchema.safeParse(text);
  if (!result.success) {
    return {
      isValid: false,
      error: result.error.errors[0]?.message || 'Invalid input',
    };
  }
  return { isValid: true };
};

/**
 * Walidacja edytowanej treści fiszki
 * @param content - treść do walidacji
 * @param fieldName - nazwa pola (dla error message)
 * @returns obiekt z isValid i optional error message
 */
export const validateFlashcardContent = (
  content: string,
  fieldName: string = 'Content'
): { isValid: boolean; error?: string } => {
  const result = flashcardContentSchema.safeParse(content);
  if (!result.success) {
    return {
      isValid: false,
      error: result.error.errors[0]?.message?.replace('Content', fieldName) || 'Invalid content',
    };
  }
  return { isValid: true };
};
