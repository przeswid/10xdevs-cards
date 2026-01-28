Jesteś wykwalifikowanym architektem frontend, którego zadaniem jest stworzenie kompleksowej architektury interfejsu użytkownika w oparciu o dokument wymagań produktu (PRD), plan API i notatki z sesji planowania. Twoim celem jest zaprojektowanie struktury interfejsu użytkownika, która skutecznie spełnia wymagania produktu, jest zgodna z możliwościami API i zawiera spostrzeżenia z sesji planowania.

Najpierw dokładnie przejrzyj następujące dokumenty:

Dokument wymagań produktu (PRD):
<prd>
.claude/docs/prd.md
</prd>

Plan API:
<api_plan>
.claude/docs/api-plan.md
</api_plan>

Session Notes:
<session_notes>
<conversation_summary> <decisions> Struktura i Nawigacja Aplikacji:
Strona startowa po zalogowaniu: strona generowania AI (/generate)
Lista sugestii AI wyświetlana na tej samej stronie co formularz (sekcja poniżej)
Nawigacja: Top bar z Shadcn/ui
Widok fiszek: Cards w responsive grid (zgodnie z rekomendacją)
Generowanie AI i Workflow: 5. Mechanizm pollingu: Progress bar + spinner z tekstem statusu (zgodnie z rekomendacją) 6. Zapisywanie zaaprobowanych sugestii: Globalny loading state (najprostsza opcja) 7. Brak persystencji drafts w localStorage (MVP) 8. Brak rate limiting w MVP Edycja i Zarządzanie Fiszkami: 9. Edycja sugestii AI: Inline (bezpośrednio w liście) 10. Wyświetlanie źródła fiszki: Tak, jako badge/tag (zgodnie z rekomendacją) 11. Akcje na kartach (Edit/Delete): Hover na desktop, zawsze widoczne na mobile (zgodnie z rekomendacją) 12. Paginacja: Classic numbered pagination (zgodnie z rekomendacją) 13. Potwierdzenie usunięcia: Modal dialog (zgodnie z rekomendacją) 14. Wyświetlanie zawartości kart: Całość (bez truncate) 15. Brak obsługi "unsaved changes" (MVP - najprostsza opcja) Sesja Nauki: 16. Mechanizm flip: Dedykowany przycisk "Pokaż odpowiedź" (zgodnie z rekomendacją) 17. Kolejność fiszek: Losowa (bez spaced repetition w MVP) 18. Animacja flip: Prosty 3D flip z CSS transform (zgodnie z rekomendacją) 19. Brak keyboard shortcuts (MVP) 20. Responsywność: Card centered na desktop, near full-screen na mobile (zgodnie z rekomendacją) Architektura Techniczna: 21. Formularze: Shadcn/ui z React Hook Form + Zod (zgodnie z rekomendacją) 22. Zarządzanie stanem: React Context dla GenerationContext i AuthContext (zgodnie z rekomendacją) 23. AuthContext: State + methods (login/logout/register) z auto-initialization (zgodnie z rekomendacją) 24. Token JWT: Najprostsza metoda wstrzykiwania do requestów (axios interceptor) 25. Refresh tokenu: Wymaga ponownego logowania (MVP) 26. Struktura API services: Grupowanie po resource (auth.ts, ai.ts, flashcards.ts) (zgodnie z rekomendacją) 27. Error handling: Hybrydowe - validation lokalne, auth/server globalne (zgodnie z rekomendacją) 28. Kontrola formularzy: Controlled via React Hook Form (zgodnie z rekomendacją) 29. Routing: Astro file-based routing z View Transitions (zgodnie z rekomendacją) 30. Empty states: Różne komponenty dla różnych scenariuszy (zgodnie z rekomendacją) </decisions> <matched_recommendations>
Strona startowa: /generate jako landing page po zalogowaniu dla szybkiego dostępu do głównej funkcji aplikacji
Single-page workflow dla AI: Formularz generowania → loading → sugestie → save na jednej stronie z płynną animacją
Polling mechanism: Spinner + progress bar + polling co 2-3s do GET /ai/sessions/{sessionId}, wyświetlenie sugestii po status=COMPLETED
Cards w responsive grid: 2-3 kolumny desktop, 1 na mobile, używając Shadcn/ui Card component (New York style)
Top navigation bar: Logo + horizontal nav (Generuj AI | Moje Fiszki | Sesja Nauki) + user menu dropdown (Account, Logout)
Źródło fiszki jako badge: Subtelny tag (AI/Edytowane/Ręczne) widoczny w liście i modalu edycji dla transparentności i analytics
Dedykowany przycisk "Pokaż odpowiedź": Daje użytkownikowi kontrolę, zgodnie z best practices spaced repetition
Filtrowanie i sortowanie: Dropdown dla source (AI/AI_USER/USER), sortowanie według createdAt/updatedAt
Inline editing w sugestiachs: Kliknięcie edit → pola zamieniają się w editable textareas z character counter, zapis zmodyfikowanego content w approve request
Numbered pagination: Shadcn/ui Pagination component z Previous/Next, wyświetlanie totalPages
Global loading state przy save: Overlay/modal "Zapisywanie fiszek..." + toast "Zapisano X fiszek" + redirect do /flashcards
Modal dla delete confirmation: AlertDialog z preview content + "Anuluj" / "Usuń" (destructive)
Losowa kolejność w studiu (MVP): Client-side shuffle po pobraniu z GET /flashcards, bez spaced repetition metadata
Prosty CSS 3D flip: Tailwind transform utilities + useState toggle, bez Framer Motion
Responsive study view: Desktop = card (max-width: 600px centered), Mobile = near full-screen (padding 1rem)
React Hook Form + Zod: Shadcn/ui Form components z schema validation w src/lib/validation/auth.ts
React Context dla stanu: AuthContext (user, token, methods) + GenerationContext (sessionId, suggestions, selections)
JWT wstrzykiwanie: Axios instance w src/lib/api/client.ts z interceptorem dodającym Authorization header
Token expiry handling: Monitor expiresIn, toast "Sesja wygasła" + redirect do login (brak auto-refresh w MVP)
API services structure:
src/lib/api/
├── client.ts (axios + interceptors)
├── auth.ts
├── ai.ts
├── flashcards.ts
└── types.ts
Hybrydowy error handling: Validation (400) lokalne w formularzu, Auth (401) globalny logout, Server (500) toast notification
Controlled components: React Hook Form dla walidacji real-time, character counters, dynamic validation messages
Astro file-based routing: Pages w src/pages/ + ViewTransitions API dla smooth transitions, React jako interaktywne "wyspy"
Hover actions desktop, visible mobile: Floating action buttons po hover (desktop), małe ikony/dropdown zawsze widoczne (mobile)
Empty states różnicowanie: Nowy user z CTA → /generate, brak wyników filtra z "Wyczyść filtry", wszystkie usunięte podobnie jak nowy user
Full content display: Pokazuj całość front/back na kartach bez truncate, flip animation na hover/click dla back
Brak unsaved changes protection w MVP: Uproszczenie dla szybszego development
Brak keyboard shortcuts w MVP: Prostszy onboarding, focus na core functionality
Brak draft persistence w MVP: Eliminacja złożoności localStorage management
Brak rate limiting UI w MVP: Backend nie implementuje tego w pierwszej wersji
</matched_recommendations> <ui_architecture_planning_summary>
Podsumowanie Planowania Architektury UI dla 10x-cards MVP
1. Główne Wymagania Architektury UI
Aplikacja 10x-cards to flashcard system z generowaniem AI, zbudowany na Astro 5 + React 19 + TypeScript 5 + Tailwind CSS 4 + Shadcn/ui (New York style). Architektura opiera się na zasadzie hybrid rendering: Astro dla statycznych stron, React dla interaktywnych komponentów. Kluczowe założenia MVP:
Maksymalna prostota implementacji
Focus na core functionality (AI generation → review → study)
Brak zaawansowanych features (spaced repetition, drafts, rate limiting UI, keyboard shortcuts)
File-based routing z Astro + View Transitions API
JWT authentication bez auto-refresh
Client-side state management przez React Context
2. Kluczowe Widoki, Ekrany i Przepływy Użytkownika
Inventory Widoków
Publiczne:
/login - Login page (Astro)
/register - Registration page (Astro)
Chronione (wymagają JWT): 3. /generate - AI Generation + Suggestions Review (Astro + React) - DEFAULT LANDING 4. /flashcards - My Flashcards list view (Astro + React) 5. /study - Study Session (Astro + React) Opcjonalne dla MVP: 6. /account - User profile/settings
Przepływy Użytkownika
Flow 1: New User → First AI Flashcards
Landing → Register → Auto-login → /generate (default) 
  → Paste text (1000-10000 chars) 
  → Click "Generuj" 
  → Loading (polling session status) 
  → View suggestions (same page, section below) 
  → Inline edit selected cards 
  → Click "Zapisz wybrane" 
  → Global loading → Success toast 
  → Redirect to /flashcards 
  → See new cards in grid
