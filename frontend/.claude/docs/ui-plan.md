# Architektura UI dla 10x-cards

## 1. Przegląd struktury UI

Aplikacja 10x-cards jest flashcard systemem opartym na architekturze hybrid rendering: Astro 5 dla statycznych stron i React 19 dla interaktywnych komponentów ("wyspy"). Głównym założeniem MVP jest maksymalna prostota implementacji z koncentracją na podstawowej funkcjonalności: AI generation → review → study.

Plan jdst opisany po polsku, ale wszystkie labelki w aplikacji powinny być w języku angielskim.

### Kluczowe założenia architektoniczne:
- **File-based routing** (Astro) z View Transitions API dla płynnych przejść
- **JWT authentication** bez auto-refresh tokenu
- **Client-side state management** przez React Context (AuthContext, GenerationContext)
- **Responsive design** z mobile-first approach
- **Accessibility-first** z pełnym wsparciem ARIA i keyboard navigation
- **Security-by-design** z RLS na poziomie bazy danych i JWT tokenami

### Stack technologiczny:
- Astro 5 (static output mode)
- React 19 (functional components + hooks)
- TypeScript 5 (strict mode)
- Tailwind CSS 4 (utility-first)
- Shadcn/ui (New York style)
- React Hook Form + Zod (walidacja)
- Axios (HTTP client)

## 2. Lista widoków

### 2.1 Widok logowania
- **Ścieżka:** `/login`
- **Typ:** Publiczny (Astro page)
- **Główny cel:** Uwierzytelnienie użytkownika i przekierowanie do głównej aplikacji

**Kluczowe informacje do wyświetlenia:**
- Formularz logowania (username/email, hasło)
- Link do rejestracji
- Komunikaty błędów walidacji i autoryzacji

**Kluczowe komponenty widoku:**
- `LoginForm` (React) - formularz z React Hook Form + Zod
- `Input` (Shadcn/ui) - pola formularza
- `Button` (Shadcn/ui) - przycisk logowania
- `Alert` (Shadcn/ui) - komunikaty błędów

**UX, dostępność i względy bezpieczeństwa:**
- **UX:** Auto-focus na polu username, enter key submits form, loading state na przycisku podczas logowania
- **Dostępność:** Wszystkie pola z `<label>` i `for`, błędy z `role="alert"`, keyboard navigation
- **Bezpieczeństwo:** Hasło typu password (masked), brak auto-complete dla hasła, sanitizacja inputów przez Zod

### 2.2 Widok rejestracji
- **Ścieżka:** `/register`
- **Typ:** Publiczny (Astro page)
- **Główny cel:** Utworzenie nowego konta użytkownika z automatycznym logowaniem po sukcesie

**Kluczowe informacje do wyświetlenia:**
- Formularz rejestracji (username, email, hasło, opcjonalnie: imię, nazwisko)
- Wymagania dotyczące hasła (min 3 znaki)
- Link do logowania dla istniejących użytkowników
- Komunikaty błędów walidacji

**Kluczowe komponenty widoku:**
- `RegisterForm` (React) - formularz z React Hook Form + Zod
- `Input` (Shadcn/ui) - pola formularza
- `Button` (Shadcn/ui) - przycisk rejestracji
- `Alert` (Shadcn/ui) - komunikaty błędów i sukcesu
- Password strength indicator (opcjonalnie)

**UX, dostępność i względy bezpieczeństwa:**
- **UX:** Real-time walidacja pól, password strength indicator, success toast + auto-redirect do /generate
- **Dostępność:** `aria-required="true"` dla wymaganych pól, `aria-describedby` dla wymagań hasła, error messages z `aria-live="polite"`
- **Bezpieczeństwo:** Walidacja siły hasła (min 8 znaków + uppercase + lowercase + digit), sprawdzanie unikalności email/username (409 Conflict), HTTPS only

### 2.3 Widok generowania AI (default landing)
- **Ścieżka:** `/generate`
- **Typ:** Chroniony (Astro + React)
- **Główny cel:** Główny interfejs do generowania fiszek AI - wklejenie tekstu, generowanie, przegląd i zatwierdzanie sugestii

