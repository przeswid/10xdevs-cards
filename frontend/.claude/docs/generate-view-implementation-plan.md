# Plan implementacji widoku generowania AI

## 1. Przegląd

Widok generowania AI jest głównym punktem wejścia aplikacji po zalogowaniu (default landing page). Jego celem jest umożliwienie użytkownikowi wygenerowania fiszek za pomocą AI poprzez wklejenie tekstu źródłowego (1000-10000 znaków), przetworzenie go przez model LLM, oraz przegląd i zatwierdzenie wygenerowanych sugestii fiszek. Widok implementuje complete single-page workflow: formularz → loading → sugestie → save, bez przeładowania strony.

Główne funkcjonalności:
- Wklejenie i walidacja tekstu wejściowego (1000-10000 znaków)
- Inicjacja sesji AI i polling statusu
- Wyświetlenie sugestii fiszek z możliwością selekcji, inline edycji i zatwierdzenia
- Obsługa błędów API i stanów pustych

## 2. Routing widoku

**Ścieżka:** `/generate`

**Typ:** Strona publiczna (authentication będzie dodane w przyszłości)

**Implementacja routingu:**
```
src/pages/generate.astro
```

**Przekierowania:**
- Po zatwierdzeniu sugestii → redirect do `/flashcards`

## 3. Struktura komponentów

```
generate.astro (Astro page)
└─ GenerationProvider (React Context)
    ├─ GenerationForm (React)
    │   ├─ Textarea (Shadcn/ui)
    │   ├─ CharacterCounter (React)
    │   └─ Button "Generate" (Shadcn/ui)
    │
    └─ SuggestionsSection (React, conditional render)
        ├─ LoadingState (React)
        │   ├─ Spinner (Lucide icon)
        │   ├─ Progress (Shadcn/ui)
        │   └─ StatusText (React)
        │
        ├─ ErrorState (React)
        │   ├─ Alert (Shadcn/ui)
        │   └─ Button "Try again" (Shadcn/ui)
        │
        └─ SuggestionsList (React)
            ├─ SelectAllCheckbox (Shadcn/ui)
            ├─ SuggestionCard[] (React)
            │   ├─ Checkbox (Shadcn/ui)
            │   ├─ Card (Shadcn/ui)
            │   ├─ EditableField (React)
            │   │   ├─ Textarea (Shadcn/ui)
            │   │   └─ CharacterCounter (React)
            │   └─ ActionButtons (React)
            │       ├─ Button "Edit" (Shadcn/ui)
            │       └─ Button "Save" (Shadcn/ui)
            │
            └─ SaveButton "Save all" (Shadcn/ui)
```

## 4. Szczegóły komponentów

### GenerationProvider (React Context)

**Opis komponentu:**
Provider zarządzający całym stanem workflow generowania AI. Odpowiada za inicjację sesji, polling statusu, zarządzanie sugestiami, selekcją i edycją oraz zapisywanie zatwierdzonych fiszek. Enkapsuluje całą logikę biznesową i udostępnia czysty API dla komponentów dzieci.

**Główne elementy:**
- Context Provider wrapper (`<GenerationContext.Provider>`)
- Polling logic (useEffect z setInterval)
- API integration layer (createSession, getSession, getSuggestions, approveSession)

**Obsługiwane interakcje:**
- `generateFlashcards()` - inicjacja sesji AI i start pollingu
- `toggleSelection(suggestionId: string)` - zaznaczenie/odznaczenie sugestii
- `toggleAllSelections()` - zaznaczenie/odznaczenie wszystkich sugestii
- `editSuggestion(suggestionId: string, field: 'front' | 'back', value: string)` - edycja pola sugestii
- `saveApproved()` - zatwierdzenie i zapis wybranych sugestii

**Obsługiwana walidacja:**
- Input text: 1000-10000 znaków (przed wywołaniem API)
- Edited content: 1-1000 znaków per field (przed zapisem)
- Minimum jedna sugestia zaznaczona przed zapisem
- Sprawdzenie czy sessionId istnieje przed operacjami

**Typy:**
- `GenerationContextType` (interface context)
- `AISession` (DTO z API)
- `Suggestion` (DTO z API)
- `CreateSessionRequest` (API request)
- `ApproveSessionRequest` (API request)

**Propsy:**
- `children: ReactNode` - komponenty dzieci korzystające z context

### GenerationForm (React Component)

**Opis komponentu:**
Formularz zawierający textarea do wklejenia tekstu oraz przycisk generowania. Odpowiada za zbieranie input text od użytkownika, walidację długości tekstu i inicjację procesu generowania poprzez wywołanie `generateFlashcards()` z context.

**Główne elementy:**
- `<form>` element z `onSubmit` handler
- `<Textarea>` (Shadcn/ui) - pole tekstowe
- `<CharacterCounter>` - wyświetlanie licznika znaków
- `<Button>` "Generate" - submit button
- Komunikaty walidacji (inline error messages)

**Obsługiwane interakcje:**
- `onChange` textarea - aktualizacja `inputText` w context + walidacja długości
- `onSubmit` form - walidacja + wywołanie `generateFlashcards()`
- Disabled state podczas `isGenerating === true`

**Obsługiwana walidacja:**
- Minimum 1000 znaków (błąd: "Minimum 1000 characters required")
- Maximum 10000 znaków (błąd: "Maximum 10000 characters allowed")
- Nie pusty string (błąd: "Text is required")
- Real-time validation podczas pisania

**Typy:**
- `GenerationContextType` (z useContext)