Flow 2: Returning User → Study
Login → /generate 
  → Navigate to "Sesja Nauki" (/study) 
  → View flashcard front (random order) 
  → Click "Pokaż odpowiedź" 
  → See back (3D flip animation) 
  → Rate difficulty buttons appear 
  → Click rating → Next card 
  → Complete session → Summary
Flow 3: Manual Flashcard Creation
/flashcards 
  → Click "Dodaj fiszkę" 
  → Modal opens with form (front/back fields) 
  → Fill + validate (1-1000 chars) 
  → Click "Zapisz" 
  → POST /flashcards 
  → Modal closes, new card appears in grid
Flow 4: Edit AI Flashcard
/flashcards 
  → Hover card (desktop) → Edit icon appears 
  → Click Edit 
  → Modal with form (pre-filled) 
  → Modify content 
  → Save → PUT /flashcards/{id} 
  → source changes AI → AI_USER 
  → Updated card in list
Flow 5: Delete Flashcard
/flashcards 
  → Hover/tap card → Delete icon 
  → Click Delete 
  → AlertDialog confirmation (shows front content preview) 
  → Confirm → DELETE /flashcards/{id} 
  → Success toast, card removed from grid
3. Strategia Integracji z API i Zarządzania Stanem
API Client Layer Structure
src/lib/api/
├── client.ts           # Axios instance + interceptors
│   ├── baseURL from env variable
│   ├── Request interceptor: inject JWT token
│   ├── Response interceptor: handle 401 → logout
│   └── Global error handling
│
├── types.ts            # Shared TypeScript interfaces
│   ├── AuthResponse { username, accessToken, expiresIn }
│   ├── User { userId, username, email, ... }
│   ├── AISession { sessionId, status, generatedCount, ... }
│   ├── Suggestion { suggestionId, frontContent, backContent }
│   ├── Flashcard { flashcardId, frontContent, backContent, source, ... }
│   └── PaginatedResponse<T>
│
├── auth.ts
│   ├── register(data: RegisterRequest): Promise<{ userId: string }>
│   └── login(credentials: LoginRequest): Promise<AuthResponse>
│
├── ai.ts
│   ├── createSession(text: string): Promise<{ sessionId: string }>
│   ├── getSession(sessionId: string): Promise<AISession>
│   ├── getSuggestions(sessionId: string): Promise<Suggestion[]>
│   └── approveSession(sessionId: string, suggestions: ApproveRequest): Promise<void>
│
└── flashcards.ts
    ├── getFlashcards(params: PaginationParams): Promise<PaginatedResponse<Flashcard>>
    ├── createFlashcard(data: CreateFlashcardRequest): Promise<Flashcard>
    ├── updateFlashcard(id: string, data: UpdateFlashcardRequest): Promise<Flashcard>
    └── deleteFlashcard(id: string): Promise<void>