**Kluczowe informacje do wyświetlenia:**
- Formularz z textarea (1000-10000 znaków) + character counter
- Loading state z progress bar + spinner + status tekstowy podczas pollingu
- Lista sugestii AI (conditional render poniżej formularza)
- Checkboxy do selekcji sugestii
- Inline editable fields dla front/back content z character counters
- Przycisk "Zapisz wybrane" (disabled jeśli brak selekcji)

**Kluczowe komponenty widoku:**
- `GenerationProvider` (React Context) - zarządzanie stanem sesji i pollingu
- `GenerationForm` (React) - textarea + przycisk "Generuj"
- `LoadingState` (React) - spinner + progress bar + status
- `SuggestionsList` (React) - lista sugestii z conditional rendering
- `SuggestionCard` (React) - pojedyncza sugestia z checkbox, inline edit, character counters
- `Textarea` (Shadcn/ui)
- `Button` (Shadcn/ui)
- `Checkbox` (Shadcn/ui)
- `Progress` (Shadcn/ui)

**UX, dostępność i względy bezpieczeństwa:**
- **UX:**
  - Single-page workflow: formularz → loading → sugestie → save (bez przeładowania strony)
  - Polling co 2.5s do status=COMPLETED
  - Inline editing: kliknięcie edit zamienia tekst w editable textareas
  - Global loading overlay przy zapisywaniu + success toast + redirect do /flashcards
  - Empty state dla nowego użytkownika: "Wklej tekst i wygeneruj swoje pierwsze fiszki"
- **Dostępność:**
  - Textarea z `aria-label="Tekst do wygenerowania fiszek"`
  - Character counter z `aria-live="polite"` dla screen readers
  - Loading state z `role="status"` i `aria-busy="true"`
  - Checkboxy z proper labels
- **Bezpieczeństwo:**
  - Walidacja długości tekstu (1000-10000) przed wysłaniem
  - Sanitizacja inputu przez Zod
  - Token JWT w Authorization header
  - Error handling dla 401 (auto-logout) i 422 (AI service unavailable)

### 2.4 Widok listy fiszek
- **Ścieżka:** `/flashcards`
- **Typ:** Chroniony (Astro + React)
- **Główny cel:** Zarządzanie wszystkimi fiszkami użytkownika (AI + manualne) - przeglądanie, edycja, usuwanie, tworzenie

**Kluczowe informacje do wyświetlenia:**
- Responsive grid (2-3 kolumny desktop, 1 kolumna mobile) z fiszkami
- Każda karta: front content, back content, source badge (AI/AI_USER/USER), createdAt
- Akcje: Edit (hover desktop, zawsze widoczne mobile), Delete
- Przycisk "Dodaj fiszkę" (sticky/floating)
- Filtry: source dropdown (AI/AI_USER/USER)
- Sortowanie: createdAt/updatedAt (ASC/DESC)
- Numbered pagination (Previous/1/2/3.../Next)
- Empty states: nowy użytkownik, brak wyników filtra, wszystkie usunięte

**Kluczowe komponenty widoku:**
- `FlashcardGrid` (React) - responsive grid layout
- `FlashcardCard` (React) - karta z full content (bez truncate), source badge, action buttons
- `FlashcardFilters` (React) - source dropdown + sort dropdown
- `CreateFlashcardModal` (React) - modal z formularzem (front/back)
- `EditFlashcardModal` (React) - modal z pre-filled formularzem
- `DeleteConfirmDialog` (React) - AlertDialog z preview front content
- `EmptyState` (React) - różne warianty w zależności od scenariusza
- `Pagination` (Shadcn/ui)
- `Card` (Shadcn/ui)
- `Badge` (Shadcn/ui) - source tag
- `Dialog` (Shadcn/ui)
- `AlertDialog` (Shadcn/ui)