**Propsy:**
Brak - pobiera dane z GenerationContext

### LoadingState (React Component)

**Opis komponentu:**
Komponent wyświetlany podczas pollingu statusu sesji AI. Pokazuje spinner, progress bar i tekstowy status informujący użytkownika o postępie generowania.

**Główne elementy:**
- Container `<div>` z flexbox centering
- `<Loader2>` icon (Lucide) z animacją spin
- `<Progress>` (Shadcn/ui) - indeterminate progress bar
- `<p>` - tekst statusu ("Generating flashcards ...", "Processing...")

**Obsługiwane interakcje:**
Brak - komponent czysto prezentacyjny

**Obsługiwana walidacja:**
Brak

**Typy:**
Brak - komponent statyczny

**Propsy:**
- `status?: string` - opcjonalny tekst statusu (default: "Generating flashcards ...")

### ErrorState (React Component)

**Opis komponentu:**
Komponent wyświetlany w przypadku błędu API lub niepowodzenia generowania. Pokazuje komunikat błędu i przycisk retry umożliwiający ponowną próbę generowania.

**Główne elementy:**
- `<Alert>` (Shadcn/ui) z wariantem destructive
- `<AlertTitle>` - tytuł błędu
- `<AlertDescription>` - szczegóły błędu
- `<Button>` "Try again" - retry action

**Obsługiwane interakcje:**
- Click "Try again" - reset error state + ponowne wywołanie `generateFlashcards()`

**Obsługiwana walidacja:**
Brak

**Typy:**
- `GenerationContextType` (z useContext)

**Propsy:**
- `error: string` - treść komunikatu błędu
- `onRetry: () => void` - callback retry

### SuggestionsList (React Component)

**Opis komponentu:**
Kontener listy sugestii fiszek wyświetlany po zakończeniu generowania. Zawiera checkbox "Zaznacz wszystkie", grid kart sugestii oraz przycisk "Zapisz wybrane".

**Główne elementy:**
- Container `<div>`
- Header section:
  - `<Checkbox>` "Check all"
  - `<span>` licznik wybranych sugestii
- `<div>` grid (2-3 kolumny desktop, 1 mobile)
- Array map → `<SuggestionCard>` components
- Footer section:
  - `<Button>` "Save selected" (disabled jeśli brak selekcji)

**Obsługiwane interakcje:**
- Click checkbox "Save all" - `toggleAllSelections()`
- Click "Save selected" - `saveApproved()` → global loading → redirect

**Obsługiwana walidacja:**
- Minimum jedna sugestia musi być zaznaczona przed zapisem
- Wszystkie edytowane pola muszą mieć 1-1000 znaków

**Typy:**
- `GenerationContextType` (z useContext)
- `Suggestion[]` (lista sugestii)

**Propsy:**
Brak - pobiera dane z GenerationContext

### SuggestionCard (React Component)

**Opis komponentu:**
Pojedyncza karta sugestii fiszki z możliwością zaznaczenia (checkbox), wyświetlenia treści (front/back) oraz inline edycji każdego pola. Komponent zarządza lokalnym stanem edycji i wywołuje callback do context przy zapisie zmian.

**Główne elementy:**
- `<Card>` (Shadcn/ui) container
- `<Checkbox>` - selekcja sugestii
- `<CardHeader>` - tytuł sugestii
- `<CardContent>`:
  - Front field section:
    - Display mode: `<p>` z treścią
    - Edit mode: `<Textarea>` + `<CharacterCounter>`
  - Back field section:
    - Display mode: `<p>` z treścią
    - Edit mode: `<Textarea>` + `<CharacterCounter>`
- `<CardFooter>`:
  - `<Button>` "Edit" (display mode)
  - `<Button>` "Save" + `<Button>` "Cancel" (edit mode)

**Obsługiwane interakcje:**
- Click checkbox - `toggleSelection(suggestionId)`
- Click "Edit" - `setIsEditing(true)` (lokalny state)
- onChange textarea - aktualizacja lokalnego state (frontEdit, backEdit)
- Click "Save" - walidacja → `editSuggestion()` dla obu pól → `setIsEditing(false)`
- Click "Cancel" - reset lokalnego state → `setIsEditing(false)`

**Obsługiwana walidacja:**
- Front content: 1-1000 znaków (wyświetlanie błędu inline)
- Back content: 1-1000 znaków (wyświetlanie błędu inline)
- Real-time character counter podczas edycji
- Disabled "Save" button jeśli walidacja nie przechodzi

**Typy:**
- `Suggestion` (props)
- `GenerationContextType` (z useContext)

**Propsy:**
- `suggestion: Suggestion` - obiekt sugestii z API
- `isSelected: boolean` - czy sugestia jest zaznaczona

### CharacterCounter (React Component)

**Opis komponentu:**
Prosty komponent utility wyświetlający licznik znaków w formacie "X/Y". Zmienia kolor na czerwony gdy przekroczono limit.

**Główne elementy:**
- `<span>` z dynamicznym className (error state gdy przekroczony limit)

**Obsługiwane interakcje:**
Brak

**Obsługiwana walidacja:**
Brak (tylko wyświetlanie)

**Propsy:**
- `current: number` - aktualna liczba znaków
- `max: number` - maksymalna liczba znaków
- `min?: number` - opcjonalna minimalna liczba znaków

## 5. Typy

### TypeScript Interfaces i Types