client.ts - JWT Injection (najprostsza metoda):
import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.PUBLIC_API_URL || 'http://localhost:8080',
});

// Request interceptor - inject token
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Response interceptor - handle 401
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('username');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
State Management Architecture
1. AuthContext (Global)
interface AuthContextType {
  user: { username: string } | null;
  token: string | null;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  register: (data: RegisterRequest) => Promise<void>;
}

// src/lib/auth/AuthContext.tsx
// Provider wraps entire app in Layout
// Auto-initialization: check localStorage on mount
// Monitor token expiry: useEffect → toast + logout when expired
2. GenerationContext (Page-level for /generate)
interface GenerationContextType {
  // Input state
  inputText: string;
  setInputText: (text: string) => void;
  
  // Session state
  sessionId: string | null;
  sessionStatus: 'idle' | 'pending' | 'completed' | 'failed';
  
  // Suggestions state
  suggestions: Suggestion[];
  selectedSuggestions: Set<string>;
  editedContent: Map<string, { front: string; back: string }>;
  
  // Actions
  generateFlashcards: () => Promise<void>;
  toggleSelection: (id: string) => void;
  editSuggestion: (id: string, content: { front: string; back: string }) => void;
  saveApproved: () => Promise<void>;
  
  // UI state
  isGenerating: boolean;
  error: string | null;
}