**UX, dostępność i względy bezpieczeństwa:**
- **UX:**
  - Hover na karcie (desktop): fade-in edit/delete icons
  - Mobile: małe ikony/dropdown zawsze widoczne
  - Click Edit → modal z pre-filled fields
  - Click Delete → AlertDialog "Czy na pewno usunąć fiszkę: [front content]?" + Anuluj/Usuń
  - Optimistic updates przy usuwaniu (rollback on error)
  - Loading skeleton cards podczas fetchowania
  - Source badge: subtelny tag (AI=niebieski, AI_USER=żółty, USER=zielony)
  - Empty state nowego użytkownika: "Nie masz jeszcze fiszek" + CTA "Wygeneruj pierwszą"
  - Empty state po filtrach: "Brak wyników" + "Wyczyść filtry"
- **Dostępność:**
  - Grid z proper semantic HTML
  - Action buttons z `aria-label="Edytuj fiszkę"` / `"Usuń fiszkę"`
  - Modal z `role="dialog"`, `aria-modal="true"`, `aria-labelledby`
  - Focus trap w modalach
  - Pagination buttons z `aria-current="page"` dla aktywnej strony
  - AlertDialog z focus na "Anuluj" (safe default)
- **Bezpieczeństwo:**
  - RLS zapewnia dostęp tylko do własnych fiszek
  - DELETE confirmation zapobiega przypadkowemu usunięciu
  - Walidacja edycji przez Zod (1-1000 znaków)

### 2.5 Widok sesji nauki
- **Ścieżka:** `/study`
- **Typ:** Chroniony (Astro + React)
- **Główny cel:** Interaktywna sesja nauki z pojedynczymi fiszkami, flip animation i oceną trudności

**Kluczowe informacje do wyświetlenia:**
- Progress indicator: "Fiszka X z Y" + progress bar
- Flashcard z 3D flip animation (front → back)
- Przycisk "Pokaż odpowiedź" (przed flip)
- Rating buttons po flip: Znowu (destructive) / Trudne (secondary) / Dobre (default) / Łatwe (success)
- Session summary po zakończeniu: "Ukończono X fiszek"
- Empty state: "Brak fiszek do nauki" + CTA → /generate

**Kluczowe komponenty widoku:**
- `StudyCard` (React) - flip card z CSS 3D transform
- `SessionSummary` (React) - podsumowanie sesji
- `ProgressIndicator` (React) - tekst + progress bar
- `EmptyState` (React) - brak fiszek
- `Button` (Shadcn/ui)
- `Progress` (Shadcn/ui)
- `Card` (Shadcn/ui)

**UX, dostępność i względy bezpieczeństwa:**
- **UX:**
  - Losowa kolejność fiszek (Fisher-Yates shuffle client-side)
  - Desktop: card centered (max-width: 600px)
  - Mobile: near full-screen (padding 1rem)
  - 3D flip animation (CSS transform, 0.6s transition)
  - Click "Pokaż odpowiedź" → flip → rating buttons appear
  - Click rating → reset flip + next card
  - Po ostatniej karcie → SessionSummary z "Ukończono" + opcja "Rozpocznij ponownie"
  - Brak keyboard shortcuts (MVP)
  - Brak spaced repetition (MVP - tylko losowa kolejność)
- **Dostępność:**
  - Przycisk flip z `aria-label="Pokaż odpowiedź"`
  - Po flip: `aria-live="polite"` announce back content
  - Rating buttons z semantic labels
  - Progress bar z `role="progressbar"`, `aria-valuenow`, `aria-valuemin`, `aria-valuemax`
- **Bezpieczeństwo:**
  - Fetch tylko własnych fiszek (RLS)
  - Brak persystencji sesji (MVP - refresh = restart)

### 2.6 Widok ustawień konta (opcjonalny dla MVP)
- **Ścieżka:** `/account`
- **Typ:** Chroniony (Astro + React)
- **Główny cel:** Zarządzanie profilem użytkownika i ustawieniami konta

**Kluczowe informacje do wyświetlenia:**
- Informacje o koncie: username, email, data rejestracji
- Opcja zmiany hasła
- Opcja usunięcia konta (GDPR compliance)
- Statystyki: liczba fiszek, liczba sesji AI

**Kluczowe komponenty widoku:**
- `AccountSettings` (React) - formularz ustawień
- `DeleteAccountDialog` (React) - confirmation dialog
- `Card` (Shadcn/ui)
- `Button` (Shadcn/ui)
- `AlertDialog` (Shadcn/ui)