```typescript
// ============================================================================
// API DTO Types (zgodne z backend)
// ============================================================================

/**
 * Request body dla POST /ai/sessions
 */
interface CreateSessionRequest {
  inputText: string; // 1000-10000 chars, required
}

/**
 * Response z POST /ai/sessions
 */
interface CreateSessionResponse {
  sessionId: string; // UUID
  status: AISessionStatus;
  createdAt: string; // ISO 8601 timestamp
}

/**
 * Response z GET /ai/sessions/{sessionId}
 */
interface AISession {
  sessionId: string; // UUID
  status: AISessionStatus;
  generatedCount: number;
  acceptedCount: number;
  aiModel: string;
  apiCost: number;
  createdAt: string; // ISO 8601
}

/**
 * Response z GET /ai/sessions/{sessionId}/suggestions
 */
interface GetSuggestionsResponse {
  sessionId: string; // UUID
  status: AISessionStatus; // Should be COMPLETED
  suggestions: Suggestion[];
}

/**
 * Pojedyncza sugestia fiszki z API
 */
interface Suggestion {
  suggestionId: string; // UUID
  frontContent: string; // Max 1000 chars
  backContent: string; // Max 1000 chars
}

/**
 * Request body dla POST /ai/sessions/{sessionId}/approve
 */
interface ApproveSessionRequest {
  approvedSuggestions: ApprovedSuggestion[];
}

interface ApprovedSuggestion {
  suggestionId: string; // UUID
  frontContent: string; // Optional - if edited, max 1000 chars
  backContent: string; // Optional - if edited, max 1000 chars
}

/**
 * Status sesji AI
 */
type AISessionStatus = 'PENDING' | 'COMPLETED' | 'FAILED';

// ============================================================================
// Frontend ViewModel Types
// ============================================================================

/**
 * Stan edycji sugestii w frontencie
 * Przechowuje zmodyfikowaną treść przed zatwierdzeniem
 */
interface EditedContent {
  front: string;
  back: string;
}

/**
 * Mapa edytowanych treści
 * Key: suggestionId (UUID)
 * Value: EditedContent (zmodyfikowane front/back)
 */
type EditedContentMap = Map<string, EditedContent>;

/**
 * Set zaznaczonych sugestii
 * Przechowuje suggestionId (UUID) zaznaczonych kart
 */
type SelectedSuggestionsSet = Set<string>;

/**
 * Context type dla GenerationProvider
 */
interface GenerationContextType {
  // Input state
  inputText: string;
  setInputText: (text: string) => void;

  // Session state
  sessionId: string | null;
  sessionStatus: AISessionStatus | 'idle';

  // Suggestions state
  suggestions: Suggestion[];
  selectedSuggestions: SelectedSuggestionsSet;
  editedContent: EditedContentMap;

  // Actions
  generateFlashcards: () => Promise<void>;
  toggleSelection: (suggestionId: string) => void;
  toggleAllSelections: () => void;
  editSuggestion: (suggestionId: string, field: 'front' | 'back', value: string) => void;
  saveApproved: () => Promise<void>;

  // UI state
  isGenerating: boolean;
  isSaving: boolean;
  error: string | null;

  // Helpers
  isAllSelected: boolean;
  selectedCount: number;
}

/**
 * Validation error type
 */
interface ValidationError {
  field: string;
  message: string;
}

/**
 * API Error Response (z backend)
 */
interface APIErrorResponse {
  timestamp: string; // ISO 8601
  status: number;
  error: string;
  message: string;
  path: string;
}

// ============================================================================
// Component Props Types
// ============================================================================

interface SuggestionCardProps {
  suggestion: Suggestion;
  isSelected: boolean;
}

interface LoadingStateProps {
  status?: string;
}

interface ErrorStateProps {
  error: string;
  onRetry: () => void;
}

interface CharacterCounterProps {
  current: number;
  max: number;
  min?: number;
}
```

**Uwaga:** Authentication zostanie dodane w przyszłości. Obecna wersja zakłada publiczny dostęp do wszystkich endpointów.

## 6. Zarządzanie stanem

### 6.1 Global Context State (GenerationProvider)

Stan zarządzany przez React Context na poziomie całego workflow generowania:

**Input State:**
- `inputText: string` - tekst wejściowy z textarea (1000-10000 chars)

**Session State:**
- `sessionId: string | null` - UUID sesji AI (null przed generowaniem)
- `sessionStatus: AISessionStatus | 'idle'` - status sesji ('idle' | 'PENDING' | 'COMPLETED' | 'FAILED')

**Suggestions State:**
- `suggestions: Suggestion[]` - lista sugestii z API
- `selectedSuggestions: Set<string>` - set UUID zaznaczonych sugestii
- `editedContent: Map<string, EditedContent>` - mapa edytowanych treści (key: suggestionId)

**UI State:**
- `isGenerating: boolean` - true podczas pollingu
- `isSaving: boolean` - true podczas zapisywania zatwierdzonych sugestii
- `error: string | null` - komunikat błędu (null jeśli brak błędu)

**Computed State:**
- `isAllSelected: boolean` - czy wszystkie sugestie są zaznaczone
- `selectedCount: number` - liczba zaznaczonych sugestii

### 6.2 Local Component State

**SuggestionCard:**
- `isEditing: boolean` - czy karta jest w trybie edycji
- `frontEdit: string` - tymczasowa kopia front content podczas edycji
- `backEdit: string` - tymczasowa kopia back content podczas edycji

### 6.3 Custom Hook: useGenerationWorkflow

**Przeznaczenie:**
Enkapsulacja całej logiki workflow generowania AI w reusable hook.