// Provider wraps /generate page
// Handles polling logic internally
// Exposes clean API to child components
3. Local State (Component-level)
Form state: Managed by React Hook Form
Modal open/close: useState<boolean>
Pagination: useState<number> for currentPage
Loading states: useState<boolean> for async operations
API Integration Patterns
Pattern 1: AI Generation Workflow (Polling)
// In GenerationContext
const generateFlashcards = async () => {
  setIsGenerating(true);
  setError(null);
  
  try {
    // 1. Create session
    const { sessionId } = await createSession(inputText);
    setSessionId(sessionId);
    setSessionStatus('pending');
    
    // 2. Poll for completion
    const pollInterval = setInterval(async () => {
      const session = await getSession(sessionId);
      setSessionStatus(session.status.toLowerCase());
      
      if (session.status === 'COMPLETED') {
        clearInterval(pollInterval);
        const suggestions = await getSuggestions(sessionId);
        setSuggestions(suggestions);
        setIsGenerating(false);
      } else if (session.status === 'FAILED') {
        clearInterval(pollInterval);
        setError('Generowanie nie powiodło się');
        setIsGenerating(false);
      }
    }, 2500); // Poll every 2.5s
    
  } catch (err) {
    setError(err.message);
    setIsGenerating(false);
  }
};
Pattern 2: Paginated List (/flashcards)
const FlashcardsPage = () => {
  const [flashcards, setFlashcards] = useState<Flashcard[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  
  useEffect(() => {
    const fetchFlashcards = async () => {
      setIsLoading(true);
      const response = await getFlashcards({ 
        page: currentPage, 
        size: 20,
        sort: 'createdAt,desc'
      });
      setFlashcards(response.content);
      setTotalPages(response.page.totalPages);
      setIsLoading(false);
    };
    
    fetchFlashcards();
  }, [currentPage]);
  
  return (
    // Grid + Pagination component
  );
};
Pattern 3: Optimistic Updates (Delete)
const handleDelete = async (flashcardId: string) => {
  // Show confirmation dialog first
  const confirmed = await showConfirmDialog();
  if (!confirmed) return;
  
  // Optimistic update
  setFlashcards(prev => prev.filter(f => f.flashcardId !== flashcardId));
  
  try {
    await deleteFlashcard(flashcardId);
    toast.success('Fiszka usunięta');
  } catch (err) {
    // Rollback on error
    fetchFlashcards(); // Re-fetch to restore
    toast.error('Błąd podczas usuwania');
  }
};
4. Responsywność, Dostępność i Bezpieczeństwo
Responsive Design Guidelines
Breakpoints (Tailwind defaults):
sm: 640px - Small tablets
md: 768px - Tablets
lg: 1024px - Desktop
xl: 1280px - Large desktop
Component Adaptations:
Component	Mobile (<640px)	Desktop (≥640px)
Navigation	Hamburger menu / Bottom nav	Horizontal top bar
Flashcards Grid	1 column	2-3 columns
Study Card	Near full-screen (padding 1rem)	Centered card (max-w-xl)
Forms	Full width	Max-width container (max-w-md)
Modals	Full screen	Centered dialog (max-w-lg)
Table/List	Stacked cards	Table/Grid layout
Touch-friendly targets:
Minimum tap area: 44x44px (buttons, icons)
Action buttons always visible on mobile (no hover-only)
Swipe gestures: Not in MVP (keep simple)
Accessibility (ARIA) Requirements
Forms:
All inputs have associated <label> with for attribute
Error messages have role="alert" and aria-live="polite"
Required fields marked with aria-required="true"
Password fields with aria-describedby for requirements
Modals:
role="dialog" and aria-modal="true"
aria-labelledby pointing to modal title
Focus trap inside modal
Close on Escape key
Return focus to trigger element on close
Flashcard Flip:
Button has aria-label="Pokaż odpowiedź"
After flip: aria-live="polite" announces back content
Pagination:
Current page button: aria-current="page"
Previous/Next: aria-label="Poprzednia strona" / "Następna strona"
Loading States:
Spinner with role="status" and aria-label="Ładowanie"
During operation: aria-busy="true" on container
Empty States:
Clear heading and descriptive text (screen reader friendly)
Bezpieczeństwo
JWT Token Management:
Store in localStorage (key: accessToken)
Never log token to console
Clear on logout and 401 responses
Check expiry on app initialization: Date.now() > expiresIn → force logout
Input Sanitization:
React automatically escapes JSX content (XSS protection)
Use Zod validation to prevent malicious input
Backend validates all input (defense in depth)
HTTPS Only:
Environment variable PUBLIC_API_URL should always use https:// in production
Add CSP headers if possible
CSRF Protection:
Not needed for JWT-based API (no cookies)
Backend should validate token properly
Sensitive Data:
Never store passwords in state or localStorage
Clear form data after submission
Don't expose API errors with sensitive details to user
Authorization:
All protected routes check useAuth() → redirect if not authenticated
RLS at database level ensures users only see own data
Frontend should trust backend authorization (don't duplicate logic)
5. Szczegóły Implementacji Kluczowych Features
Feature 1: AI Generation Workflow
Components:
<GenerationPage> (Astro page)
  └─ <GenerationProvider> (React Context)
       ├─ <GenerationForm> (React)
       │    ├─ <Textarea> (Shadcn/ui)
       │    ├─ Character counter (1000-10000)
       │    └─ <Button> Generuj
       │
       └─ <SuggestionsList> (React, conditional render)
            ├─ Loading state (spinner + progress bar)
            ├─ Error state (alert with retry)
            └─ Success state:
                 ├─ <SuggestionCard> × N
                 │    ├─ Checkbox (select)
                 │    ├─ Inline editable fields (front/back)
                 │    └─ Character counters
                 │
                 └─ <Button> Zapisz wybrane (disabled if none selected)
Inline Editing Implementation:
const SuggestionCard = ({ suggestion, onEdit, isSelected, onToggle }) => {
  const [isEditing, setIsEditing] = useState(false);
  const [front, setFront] = useState(suggestion.frontContent);
  const [back, setBack] = useState(suggestion.backContent);
  
  const handleSave = () => {
    onEdit(suggestion.suggestionId, { front, back });
    setIsEditing(false);
  };
  
  return (
    <Card>
      <Checkbox checked={isSelected} onCheckedChange={onToggle} />
      
      {isEditing ? (
        <>
          <Textarea value={front} onChange={e => setFront(e.target.value)} />
          <span>{front.length}/1000</span>
          <Textarea value={back} onChange={e => setBack(e.target.value)} />
          <span>{back.length}/1000</span>
          <Button onClick={handleSave}>Zapisz</Button>
          <Button variant="ghost" onClick={() => setIsEditing(false)}>Anuluj</Button>
        </>
      ) : (
        <>
          <p>{front}</p>
          <p className="text-muted">{back}</p>
          <Button variant="ghost" size="icon" onClick={() => setIsEditing(true)}>
            <PencilIcon />
          </Button>
        </>
      )}
    </Card>
  );
};
Save Approved Flow:
const saveApproved = async () => {
  if (selectedSuggestions.size === 0) {
    toast.error('Wybierz przynajmniej jedną fiszkę');
    return;
  }
  
  setIsLoading(true);
  
  try {
    // Build approve request with edited content
    const suggestionsToApprove = Array.from(selectedSuggestions).map(id => {
      const edited = editedContent.get(id);
      const original = suggestions.find(s => s.suggestionId === id);
      
      return {
        suggestionId: id,
        frontContent: edited?.front || original.frontContent,
        backContent: edited?.back || original.backContent
      };
    });
    
    await approveSession(sessionId, { suggestions: suggestionsToApprove });
    
    toast.success(`Zapisano ${suggestionsToApprove.length} fiszek`);
    navigate('/flashcards'); // Redirect
    
  } catch (err) {
    toast.error('Błąd podczas zapisywania');
  } finally {
    setIsLoading(false);
  }
};
Feature 2: Flashcard Flip Animation
CSS Implementation (Tailwind 4):
/* In global.css or component styles */
.flip-card {
  perspective: 1000px;
}

.flip-card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.6s;
  transform-style: preserve-3d;
}

.flip-card-inner.flipped {
  transform: rotateY(180deg);
}

.flip-card-front,
.flip-card-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
}