**UX, dostępność i względy bezpieczeństwa:**
- **UX:** Destructive action (delete account) wymaga potwierdzenia + wpisania "USUŃ" lub username
- **Dostępność:** Clear warnings dla destructive actions
- **Bezpieczeństwo:** Re-authentication przed zmianą hasła lub usunięciem konta

## 3. Mapa podróży użytkownika

### 3.1 Flow 1: Nowy użytkownik → Pierwsze fiszki AI

```
Landing page → /register
    ↓
Rejestracja (username, email, password)
    ↓
Auto-login + success toast
    ↓
Redirect → /generate (default landing)
    ↓
Wklejenie tekstu (1000-10000 znaków)
    ↓
Click "Generuj" → POST /ai/sessions
    ↓
Loading state (polling co 2.5s → GET /ai/sessions/{sessionId})
    ↓
Status = COMPLETED → GET /ai/sessions/{sessionId}/suggestions
    ↓
Wyświetlenie listy sugestii (same page, section below form)
    ↓
Zaznaczenie checkboxów + inline editing selected cards
    ↓
Click "Zapisz wybrane" → POST /ai/sessions/{sessionId}/approve
    ↓
Global loading overlay → Success toast "Zapisano X fiszek"
    ↓
Redirect → /flashcards
    ↓
Widok saved flashcards w grid (source badge = AI lub AI_USER jeśli edytowane)
```

### 3.2 Flow 2: Powracający użytkownik → Sesja nauki

```
Landing page → /login
    ↓
Logowanie (username/email, password)
    ↓
Success → Redirect /generate
    ↓
Nawigacja → "Sesja Nauki" (top bar)
    ↓
/study → Fetch flashcards + shuffle (client-side)
    ↓
Wyświetlenie front pierwszej fiszki + progress "1/X"
    ↓
Click "Pokaż odpowiedź" → 3D flip animation
    ↓
Wyświetlenie back + rating buttons (Znowu/Trudne/Dobre/Łatwe)
    ↓
Click rating → next card (reset flip)
    ↓
Repeat until currentIndex = flashcards.length - 1
    ↓
SessionSummary: "Ukończono X fiszek" + "Rozpocznij ponownie"
```

### 3.3 Flow 3: Manualne tworzenie fiszki

```
/flashcards
    ↓
Click "Dodaj fiszkę" (floating button)
    ↓
Modal opens → CreateFlashcardModal
    ↓
Formularz: front (1-1000 chars), back (1-1000 chars)
    ↓
Character counters + real-time validation
    ↓
Click "Zapisz" → POST /flashcards
    ↓
Success → Modal closes + new card appears in grid (source = USER)
    ↓
Success toast "Fiszka dodana"
```

### 3.4 Flow 4: Edycja fiszki AI

```
/flashcards
    ↓
Hover card (desktop) → Edit icon fade-in
lub
Tap card (mobile) → Edit icon zawsze widoczny
    ↓
Click Edit → EditFlashcardModal opens
    ↓
Pre-filled form (front, back)
    ↓
Modyfikacja content + validation
    ↓
Click "Zapisz" → PUT /flashcards/{id}
    ↓
Success → source changes AI → AI_USER (jeśli było AI)
    ↓
Modal closes + updated card in grid + success toast
```

### 3.5 Flow 5: Usuwanie fiszki

```
/flashcards
    ↓
Hover/tap card → Delete icon appears
    ↓
Click Delete → DeleteConfirmDialog (AlertDialog)
    ↓
Preview: "Czy na pewno usunąć fiszkę: [front content preview]?"
    ↓
Buttons: "Anuluj" (default focus) / "Usuń" (destructive)
    ↓
Click "Usuń" → Optimistic update (remove from grid)
    ↓
DELETE /flashcards/{id}
    ↓
Success → success toast "Fiszka usunięta"
lub
Error → rollback (re-fetch) + error toast "Błąd podczas usuwania"
```

### 3.6 Flow 6: Token expiry

```
Dowolna strona chroniona
    ↓
AuthContext: useEffect monitoring expiresIn
    ↓
Date.now() > expiresIn → toast "Sesja wygasła"
    ↓
Logout: clear localStorage (accessToken, username)
    ↓
Redirect → /login
```