**Funkcjonalność:**
- Inicjacja sesji AI (`createSession`)
- Polling statusu sesji (setInterval co 2.5s)
- Pobieranie sugestii po COMPLETED
- Zarządzanie selekcją i edycją
- Walidacja i zapis zatwierdzonych sugestii
- Error handling i cleanup

**API:**
Hook enkapsuluje całą logikę workflow:
- State management dla inputText, sessionId, sessionStatus, suggestions, selections, editedContent
- Actions: generateFlashcards(), toggleSelection(), toggleAllSelections(), editSuggestion(), saveApproved()
- Polling logic z timeout handling
- Error handling i cleanup

**Cleanup:**
- `useEffect` cleanup dla polling interval
- Clear error na unmount
- Cancel pending requests (axios CancelToken)

## 7. Integracja API

### 7.1 API Client Setup

**Lokalizacja:** `src/lib/api/ai.ts`

**Funkcje:**

```typescript
/**
 * Tworzy nową sesję generowania AI
 * @param inputText - tekst wejściowy (1000-10000 chars)
 * @returns CreateSessionResponse z sessionId i statusem
 * @throws APIError dla 400, 401, 422
 */
export const createSession = async (inputText: string): Promise<CreateSessionResponse> => {
  const response = await apiClient.post<CreateSessionResponse>('/ai/sessions', {
    inputText
  });
  return response.data;
};

/**
 * Pobiera status sesji AI
 * @param sessionId - UUID sesji
 * @returns AISession z aktualnym statusem
 * @throws APIError dla 401, 403, 404
 */
export const getSession = async (sessionId: string): Promise<AISession> => {
  const response = await apiClient.get<AISession>(`/ai/sessions/${sessionId}`);
  return response.data;
};

/**
 * Pobiera sugestie fiszek z zakończonej sesji
 * @param sessionId - UUID sesji
 * @returns GetSuggestionsResponse z listą sugestii
 * @throws APIError dla 401, 403, 404, 409 (session not completed)
 */
export const getSuggestions = async (sessionId: string): Promise<GetSuggestionsResponse> => {
  const response = await apiClient.get<GetSuggestionsResponse>(`/ai/sessions/${sessionId}/suggestions`);
  return response.data;
};

/**
 * Zatwierdza i zapisuje wybrane sugestie jako fiszki
 * @param sessionId - UUID sesji
 * @param request - ApproveSessionRequest z listą zatwierdzonych sugestii
 * @throws APIError dla 400, 401, 403, 404
 */
export const approveSession = async (
  sessionId: string,
  request: ApproveSessionRequest
): Promise<void> => {
  await apiClient.post(`/ai/sessions/${sessionId}/approve`, request);
};
```

### 7.2 Request/Response Flow

**Flow 1: Generowanie fiszek**

```
User action: Click "Generate"
↓
Frontend validation: inputText.length >= 1000 && <= 10000
↓
POST /ai/sessions
Request: { inputText: string }
Response: { sessionId: UUID, status: "PENDING", createdAt: ISO }
↓
Polling start (interval 2.5s)
↓
GET /ai/sessions/{sessionId}
Response: { sessionId, status: "PENDING"|"COMPLETED"|"FAILED", ... }
↓
If status === "COMPLETED":
  ↓
  GET /ai/sessions/{sessionId}/suggestions
  Response: { sessionId, status: "COMPLETED", suggestions: [...] }
  ↓
  Display suggestions
↓
If status === "FAILED":
  ↓
  Display error + retry button
```

**Flow 2: Zatwierdzanie sugestii**

```
User action: Click "Save selected"
↓
Frontend validation:
  - selectedSuggestions.size >= 1
  - All editedContent fields: 1-1000 chars
↓
Build ApproveSessionRequest:
  approvedSuggestions = selectedSuggestions.map(id => {
    suggestionId: id,
    frontContent: editedContent.get(id)?.front || original.frontContent,
    backContent: editedContent.get(id)?.back || original.backContent
  })
↓
POST /ai/sessions/{sessionId}/approve
Request: { approvedSuggestions: [...] }
Response: 201 Created (no body)
↓
Success toast: "Saved X flashcards"
↓
Redirect to /flashcards
```

### 7.3 Error Handling

**400 Bad Request (invalid input length):**
- Wyświetl error message w formularzu: "Text must be between 1000 and 10000 characters"
- Highlight textarea border czerwonym

**422 Unprocessable Entity (AI service unavailable):**
- Display ErrorState component
- Message: "AI service is currently unavailable. Please try again later."
- Show retry button

**409 Conflict (session not completed):**
- Continue polling (nie powinno się zdarzyć jeśli polling działa poprawnie)

**Network Error:**
- Display ErrorState component
- Message: "No connection to server. Check your internet connection."
- Show retry button

## 8. Interakcje użytkownika

### 8.1 Wklejanie tekstu

**Akcja:** User wkleja lub wpisuje tekst w textarea

**Handler:** `onChange` event textarea

**Zachowanie:**
1. Aktualizacja `inputText` w context
2. Real-time walidacja długości tekstu
3. Aktualizacja CharacterCounter (X/10000)
4. Wyświetlenie błędu jeśli < 1000 lub > 10000
5. Enable/disable "Generuj" button na podstawie walidacji

**Feedback:**
- Character counter zmienia kolor na czerwony jeśli przekroczony limit
- Inline error message pod textarea
- Button "Generate" disabled jeśli walidacja nie przechodzi

### 8.2 Generowanie fiszek

**Akcja:** User klika "Generate"

**Handler:** `onSubmit` form → `generateFlashcards()`