.flip-card-back {
  transform: rotateY(180deg);
}
React Component:
const StudyCard = ({ flashcard, onRate }) => {
  const [isFlipped, setIsFlipped] = useState(false);
  
  const handleFlip = () => setIsFlipped(true);
  
  const handleRate = (rating: number) => {
    onRate(flashcard.flashcardId, rating);
    setIsFlipped(false); // Reset for next card
  };
  
  return (
    <div className="flip-card">
      <div className={cn("flip-card-inner", isFlipped && "flipped")}>
        {/* Front */}
        <div className="flip-card-front">
          <Card className="p-8">
            <h2 className="text-2xl">{flashcard.frontContent}</h2>
          </Card>
        </div>
        
        {/* Back */}
        <div className="flip-card-back">
          <Card className="p-8">
            <p className="text-lg">{flashcard.backContent}</p>
          </Card>
        </div>
      </div>
      
      {/* Controls */}
      {!isFlipped ? (
        <Button onClick={handleFlip} size="lg" className="mt-4">
          Pokaż odpowiedź
        </Button>
      ) : (
        <div className="flex gap-2 mt-4">
          <Button onClick={() => handleRate(1)} variant="destructive">Znowu</Button>
          <Button onClick={() => handleRate(2)} variant="secondary">Trudne</Button>
          <Button onClick={() => handleRate(3)} variant="default">Dobre</Button>
          <Button onClick={() => handleRate(4)} variant="success">Łatwe</Button>
        </div>
      )}
    </div>
  );
};
Feature 3: Study Session (Random Order)
const StudySessionPage = () => {
  const [flashcards, setFlashcards] = useState<Flashcard[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [sessionComplete, setSessionComplete] = useState(false);
  
  useEffect(() => {
    const fetchAndShuffleFlashcards = async () => {
      const response = await getFlashcards({ page: 0, size: 100 }); // Get all for session
      
      // Shuffle array (Fisher-Yates)
      const shuffled = [...response.content];
      for (let i = shuffled.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
      }
      
      setFlashcards(shuffled);
      setIsLoading(false);
    };
    
    fetchAndShuffleFlashcards();
  }, []);
  
  const handleRate = (flashcardId: string, rating: number) => {
    // In MVP: just move to next card (no spaced repetition)
    if (currentIndex < flashcards.length - 1) {
      setCurrentIndex(prev => prev + 1);
    } else {
      setSessionComplete(true);
    }
  };
  
  if (isLoading) return <LoadingSpinner />;
  if (flashcards.length === 0) return <EmptyState />;
  if (sessionComplete) return <SessionSummary total={flashcards.length} />;
  
  return (
    <div className="container mx-auto py-8">
      {/* Progress */}
      <div className="mb-4">
        <p className="text-sm text-muted-foreground">
          Fiszka {currentIndex + 1} z {flashcards.length}
        </p>
        <Progress value={(currentIndex / flashcards.length) * 100} />
      </div>
      
      {/* Card */}
      <StudyCard 
        flashcard={flashcards[currentIndex]} 
        onRate={handleRate}
      />
    </div>
  );
};
Feature 4: Form Validation (React Hook Form + Zod)
Validation Schema:
// src/lib/validation/auth.ts
import { z } from 'zod';

export const registerSchema = z.object({
  username: z.string()
    .min(1, 'Nazwa użytkownika jest wymagana')
    .max(50, 'Maksymalnie 50 znaków')
    .regex(/^[a-zA-Z0-9_]+$/, 'Tylko litery, cyfry i _'),
  
  email: z.string()
    .email('Nieprawidłowy format email')
    .max(254, 'Maksymalnie 254 znaki'),
  
  password: z.string()
    .min(8, 'Minimum 8 znaków')
    .regex(/[A-Z]/, 'Wymagana jedna wielka litera')
    .regex(/[a-z]/, 'Wymagana jedna mała litera')
    .regex(/[0-9]/, 'Wymagana jedna cyfra'),
  
  firstName: z.string().max(100).optional(),
  lastName: z.string().max(100).optional(),
});

export type RegisterFormData = z.infer<typeof registerSchema>;
Form Component:
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

const RegisterForm = () => {
  const { register: registerUser } = useAuth();
  const form = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      username: '',
      email: '',
      password: '',
    }
  });
  
  const onSubmit = async (data: RegisterFormData) => {
    try {
      await registerUser(data);
      toast.success('Rejestracja zakończona sukcesem');
      // Auto-login handled in AuthContext
    } catch (err) {
      if (err.response?.status === 409) {
        form.setError('email', { message: 'Email już istnieje' });
      } else {
        toast.error('Błąd rejestracji');
      }
    }
  };
  
  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)}>
        <FormField
          control={form.control}
          name="username"
          render={({ field }) => (
            <FormItem>
              <FormLabel>Nazwa użytkownika</FormLabel>
              <FormControl>
                <Input {...field} />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />
        
        {/* Repeat for email, password, etc. */}
        
        <Button type="submit" disabled={form.formState.isSubmitting}>
          {form.formState.isSubmitting ? 'Rejestrowanie...' : 'Zarejestruj się'}
        </Button>
      </form>
    </Form>
  );
};
6. Struktura Komponentów
src/
├── components/
│   ├── ui/                      # Shadcn/ui components (pre-built)
│   │   ├── button.tsx
│   │   ├── card.tsx
│   │   ├── input.tsx
│   │   ├── form.tsx
│   │   ├── dialog.tsx
│   │   ├── alert-dialog.tsx
│   │   ├── toast.tsx / sonner.tsx
│   │   ├── pagination.tsx
│   │   ├── progress.tsx
│   │   └── ...
│   │
│   ├── layout/
│   │   ├── Header.tsx           # Top navigation bar
│   │   ├── Footer.tsx
│   │   └── ProtectedRoute.tsx   # Auth guard wrapper
│   │
│   ├── auth/
│   │   ├── LoginForm.tsx        # React Hook Form + Zod
│   │   └── RegisterForm.tsx
│   │
│   ├── generation/
│   │   ├── GenerationForm.tsx   # Textarea + Generate button
│   │   ├── GenerationProvider.tsx  # Context provider
│   │   ├── SuggestionsList.tsx  # List of suggestions
│   │   ├── SuggestionCard.tsx   # Individual suggestion with inline edit
│   │   └── LoadingState.tsx     # Polling progress UI
│   │
│   ├── flashcards/
│   │   ├── FlashcardGrid.tsx    # Responsive grid layout
│   │   ├── FlashcardCard.tsx    # Individual card with flip + actions
│   │   ├── FlashcardFilters.tsx # Source filter + sort dropdown
│   │   ├── CreateFlashcardModal.tsx
│   │   ├── EditFlashcardModal.tsx
│   │   ├── DeleteConfirmDialog.tsx
│   │   └── EmptyState.tsx       # Different states (new user, no results, etc.)
│   │
│   └── study/
│       ├── StudyCard.tsx        # Flip card with controls
│       ├── SessionSummary.tsx   # End of session stats
│       └── ProgressBar.tsx      # X of Y indicator
│
├── layouts/
│   ├── BaseLayout.astro         # HTML head, global styles
│   └── AppLayout.astro          # Header + Footer + slot
│
├── pages/
│   ├── index.astro              # Redirects to /generate or marketing
│   ├── login.astro
│   ├── register.astro
│   ├── generate.astro           # AI generation page
│   ├── flashcards.astro         # My flashcards list
│   └── study.astro              # Study session
│
├── lib/
│   ├── api/                     # API client layer (described above)
│   ├── auth/
│   │   └── AuthContext.tsx      # Global auth state
│   ├── validation/
│   │   ├── auth.ts              # Zod schemas for auth forms
│   │   └── flashcard.ts         # Zod schemas for flashcard forms
│   └── utils.ts                 # cn() helper
│
└── styles/
    └── global.css               # Tailwind imports + custom CSS