### 3.7 Flow 7: 401 Unauthorized (global interceptor)

```
Dowolny request z expired/invalid token
    ↓
Axios response interceptor catches 401
    ↓
localStorage.removeItem('accessToken', 'username')
    ↓
window.location.href = '/login'
```

## 4. Układ i struktura nawigacji

### 4.1 Top Navigation Bar (Desktop)

```
┌─────────────────────────────────────────────────────────────────┐
│ [Logo]  Generuj AI  |  Moje Fiszki  |  Sesja Nauki    [user ▼] │
└─────────────────────────────────────────────────────────────────┘
                                                           ├─ Konto
                                                           └─ Wyloguj
```

**Elementy:**
- Logo (link → /generate)
- Horizontal navigation links
  - "Generuj AI" → /generate
  - "Moje Fiszki" → /flashcards
  - "Sesja Nauki" → /study
- User menu dropdown (prawy róg)
  - Username display
  - "Konto" → /account
  - "Wyloguj" → logout + redirect /login

**Responsywność:**
- Desktop (≥640px): Horizontal layout jak powyżej
- Mobile (<640px): Hamburger menu lub bottom nav (do decyzji podczas implementacji)

### 4.2 Mobile Navigation (opcja 1: Hamburger)

```
┌─────────────────────────┐
│ ☰  [Logo]        [user] │
└─────────────────────────┘

Po kliknięciu ☰:
┌─────────────────────────┐
│ ✕  Menu                 │
├─────────────────────────┤
│ Generuj AI              │
│ Moje Fiszki             │
│ Sesja Nauki             │
│ Konto                   │
│ Wyloguj                 │
└─────────────────────────┘
```

### 4.3 Mobile Navigation (opcja 2: Bottom Nav)

```
┌─────────────────────────┐
│    [Logo]        [user] │
└─────────────────────────┘

        Content

┌─────────────────────────┐
│ [⚡AI] [📚Fiszki] [🎓Nauka] │
└─────────────────────────┘
```

### 4.4 Protected Routes Guard

**Implementacja:**
- Wszystkie widoki chronione sprawdzają `useAuth()` hook
- Jeśli `user === null` lub `token === null` → redirect /login
- Public routes (/login, /register) → redirect /generate jeśli already authenticated
- After login/register → redirect /generate (default landing)

**View Transitions:**
- Astro View Transitions API dla smooth page changes
- Fade animation między stronami
- Maintain scroll position dla back navigation (opcjonalnie)

### 4.5 Breadcrumbs (nie w MVP)

Dla przyszłych wersji można dodać breadcrumbs dla lepszej orientacji, ale w MVP prostota nawigacji eliminuje potrzebę.

## 5. Kluczowe komponenty

### 5.1 Komponenty Layout

#### Header.tsx
**Cel:** Top navigation bar z logo, menu links i user dropdown

**Props:**
- `currentPath: string` - dla active link highlighting
- `username: string` - wyświetlanie w user menu

**Funkcjonalność:**
- Responsive: horizontal desktop, hamburger/bottom mobile
- Active link styling
- User dropdown z logout action
- Accessibility: `nav` element, `aria-current="page"` dla active link

#### Footer.tsx
**Cel:** Optional footer z copyright i links (opcjonalny dla MVP)

**Props:** brak

#### ProtectedRoute.tsx
**Cel:** Wrapper component dla chronionych stron, sprawdza auth state

**Props:**
- `children: ReactNode`

**Funkcjonalność:**
- useAuth() hook check
- Redirect /login jeśli not authenticated
- Loading state podczas sprawdzania auth

### 5.2 Komponenty Auth

#### LoginForm.tsx
**Cel:** Formularz logowania z walidacją

**Wykorzystuje:**
- React Hook Form + Zod
- Shadcn/ui Form, Input, Button

**Funkcjonalność:**
- Real-time validation
- Submit → AuthContext.login()
- Error handling (400, 401)
- Loading state

#### RegisterForm.tsx
**Cel:** Formularz rejestracji z walidacją hasła