**Zachowanie:**
1. Walidacja inputText (już wykonana, double-check)
2. POST /ai/sessions → otrzymanie sessionId
3. Zmiana UI na LoadingState (spinner + progress bar)
4. Start polling co 2.5s → GET /ai/sessions/{sessionId}
5. Monitor statusu:
   - PENDING → kontynuuj polling
   - COMPLETED → GET suggestions → display SuggestionsList
   - FAILED → display ErrorState
6. Cleanup interval on unmount

**Feedback:**
- Button "Generate" zmienia się na disabled z spinner
- Form textarea disabled podczas generowania
- LoadingState z tekstem "Generating flashcards..."
- Progress bar (indeterminate)

### 8.3 Zaznaczanie sugestii

**Akcja 1:** User klika checkbox pojedynczej sugestii

**Handler:** `onClick` checkbox → `toggleSelection(suggestionId)`

**Zachowanie:**
1. Toggle suggestionId w selectedSuggestions Set
2. Update selectedCount
3. Update "Save selected" button state (disabled jeśli count === 0)
4. Update "Select all" checkbox (indeterminate jeśli partial selection)

**Akcja 2:** User klika "Select all"

**Handler:** `onClick` checkbox header → `toggleAllSelections()`

**Zachowanie:**
1. Jeśli wszystkie zaznaczone → clear selectedSuggestions Set
2. Jeśli nie wszystkie → add wszystkie suggestionId do Set
3. Update selectedCount i button states

**Feedback:**
- Checkbox visual state (checked/unchecked/indeterminate)
- Licznik "Selected X of Y"
- "Save selected" button enabled/disabled

### 8.4 Inline edycja sugestii

**Akcja 1:** User klika "Edit" na karcie

**Handler:** `onClick` button → `setIsEditing(true)` (local state)

**Zachowanie:**
1. Zamiana display mode na edit mode
2. `<p>` → `<Textarea>` dla front i back
3. Pre-fill textarea z current content (original lub edited)
4. Show character counters
5. Show "Save" i "Cancel" buttons

**Akcja 2:** User edytuje treść w textarea

**Handler:** `onChange` textarea → update local state (frontEdit, backEdit)

**Zachowanie:**
1. Aktualizacja lokalnego stanu
2. Real-time walidacja 1-1000 chars
3. Update character counter
4. Enable/disable "Save" na podstawie walidacji

**Akcja 3:** User klika "Save"

**Handler:** `onClick` button → walidacja → `editSuggestion()` → `setIsEditing(false)`

**Zachowanie:**
1. Walidacja: front 1-1000, back 1-1000
2. Wywołanie `editSuggestion(suggestionId, 'front', frontEdit)`
3. Wywołanie `editSuggestion(suggestionId, 'back', backEdit)`
4. Update editedContent Map w context
5. Powrót do display mode z zaktualizowaną treścią
6. Visual indicator edycji (np. badge "Edited")

**Akcja 4:** User klika "Cancel"

**Handler:** `onClick` button → reset local state → `setIsEditing(false)`

**Zachowanie:**
1. Discard local changes (frontEdit, backEdit)
2. Powrót do display mode
3. Wyświetlenie original lub previously saved edited content

**Feedback:**
- Mode transition (display ↔ edit)
- Character counters podczas edycji
- Validation errors inline
- Visual indicator po zapisaniu edycji

### 8.5 Zapisywanie zatwierdzonych sugestii

**Akcja:** User klika "Save selected"

**Handler:** `onClick` button → `saveApproved()`

**Zachowanie:**
1. Walidacja: selectedSuggestions.size >= 1 (już sprawdzone przez disabled state)
2. Walidacja: wszystkie edytowane pola 1-1000 chars
3. Build ApproveSessionRequest payload
4. Global loading overlay: "Saving flashcards..."
5. POST /ai/sessions/{sessionId}/approve
6. Success toast: "Saved X flashcards"
7. Redirect to /flashcards

**Feedback:**
- Global loading overlay z spinner
- Toast notification po sukcesie
- Redirect animation (View Transitions)

## 9. Warunki i walidacja

### 9.1 Input Text Validation (GenerationForm)

**Komponent:** GenerationForm

**Warunki:**
1. **Minimum 1000 znaków:**
   - Sprawdzenie: `inputText.length >= 1000`
   - Komunikat błędu: "Minimum 1000 characters required"
   - Wpływ na UI: Button "Generate" disabled, red border textarea

2. **Maximum 10000 znaków:**
   - Sprawdzenie: `inputText.length <= 10000`
   - Komunikat błędu: "Maximum 10000 characters allowed"
   - Wpływ na UI: Button "Generate" disabled, red border textarea, character counter czerwony

3. **Nie pusty:**
   - Sprawdzenie: `inputText.trim().length > 0`
   - Komunikat błędu: "Text is required"
   - Wpływ na UI: Button disabled

**Implementacja:**
- Real-time validation podczas `onChange`
- Wyświetlanie błędu inline pod textarea
- Disabled submit button jeśli nie przechodzi

### 9.2 Edited Content Validation (SuggestionCard)

**Komponent:** SuggestionCard (local state podczas edycji)

**Warunki:**
1. **Front content 1-1000 znaków:**
   - Sprawdzenie: `frontEdit.length >= 1 && frontEdit.length <= 1000`
   - Komunikat błędu: "Question must be 1-1000 characters"
   - Wpływ na UI: Button "Save" disabled, red character counter