7. Tech Stack Implementation Details
Astro 5:
Static output mode (output: "static")
View Transitions API for smooth page transitions
File-based routing in src/pages/
React integration via @astrojs/react
Islands architecture (React components as interactive islands)
React 19:
Functional components only
Hooks: useState, useEffect, useContext, custom hooks
Context API for global state (AuthContext, GenerationContext)
Controlled forms via React Hook Form
TypeScript 5:
Strict mode enabled
Absolute imports via @/* path alias
Interface definitions in lib/api/types.ts
Zod for runtime validation + TypeScript type inference
Tailwind CSS 4:
Imported via @import "tailwindcss" in global.css
No traditional tailwind.config.js (uses Vite plugin)
Custom theme via @theme inline in CSS
Dark mode: @custom-variant dark (&:is(.dark *))
Utility-first approach with responsive breakpoints
Shadcn/ui (New York style):
Components in src/components/ui/
Uses Radix UI primitives under the hood
Styled with Tailwind + CSS variables for theming
Components: Button, Card, Input, Form, Dialog, AlertDialog, Toast, Pagination, Progress
cn() utility for conditional class merging (clsx + tailwind-merge)
8. Validation i Error Handling
Client-side Validation:
Forms: React Hook Form + Zod schemas
Real-time validation with error messages below fields
Character counters for text inputs (show count/max)
Submit button disabled when form invalid
API Error Handling:
HTTP Status	Handling Strategy
400 Bad Request	Show validation errors in form (field-level)
401 Unauthorized	Global: Clear token + redirect to /login
403 Forbidden	Toast: "Brak dostępu"
404 Not Found	Toast: "Nie znaleziono zasobu"
409 Conflict	Form error: "Email/username już istnieje"
422 Unprocessable	Toast: "Błąd przetwarzania, spróbuj ponownie"
500 Server Error	Toast: "Błąd serwera, spróbuj później"
Network Error	Toast: "Brak połączenia z serwerem"
Loading States:
Forms: Button spinner + disabled state + text change ("Zapisywanie...")
Lists: Skeleton cards while loading
AI Generation: Spinner + progress bar + status text
Global: Optional loading overlay for critical operations
Empty States:
New user: "Nie masz jeszcze fiszek" + CTA "Wygeneruj pierwszą"
No filter results: "Brak wyników" + "Wyczyść filtry"
All deleted: Similar to new user state
9. Routing i Navigation
File-based Routing (Astro):
src/pages/
├── index.astro          → /
├── login.astro          → /login
├── register.astro       → /register
├── generate.astro       → /generate (default after login)
├── flashcards.astro     → /flashcards
└── study.astro          → /study
Navigation Structure: Header (Desktop):
[Logo] | Generuj AI | Moje Fiszki | Sesja Nauki        [username ▼]
                                                         ├─ Konto
                                                         └─ Wyloguj
Header (Mobile - Hamburger or Bottom Nav):
☰ Menu
├─ Generuj AI
├─ Moje Fiszki
├─ Sesja Nauki
├─ Konto
└─ Wyloguj
Protected Routes:
All routes except /login and /register require authentication
Use ProtectedRoute.tsx wrapper or check useAuth() in page component
Redirect to /login if not authenticated
After login: redirect to /generate (default landing)
View Transitions:
---
// In BaseLayout.astro
import { ViewTransitions } from 'astro:transitions';
---
<head>
  <ViewTransitions />
</head>
10. Performance Considerations (MVP)
Code Splitting:
Astro automatically splits by page
React components loaded as islands (lazy by default)
Image Optimization:
Use Astro <Image> component for static images
Not critical for MVP (mostly text-based)
API Optimization:
Pagination (20 items per page) to limit data transfer
No infinite scroll (simpler implementation)
Poll interval: 2.5s (balance between responsiveness and API load)
React Optimization:
Avoid unnecessary re-renders (keep context updates minimal)
Use React.memo for expensive components (if needed)
Form state isolated in React Hook Form (doesn't trigger parent re-renders)
Bundle Size:
Shadcn/ui components are tree-shakeable (import only what's needed)
No heavy animation libraries (CSS-only flip)
Astro ships zero JS by default (only React islands have JS)
</ui_architecture_planning_summary> <unresolved_issues>
Environment Variables Configuration:
Need to define PUBLIC_API_URL for backend API base URL
Separate configs for development (localhost:8080) vs production
Astro uses import.meta.env.PUBLIC_* for client-accessible vars
Token Expiry Monitoring Implementation:
expiresIn from API is timestamp or duration in seconds?
Need clarification on format to implement proper expiry check
Should expiry check happen on every route change, or interval-based?
Study Session Empty State:
What happens when user starts study session but has 0 flashcards?
Should there be a minimum number of flashcards required to start session?
Redirect to /flashcards with message, or allow empty session?
Filtering Implementation Details:
API plan shows ?source=AI parameter, but does it accept comma-separated for multiple filters?
Can user filter by multiple sources simultaneously (AI + AI_USER)?
Should filters persist across sessions (localStorage)?
Session Summary Metrics:
What metrics should be displayed after completing study session?
Just "Completed X flashcards" or track ratings (Again/Hard/Good/Easy counts)?
Store session history locally or no persistence in MVP?
Error Recovery for Failed AI Generation:
When session status = FAILED, should user be able to retry with same text?
Should sessionId be reused or create new session?
What error message to show? (generic vs specific AI error)
Concurrent AI Sessions:
Can user start new generation while previous is still PENDING?
Should UI block new generation until current completes?
Or allow multiple concurrent sessions with tabs/list?
Logout Behavior:
Should logout clear localStorage completely (including drafts if implemented later)?
Redirect to /login or landing page?
Show confirmation dialog or immediate logout?
Badge/Tag Visual Design for Source:
Specific colors for AI (blue?), AI_USER (yellow?), USER (green?)?
Should badge be clickable to filter by that source?
Icon + text, or just text?
Approval Flow Confirmation:
After clicking "Zapisz wybrane", should there be a confirmation dialog?
Or direct save with toast notification only?
Can user go back to edit after save, or are suggestions lost?
Study Session Randomization Scope:
Random from all flashcards, or only recent X flashcards?
Should user be able to choose subset (e.g., only AI-generated, only from specific date range)?
Or always all flashcards shuffled?
Pagination State Persistence:
When user navigates from /flashcards (page 3) to /generate and back, should they return to page 3?
Or always reset to page 1?
Use URL query params (?page=3) or just local state?
</unresolved_issues> </conversation_summary>
</session_notes>

Twoim zadaniem jest stworzenie szczegółowej architektury interfejsu użytkownika, która obejmuje niezbędne widoki, mapowanie podróży użytkownika, strukturę nawigacji i kluczowe elementy dla każdego widoku. Projekt powinien uwzględniać doświadczenie użytkownika, dostępność i bezpieczeństwo.

Wykonaj następujące kroki, aby ukończyć zadanie:

1. Dokładnie przeanalizuj PRD, plan API i notatki z sesji.
2. Wyodrębnij i wypisz kluczowe wymagania z PRD.
3. Zidentyfikuj i wymień główne punkty końcowe API i ich cele.
4. Utworzenie listy wszystkich niezbędnych widoków na podstawie PRD, planu API i notatek z sesji.
5. Określenie głównego celu i kluczowych informacji dla każdego widoku.
6. Zaplanuj podróż użytkownika między widokami, w tym podział krok po kroku dla głównego przypadku użycia.
7. Zaprojektuj strukturę nawigacji.
8. Zaproponuj kluczowe elementy interfejsu użytkownika dla każdego widoku, biorąc pod uwagę UX, dostępność i bezpieczeństwo.
9. Rozważ potencjalne przypadki brzegowe lub stany błędów.
10. Upewnij się, że architektura interfejsu użytkownika jest zgodna z planem API.
11. Przejrzenie i zmapowanie wszystkich historyjek użytkownika z PRD do architektury interfejsu użytkownika.
12. Wyraźne mapowanie wymagań na elementy interfejsu użytkownika.
13. Rozważ potencjalne punkty bólu użytkownika i sposób, w jaki interfejs użytkownika je rozwiązuje.

Dla każdego głównego kroku pracuj wewnątrz tagów <ui_architecture_planning> w bloku myślenia, aby rozbić proces myślowy przed przejściem do następnego kroku. Ta sekcja może być dość długa. To w porządku, że ta sekcja może być dość długa.

Przedstaw ostateczną architekturę interfejsu użytkownika w następującym formacie Markdown:

```markdown
# Architektura UI dla [Nazwa produktu]

## 1. Przegląd struktury UI

[Przedstaw ogólny przegląd struktury UI]

## 2. Lista widoków

[Dla każdego widoku podaj:
- Nazwa widoku
- Ścieżka widoku
- Główny cel
- Kluczowe informacje do wyświetlenia
- Kluczowe komponenty widoku
- UX, dostępność i względy bezpieczeństwa]

## 3. Mapa podróży użytkownika

[Opisz przepływ między widokami i kluczowymi interakcjami użytkownika]

## 4. Układ i struktura nawigacji

[Wyjaśnij, w jaki sposób użytkownicy będą poruszać się między widokami]

## 5. Kluczowe komponenty

[Wymień i krótko opisz kluczowe komponenty, które będą używane w wielu widokach].
```

Skup się wyłącznie na architekturze interfejsu użytkownika, podróży użytkownika, nawigacji i kluczowych elementach dla każdego widoku. Nie uwzględniaj szczegółów implementacji, konkretnego projektu wizualnego ani przykładów kodu, chyba że są one kluczowe dla zrozumienia architektury.

Końcowy rezultat powinien składać się wyłącznie z architektury UI w formacie Markdown w języku polskim, którą zapiszesz w pliku .claude/docs/ui-plan.md. Nie powielaj ani nie powtarzaj żadnej pracy wykonanej w bloku myślenia.