**Wykorzystuje:**
- React Hook Form + Zod
- Shadcn/ui Form, Input, Button, Alert

**Funkcjonalność:**
- Password strength validation
- Real-time validation
- Submit → AuthContext.register() → auto-login
- Error handling (400, 409)
- Success toast + redirect

### 5.3 Komponenty Generation

#### GenerationProvider.tsx
**Cel:** React Context dla zarządzania stanem AI generation workflow

**State:**
- `inputText: string`
- `sessionId: string | null`
- `sessionStatus: 'idle' | 'pending' | 'completed' | 'failed'`
- `suggestions: Suggestion[]`
- `selectedSuggestions: Set<string>`
- `editedContent: Map<string, { front: string; back: string }>`
- `isGenerating: boolean`
- `error: string | null`

**Actions:**
- `generateFlashcards()` - create session + polling
- `toggleSelection(id)` - checkbox toggle
- `editSuggestion(id, content)` - inline edit save
- `saveApproved()` - approve session + redirect

#### GenerationForm.tsx
**Cel:** Textarea dla input tekstu + przycisk generowania

**Wykorzystuje:**
- Shadcn/ui Textarea, Button
- GenerationContext

**Funkcjonalność:**
- Character counter (1000-10000)
- Validation przed submit
- Disabled podczas isGenerating

#### SuggestionsList.tsx
**Cel:** Lista sugestii AI z conditional rendering (loading/error/success states)

**Wykorzystuje:**
- GenerationContext
- LoadingState, SuggestionCard components
- Shadcn/ui Alert

**Funkcjonalność:**
- Conditional render based on sessionStatus
- Loop przez suggestions array
- "Zapisz wybrane" button (disabled if none selected)

#### SuggestionCard.tsx
**Cel:** Pojedyncza sugestia z checkbox, inline edit i character counters

**Props:**
- `suggestion: Suggestion`
- `isSelected: boolean`
- `onToggle: (id: string) => void`
- `onEdit: (id: string, content: { front: string; back: string }) => void`

**State:**
- `isEditing: boolean`
- `front: string`
- `back: string`

**Funkcjonalność:**
- Toggle checkbox
- Click Edit → editable textareas
- Character counters (1000 max each)
- Save → onEdit callback → setIsEditing(false)

#### LoadingState.tsx
**Cel:** Loading UI z spinner, progress bar i status text podczas pollingu

**Wykorzystuje:**
- Shadcn/ui Progress, Spinner (custom lub lucide-react icon)

**Funkcjonalność:**
- Progress bar (indeterminate lub z percent jeśli API dostarcza)
- Status text: "Generowanie fiszek..." / "Przetwarzanie..."
- Accessibility: `role="status"`, `aria-busy="true"`

### 5.4 Komponenty Flashcards

#### FlashcardGrid.tsx
**Cel:** Responsive grid layout dla listy fiszek

**Props:**
- `flashcards: Flashcard[]`
- `onEdit: (id: string) => void`
- `onDelete: (id: string) => void`

**Funkcjonalność:**
- Grid: 2-3 kolumny desktop, 1 mobile
- Loop przez flashcards → FlashcardCard
- Loading skeleton podczas fetching
- Empty state jeśli brak fiszek

#### FlashcardCard.tsx
**Cel:** Pojedyncza karta fiszki z full content, source badge i action buttons

**Props:**
- `flashcard: Flashcard`
- `onEdit: () => void`
- `onDelete: () => void`

**Funkcjonalność:**
- Display front, back (full content, no truncate)
- Source badge (AI/AI_USER/USER) z kolorami
- Hover (desktop): fade-in edit/delete icons
- Mobile: ikony zawsze widoczne
- Click Edit → callback
- Click Delete → callback

#### FlashcardFilters.tsx
**Cel:** Filtry i sortowanie dla listy fiszek

**Props:**
- `onFilterChange: (source: string | null) => void`
- `onSortChange: (sort: string) => void`

**Wykorzystuje:**
- Shadcn/ui Select (dropdown)

**Funkcjonalność:**
- Source filter: All / AI / AI_USER / USER
- Sort: createdAt desc/asc, updatedAt desc/asc