2. **Back content 1-1000 znaków:**
   - Sprawdzenie: `backEdit.length >= 1 && backEdit.length <= 1000`
   - Komunikat błędu: "Answer must be 1-1000 characters"
   - Wpływ na UI: Button "Save" disabled, red character counter

**Implementacja:**
- Real-time validation podczas `onChange` textarea
- Character counter zmienia kolor
- Inline error message pod textarea
- Disabled "Save" button

### 9.3 Save Validation (przed POST approve)

**Komponent:** GenerationProvider (w `saveApproved()`)

**Warunki:**
1. **Minimum jedna sugestia zaznaczona:**
   - Sprawdzenie: `selectedSuggestions.size >= 1`
   - Komunikat błędu: "Select at least one flashcard"
   - Wpływ na UI: Button "Save selected" disabled (proactive validation)

2. **Wszystkie edytowane pola w zakresie 1-1000:**
   - Sprawdzenie: Dla każdego entry w `editedContent`:
     ```typescript
     editedContent.forEach((content, id) => {
       if (content.front.length < 1 || content.front.length > 1000) throw Error;
       if (content.back.length < 1 || content.back.length > 1000) throw Error;
     });
     ```
   - Komunikat błędu: "All edited fields must be 1-1000 characters"
   - Wpływ na UI: Toast error, zatrzymanie zapisu

**Implementacja:**
- Button "Save selected" disabled jeśli selectedCount === 0
- Pre-save validation przed POST request
- Toast error message jeśli validation fails
- No API call jeśli validation nie przechodzi

### 9.4 Session Status Validation

**Komponent:** GenerationProvider (polling logic)

**Warunki:**
1. **Session ID istnieje przed operacjami:**
   - Sprawdzenie: `sessionId !== null` przed GET suggestions lub approve
   - Komunikat błędu: "Session not initialized"
   - Wpływ na UI: Disable przyciski, pokazanie error state

2. **Status COMPLETED przed pobraniem sugestii:**
   - Sprawdzenie: `sessionStatus === 'COMPLETED'` przed GET suggestions
   - Komunikat błędu: "Session not yet completed" (409 z API)
   - Wpływ na UI: Kontynuacja pollingu, nie wyświetlanie sugestii

**Implementacja:**
- Guard clauses w API call functions
- Error handling dla 409 Conflict
- Polling continues until COMPLETED or FAILED

## 10. Obsługa błędów

### 10.1 API Errors

**400 Bad Request - Invalid Input Length**

**Scenariusz:** Input text poza zakresem 1000-10000

**Obsługa:**
- Catch w `generateFlashcards()`
- Parse error message z API response
- Wyświetl inline error w formularzu
- Highlight textarea border czerwonym
- Focus textarea dla poprawy UX

**422 Unprocessable Entity - AI Service Unavailable**

**Scenariusz:** Backend nie może połączyć się z OpenRouter API

**Obsługa:**
- Catch w `generateFlashcards()`
- Set error state
- Display ErrorState component
- Message: "AI service is currently unavailable. Please try again later."
- Przycisk "Try again" → retry generateFlashcards()

**409 Conflict - Session Not Completed**

**Scenariusz:** User próbuje pobrać sugestie przed zakończeniem generowania

**Obsługa:**
- Nie powinno się zdarzyć przy poprawnym pollingu
- Jeśli wystąpi: kontynuacja pollingu
- Log warning do console dla debugowania

**Network Error - No Connection**

**Scenariusz:** Brak połączenia internetowego

**Obsługa:**
- Catch network errors w API calls
- Display ErrorState component
- Message: "No connection to server. Check your internet connection."
- Przycisk "Try again"

### 10.2 Validation Errors

**Input Text Too Short**

**Scenariusz:** User klika "Generate" z < 1000 znaków

**Obsługa:**
- Frontend validation przed API call
- Inline error message: "Minimum 1000 characters required"
- Button disabled (proactive prevention)
- Character counter pokazuje "X/10000" czerwonym jeśli < 1000

**Input Text Too Long**

**Scenariusz:** User wkleja > 10000 znaków

**Obsługa:**
- Frontend validation real-time
- Opcja 1: Obcięcie tekstu do 10000 (autocorrect)
- Opcja 2: Error message + disabled button
- Preferowana: Opcja 2 dla transparency
- Message: "Maximum 10000 characters allowed (currently: X)"

**Edited Content Invalid**

**Scenariusz:** User próbuje zapisać edycję z < 1 lub > 1000 znaków

**Obsługa:**
- Disabled "Save" button w SuggestionCard
- Inline error message pod textarea
- Red character counter
- Prevent save action

**No Suggestions Selected**

**Scenariusz:** User klika "Save selected" bez zaznaczonych sugestii

**Obsługa:**
- Button "Save selected" disabled (proactive)
- Jeśli jakimś cudem dojdzie do kliknięcia: toast error "Select at least one flashcard"

### 10.3 Edge Cases

**Polling Timeout**

**Scenariusz:** Sesja AI trwa > 2 minuty (stuck PENDING)

**Obsługa:**
- Timeout po X polling attempts (np. 48 attempts × 2.5s = 2 min)
- Stop polling
- Display error: "Generation is taking too long. Please try again."
- Przycisk retry

**User Navigates Away During Polling**

**Scenariusz:** User opuszcza stronę podczas pollingu

**Obsługa:**
- useEffect cleanup function
- clearInterval(pollInterval)
- Cancel pending axios requests (CancelToken)

**Zero Suggestions Generated**

**Scenariusz:** AI generuje 0 sugestii (błąd modelu)