#### CreateFlashcardModal.tsx
**Cel:** Modal z formularzem do tworzenia nowej fiszki

**Props:**
- `isOpen: boolean`
- `onClose: () => void`
- `onCreate: (data: CreateFlashcardRequest) => void`

**Wykorzystuje:**
- React Hook Form + Zod
- Shadcn/ui Dialog, Form, Textarea, Button

**Funkcjonalność:**
- Form validation (1-1000 chars)
- Character counters
- Submit → onCreate callback
- Close on success lub Cancel

#### EditFlashcardModal.tsx
**Cel:** Modal z pre-filled formularzem do edycji fiszki

**Props:**
- `isOpen: boolean`
- `flashcard: Flashcard | null`
- `onClose: () => void`
- `onUpdate: (id: string, data: UpdateFlashcardRequest) => void`

**Wykorzystuje:**
- React Hook Form + Zod
- Shadcn/ui Dialog, Form, Textarea, Button

**Funkcjonalność:**
- Pre-fill z flashcard data
- Form validation
- Submit → onUpdate callback
- Source update visual indicator (AI → AI_USER)

#### DeleteConfirmDialog.tsx
**Cel:** AlertDialog z confirmation i preview content

**Props:**
- `isOpen: boolean`
- `flashcard: Flashcard | null`
- `onConfirm: () => void`
- `onCancel: () => void`

**Wykorzystuje:**
- Shadcn/ui AlertDialog

**Funkcjonalność:**
- Preview front content: "Czy na pewno usunąć fiszkę: [front]?"
- Buttons: Anuluj (default focus) / Usuń (destructive)
- Keyboard: Escape → Cancel, Enter → Confirm

#### EmptyState.tsx
**Cel:** Różne empty states w zależności od scenariusza

**Props:**
- `variant: 'new-user' | 'no-results' | 'no-flashcards'`

**Funkcjonalność:**
- new-user: "Nie masz jeszcze fiszek" + CTA "Wygeneruj pierwszą" → /generate
- no-results: "Brak wyników" + "Wyczyść filtry" button
- no-flashcards: "Brak fiszek do nauki" + CTA → /generate

### 5.5 Komponenty Study

#### StudyCard.tsx
**Cel:** Flip card z 3D animation, show answer button i rating buttons

**Props:**
- `flashcard: Flashcard`
- `onRate: (flashcardId: string, rating: number) => void`

**State:**
- `isFlipped: boolean`

**Funkcjonalność:**
- 3D flip animation (CSS transform)
- Front/back w Card components (absolute positioning)
- Przycisk "Pokaż odpowiedź" → setIsFlipped(true)
- Po flip: rating buttons (1-4) → onRate callback → setIsFlipped(false)
- Responsive: centered desktop, near full-screen mobile

#### SessionSummary.tsx
**Cel:** Podsumowanie sesji nauki po zakończeniu

**Props:**
- `totalCards: number`
- `onRestart: () => void`

**Funkcjonalność:**
- Display "Ukończono X fiszek"
- Button "Rozpocznij ponownie" → onRestart
- Opcjonalnie: stats (ile razy Znowu/Trudne/Dobre/Łatwe - jeśli trackowane)

#### ProgressIndicator.tsx
**Cel:** Wyświetlanie postępu sesji

**Props:**
- `current: number`
- `total: number`

**Wykorzystuje:**
- Shadcn/ui Progress

**Funkcjonalność:**
- Tekst: "Fiszka X z Y"
- Progress bar: (current / total) * 100
- Accessibility: `role="progressbar"`, `aria-valuenow`, `aria-valuemin="0"`, `aria-valuemax={total}`

### 5.6 Shared Components (Shadcn/ui)

Wykorzystywane gotowe komponenty z Shadcn/ui (New York style):

- **Button** - wszystkie przyciski z wariantami (default, destructive, secondary, ghost, success)
- **Card** - kontenery dla fiszek, form sections
- **Input** - text inputs w formularzach
- **Textarea** - multi-line inputs
- **Form** - wrapper dla React Hook Form
- **Dialog** - modals (Create/Edit flashcard)
- **AlertDialog** - confirmation dialogs (Delete)
- **Badge** - source tags (AI/AI_USER/USER)
- **Checkbox** - selection w suggestions
- **Select** - dropdowns dla filters/sort
- **Progress** - loading progress, study session progress
- **Toast/Sonner** - notifications (success/error messages)
- **Pagination** - numbered pagination dla /flashcards
- **Skeleton** - loading placeholders

### 5.7 API Client Layer

#### client.ts
**Cel:** Axios instance z interceptorami dla JWT i error handling

**Funkcjonalność:**
- baseURL z env variable
- Request interceptor: inject token z localStorage
- Response interceptor: handle 401 → logout + redirect /login
- Global error handling

#### types.ts
**Cel:** TypeScript interfaces dla API requests/responses

**Interfaces:**
- `AuthResponse`
- `User`
- `AISession`
- `Suggestion`
- `Flashcard`
- `PaginatedResponse<T>`
- Request types (RegisterRequest, LoginRequest, etc.)

#### auth.ts, ai.ts, flashcards.ts
**Cel:** API service functions grouped by resource

**Funkcje:**
- auth.ts: `register()`, `login()`
- ai.ts: `createSession()`, `getSession()`, `getSuggestions()`, `approveSession()`
- flashcards.ts: `getFlashcards()`, `createFlashcard()`, `updateFlashcard()`, `deleteFlashcard()`

### 5.8 Context Providers

#### AuthContext.tsx
**Cel:** Global auth state management

**State:**
- `user: { username: string } | null`
- `token: string | null`
- `isLoading: boolean`

**Methods:**
- `login(credentials)` - POST /auth/login → store token → redirect /generate
- `logout()` - clear localStorage → redirect /login
- `register(data)` - POST /auth/register → auto-login

**Funkcjonalność:**
- Auto-initialization: check localStorage on mount
- Token expiry monitoring: useEffect → toast + logout when expired
- Provide to entire app via AppLayout

#### GenerationContext.tsx
**Cel:** Page-level state dla /generate workflow

**State i Actions:** (opisane w sekcji GenerationProvider.tsx powyżej)

**Funkcjonalność:**
- Polling logic internal
- Clean API exposed to children components
- Provide tylko dla /generate page

### 5.9 Validation Schemas

#### auth.ts (validation)
**Cel:** Zod schemas dla auth forms

**Schemas:**
- `loginSchema` - username, password
- `registerSchema` - username, email, password (z regex dla siły hasła), firstName, lastName

#### flashcard.ts (validation)
**Cel:** Zod schemas dla flashcard forms

**Schemas:**
- `createFlashcardSchema` - frontContent (1-1000), backContent (1-1000)
- `updateFlashcardSchema` - identyczny jak create
- `generationInputSchema` - inputText (1000-10000)

### 5.10 Utility Functions

#### utils.ts
**Cel:** Helper functions

**Funkcje:**
- `cn(...inputs)` - clsx + tailwind-merge dla conditional class names
- `shuffleArray<T>(array: T[])` - Fisher-Yates shuffle dla study session
- `formatDate(date: string)` - formatowanie dat do display
- `truncate(text: string, maxLength: number)` - opcjonalnie, choć w MVP pokazujemy full content

---

## Podsumowanie

Architektura UI dla 10x-cards została zaprojektowana z naciskiem na:

1. **Prostotę implementacji** - MVP features only, brak advanced functionality
2. **User experience** - smooth workflows, clear feedback, responsive design
3. **Accessibility** - ARIA attributes, keyboard navigation, screen reader support
4. **Security** - JWT authentication, RLS, input validation, HTTPS only
5. **Scalability** - modular component structure, clean API layer, TypeScript types

Kluczowe user journeys (new user → AI flashcards, returning user → study session) są zoptymalizowane dla minimalnej liczby kliknięć i maksymalnej klarowności. Single-page workflow dla AI generation eliminuje niepotrzebne przeładowania, a responsive grid dla flashcards zapewnia spójne doświadczenie na wszystkich urządzeniach.

Architektura jest gotowa do rozszerzenia o zaawansowane features (spaced repetition algorithm, keyboard shortcuts, draft persistence) w przyszłych iteracjach, przy zachowaniu backward compatibility.