**Obsługa:**
- Check `suggestions.length === 0` po otrzymaniu response
- Display info message: "AI generated no suggestions. Try with different text."
- Przycisk powrotu do formularza

**Session Status FAILED**

**Scenariusz:** Backend/AI model zwraca status FAILED

**Obsługa:**
- Stop polling
- Display ErrorState
- Message: "Generation failed. Please try again."
- Przycisk retry → reset state + nowe generowanie

### 10.4 User Experience Errors

**Accidental Navigation During Edit**

**Scenariusz:** User ma niezapisane edycje i próbuje opuścić stronę

**Obsługa MVP:** Brak (zgodnie z decyzjami - no unsaved changes protection)

**Future:** beforeunload event listener z confirmation dialog

**Lost Session After Refresh**

**Scenariusz:** User refreshuje stronę podczas pollingu

**Obsługa MVP:** Session lost (brak persistence)

**Feedback:** Info message na starcie strony jeśli detectujemy poprzednią sessionId (localStorage)
- "Previous session was interrupted. Start a new one."

## 11. Kroki implementacji

### Krok 1: Setup struktury plików i typów

**Zadania:**
1. Utworzenie `src/pages/generate.astro` (Astro page)
2. Utworzenie `src/lib/api/types.ts` - definicje wszystkich TypeScript interfaces
3. Utworzenie `src/lib/api/ai.ts` - API client functions (createSession, getSession, getSuggestions, approveSession)
4. Utworzenie `src/lib/validation/generation.ts` - Zod schemas dla input validation

**Deliverables:**
- Wszystkie typy zdefiniowane i wyexportowane
- API functions szkielet (return types, JSDoc comments)
- Zod schema dla inputText (min 1000, max 10000)

### Krok 2: Implementacja GenerationProvider (Context)

**Zadania:**
1. Utworzenie `src/lib/context/GenerationContext.tsx`
2. Implementacja useGenerationWorkflow custom hook
3. State management setup (wszystkie state variables)
4. Implementacja actions:
   - `generateFlashcards()` - create session + polling logic
   - `toggleSelection()` - toggle suggestionId w Set
   - `toggleAllSelections()` - select/deselect all
   - `editSuggestion()` - update editedContent Map
   - `saveApproved()` - build request + POST approve + redirect
5. Error handling i cleanup (useEffect dla polling)

**Deliverables:**
- Działający GenerationContext z pełnym API
- Polling mechanism z timeout handling
- Cleanup logic (clearInterval on unmount)

**Testy manualne:**
- Console.log state changes
- Mockowanie API responses (MSW lub hardcoded data)
- Weryfikacja polling start/stop

### Krok 3: Implementacja GenerationForm

**Zadania:**
1. Utworzenie `src/components/generation/GenerationForm.tsx`
2. Implementacja textarea z Shadcn/ui
3. Integracja z GenerationContext (useContext)
4. Real-time validation (character count, error messages)
5. Implementacja CharacterCounter component
6. Submit handler → `generateFlashcards()`
7. Disabled states podczas generowania

**Deliverables:**
- Funkcjonalny formularz z walidacją
- Character counter (X/10000)
- Inline error messages
- Disabled state podczas isGenerating

**Testy manualne:**
- Wklejenie < 1000 znaków → error
- Wklejenie > 10000 znaków → error
- Wklejenie 1000-10000 → enabled button
- Submit → sprawdzenie czy wywołuje generateFlashcards()

### Krok 4: Implementacja LoadingState i ErrorState

**Zadania:**
1. Utworzenie `src/components/generation/LoadingState.tsx`
   - Spinner (Lucide Loader2 icon z animacją)
   - Progress bar (Shadcn/ui indeterminate)
   - Status text ("Generowanie fiszek...")
   - ARIA attributes (role="status", aria-busy)

2. Utworzenie `src/components/generation/ErrorState.tsx`
   - Alert component (Shadcn/ui destructive variant)
   - Error message display
   - Retry button → wywołanie onRetry callback

**Deliverables:**
- LoadingState z spinner + progress bar
- ErrorState z retry functionality

**Testy manualne:**
- Wyświetlenie LoadingState → sprawdzenie animacji
- Wyświetlenie ErrorState → kliknięcie retry

### Krok 5: Implementacja SuggestionCard

**Zadania:**
1. Utworzenie `src/components/generation/SuggestionCard.tsx`
2. Local state setup (isEditing, frontEdit, backEdit)
3. Display mode:
   - Checkbox (Shadcn/ui)
   - Front/back content wyświetlenie
   - "Edytuj" button
4. Edit mode:
   - Front textarea + character counter
   - Back textarea + character counter
   - "Zapisz" + "Anuluj" buttons
   - Validation logic
5. Integracja z GenerationContext (toggleSelection, editSuggestion)
6. Mode switching (display ↔ edit)

**Deliverables:**
- SuggestionCard z pełną funkcjonalnością edycji
- Character counters dla obu pól
- Validation i disabled states

**Testy manualne:**
- Zaznaczenie checkbox
- Kliknięcie "Edytuj" → mode switch
- Edycja treści → validation
- Zapisanie → sprawdzenie update w context
- Anulowanie → sprawdzenie reset

### Krok 6: Implementacja SuggestionsList

**Zadania:**
1. Utworzenie `src/components/generation/SuggestionsList.tsx`
2. Header section:
   - "Zaznacz wszystkie" checkbox
   - Selected count display ("Wybrano X z Y")
3. Grid layout (2-3 kolumny desktop, 1 mobile)
4. Map przez suggestions → render SuggestionCard
5. Footer section:
   - "Zapisz wybrane" button
   - Disabled state jeśli selectedCount === 0
6. Integration z GenerationContext

**Deliverables:**
- SuggestionsList z responsive grid
- "Zaznacz wszystkie" functionality
- "Zapisz wybrane" button z proper states

**Testy manualne:**
- Wyświetlenie listy sugestii
- "Zaznacz wszystkie" → sprawdzenie all checkboxes
- Odznaczenie pojedynczej → indeterminate state header checkbox
- "Zapisz wybrane" disabled gdy brak selekcji

### Krok 7: Implementacja conditional rendering w głównym widoku

**Zadania:**
1. Setup `src/pages/generate.astro`:
   - Wrap w GenerationProvider
2. Conditional rendering logika:
   - sessionStatus === 'idle' → GenerationForm only
   - sessionStatus === 'PENDING' → LoadingState
   - sessionStatus === 'FAILED' → ErrorState
   - sessionStatus === 'COMPLETED' → GenerationForm + SuggestionsList
3. Layout i styling (Tailwind)

**Deliverables:**
- Kompletna strona z conditional rendering
- Płynne transitions między stanami
- Responsive layout

**Testy manualne:**
- Initial load → tylko form
- Po submit → loading state
- Po COMPLETED → form + suggestions
- Po FAILED → error state

### Krok 8: Implementacja zapisywania i redirectu

**Zadania:**
1. Implementacja `saveApproved()` w GenerationProvider:
   - Build ApproveSessionRequest payload
   - Validation przed POST
   - POST /ai/sessions/{sessionId}/approve
   - Global loading overlay (Dialog z spinner)
   - Success toast (Shadcn/ui Toast/Sonner)
   - Redirect do /flashcards (Astro navigation)

2. Utworzenie global loading overlay component
3. Toast notifications setup

**Deliverables:**
- Działające zapisywanie sugestii
- Global loading overlay
- Success toast + redirect

**Testy manualne:**
- Zaznaczenie sugestii → zapisanie
- Sprawdzenie payload (Network tab)
- Sprawdzenie toast notification
- Sprawdzenie redirectu do /flashcards

### Krok 9: Error handling i edge cases

**Zadania:**
1. Dodanie error handling dla wszystkich API calls:
   - 400, 401, 422, 409, network errors
   - Proper error messages
   - Retry functionality
2. Polling timeout implementation (MAX_POLLING_ATTEMPTS)
3. Cleanup logic refinement (useEffect dependencies)
4. Empty state handling (0 suggestions)

**Deliverables:**
- Comprehensive error handling
- Graceful degradation
- User-friendly error messages

**Testy manualne:**
- Symulacja błędów API (Network tab throttling)
- Sprawdzenie timeout pollingu
- Sprawdzenie cleanup po unmount

### Krok 10: Styling i accessibility

**Zadania:**
1. Tailwind styling refinement:
   - Responsive design (mobile/tablet/desktop)
   - Spacing i alignment
   - Color scheme (error states, disabled states)
2. ARIA attributes dodanie:
   - role="status" dla loading
   - aria-label dla buttons
   - aria-live dla character counters
   - aria-busy dla containers
3. Keyboard navigation:
   - Tab order
   - Enter key submit
   - Escape key cancel edit
4. Focus management:
   - Focus textarea on validation error
   - Focus first suggestion po load

**Deliverables:**
- Fully responsive UI
- WCAG 2.1 AA compliant accessibility
- Smooth keyboard navigation

**Testy manualne:**
- Testowanie na różnych rozdzielczościach
- Screen reader testing (VoiceOver/NVDA)
- Keyboard-only navigation

### Krok 11: Integracja z backend i end-to-end testing

**Zadania:**
1. Podłączenie do rzeczywistego backend API
2. Environment variables setup (API URL)
3. End-to-end flow testing:
   - Generowanie fiszek z prawdziwym AI
   - Edycja i zatwierdzanie
   - Redirect i persistencja w /flashcards
4. Performance optimization:
   - Debouncing validation
   - Memoization (React.memo) jeśli potrzebne
5. Bug fixing na podstawie testów

**Deliverables:**
- Działający end-to-end workflow
- Zintegrowany z backend
- Performance optimized

**Testy manualne:**
- Complete user journey (generate → approve → flashcards)
- Edge cases testing
- Error scenarios testing

### Krok 12: Code review i dokumentacja

**Zadania:**
1. Code review:
   - TypeScript types consistency
   - Error handling completeness
   - Component reusability
   - Code comments i JSDoc
2. Dokumentacja:
   - README update (jeśli potrzebne)
   - Component usage examples
   - API integration notes
3. Final cleanup:
   - Remove console.logs
   - Remove dead code
   - Optimize imports

**Deliverables:**
- Clean, well-documented code
- Ready for production deployment

---

## Podsumowanie

Ten plan implementacji dostarcza szczegółowego przewodnika do stworzenia widoku generowania AI zgodnie z PRD, user stories i API specification. Implementacja powinna być wykonywana iteracyjnie, krok po kroku, z testowaniem każdego komponentu przed przejściem do następnego.

Kluczowe punkty do zapamiętania:
- Single-page workflow (form → loading → suggestions → save)
- Polling mechanism z timeout handling
- Inline validation z real-time feedback
- Comprehensive error handling
- Accessibility-first approach
- Clean separation of concerns (Context → Components → UI)

Szacowany czas implementacji: 3-5 dni roboczych dla doświadczonego frontend developera.
