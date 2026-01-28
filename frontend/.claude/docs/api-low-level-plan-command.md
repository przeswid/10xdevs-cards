Jako starszy programista frontendu Twoim zadaniem jest stworzenie szczegółowego planu wdrożenia nowego widoku w aplikacji internetowej. Plan ten powinien być kompleksowy i wystarczająco jasny dla innego programisty frontendowego, aby mógł poprawnie i wydajnie wdrożyć widok.

Najpierw przejrzyj następujące informacje:

1. Product Requirements Document (PRD):
<prd>
@.claude/docs/prd.md
</prd>

2. Opis widoku:
<view_description>
### 2.3 Widok generowania AI (default landing)
</view_description>

3. User Stories:
<user_stories>
<user_story>
ID: US-003
Title: AI flashcard generation
Description: As a logged-in user, I want to paste a piece of text and use a button to generate flashcard suggestions to save time on manually creating questions and answers.
Acceptance Criteria:
- The flashcard generation view contains a text field where user can paste their text.
- Text field expects 1000 to 10,000 characters.
- After clicking the generation button, application communicates with LLM model API and displays a list of generated flashcard suggestions for user approval.
- In case of API problems or no model response, user will see an appropriate error message.
</user_story>

<user_story>
ID: US-004
Title: Review and approve flashcard suggestions
Description: As a logged-in user, I want to be able to review generated flashcards and decide which ones I want to add to my set, to keep only useful questions and answers.
Acceptance Criteria:
- List of generated flashcards is displayed below the generation form.
- Each flashcard has a button allowing approval, editing, or rejection.
- After approving selected flashcards, user can click save button and add them to database.
</user_stpry>
</user_stories>

4. Endpoint Description:
<endpoint_description>

<endpoint>
#### POST /ai/sessions
**Description:** Start AI flashcard generation session  
**Headers:** `Authorization: Bearer {token}`
**Request Body:**
```json
{
  "inputText": "string (1000-10000 chars, required)"
}
```
**Success Response:** `201 Created`
```json
{
  "sessionId": "uuid",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00Z"
}
```
**Error Responses:**
- `400 Bad Request` - Invalid input text length
- `401 Unauthorized` - Invalid or missing token
- `422 Unprocessable Entity` - AI service unavailable
</endpoint>

<endpoint>
#### GET /ai/sessions/{sessionId}/suggestions
**Description:** Retrieve AI-generated flashcard suggestions  
**Headers:** `Authorization: Bearer {token}`
**Success Response:** `200 OK`
```json
{
  "sessionId": "uuid",
  "status": "COMPLETED",
  "suggestions": [
    {
      "suggestionId": "uuid",
      "frontContent": "What is the capital of France?",
      "backContent": "Paris"
    }
  ]
}
```
**Error Responses:**
- `401 Unauthorized` - Invalid or missing token
- `403 Forbidden` - Session belongs to different user
- `404 Not Found` - Session not found
- `409 Conflict` - Session not completed yet
</endpoint>

</endpoint_description>

5. Endpoint Implementation:
<endpoint_implementation>
@RequestMapping("/ai")
@RestController
@RequiredArgsConstructor
public class AiGenerationController {

    private final Pipeline cqsService;

    /**
     * POST /ai/sessions - Start AI flashcard generation session
     */
    @PostMapping("/sessions")
    public ResponseEntity<CreateAiSessionResponse> createAiSession(
            @Valid @RequestBody CreateAiSessionRequest request
    ) {
        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Dummy user ID

        CreateAiGenerationSessionCommand command = CreateAiGenerationSessionCommand.builder()
                .userId(userId)
                .inputText(request.inputText())
                .build();

        CreateAiSessionResponse response = cqsService.send(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sessions/{sessionId}/suggestions")
    public ResponseEntity<GetAiSuggestionsResponse> getAiSuggestions(
            @PathVariable UUID sessionId
    ) {
        // TODO: Extract userId from SecurityContext
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Dummy user ID

        GetAiSuggestionsCommand command = GetAiSuggestionsCommand.builder()
                .userId(userId)
                .sessionId(sessionId)
                .build();

        GetAiSuggestionsResponse response = cqsService.send(command);
        return ResponseEntity.ok(response);
    }
</endpoint_implementation>

6. Type Definitions:
<type_definitions>
public record CreateAiSessionRequest(
    @NotBlank(message = "Input text is required")
    @Size(min = 1000, max = 10000, message = "Input text must be between 1000 and 10000 characters")
    String inputText
) {
}

public record CreateAiSessionResponse(
    UUID sessionId,
    String status,      // PENDING, COMPLETED, FAILED
    Instant createdAt
) {
}

public record GetAiSuggestionsResponse(
    UUID sessionId,
    String status,          // Should be COMPLETED for suggestions to be available
    List<FlashcardSuggestion> suggestions
) {
    
    /**
     * Individual flashcard suggestion from AI
     * Based on flashcards table content structure
     */
    public record FlashcardSuggestion(
        UUID suggestionId,
        String frontContent,    // Max 1000 chars as per flashcards table
        String backContent      // Max 1000 chars as per flashcards table
    ) {
    }
}

</type_definitions>

7. Tech Stack:
<tech_stack>
- **Astro 5** - Static site generation framework with SSR capabilities
- **React 19** - UI library for interactive components only
- **TypeScript 5** - Type-safe JavaScript with strict configuration
- **Tailwind CSS 4** - Utility-first CSS framework via Vite plugin
- **Shadcn/ui** - Component library configured with New York style
- **Node.js v22.14.0** - Required runtime version (see .nvmrc)
</tech_stack>

Przed utworzeniem ostatecznego planu wdrożenia przeprowadź analizę i planowanie wewnątrz tagów <implementation_breakdown> w swoim bloku myślenia. Ta sekcja może być dość długa, ponieważ ważne jest, aby być dokładnym.

W swoim podziale implementacji wykonaj następujące kroki:
1. Dla każdej sekcji wejściowej (PRD, User Stories, Endpoint Description, Endpoint Implementation, Type Definitions, Tech Stack):
  - Podsumuj kluczowe punkty
 - Wymień wszelkie wymagania lub ograniczenia
 - Zwróć uwagę na wszelkie potencjalne wyzwania lub ważne kwestie
2. Wyodrębnienie i wypisanie kluczowych wymagań z PRD
3. Wypisanie wszystkich potrzebnych głównych komponentów, wraz z krótkim opisem ich opisu, potrzebnych typów, obsługiwanych zdarzeń i warunków walidacji
4. Stworzenie wysokopoziomowego diagramu drzewa komponentów
5. Zidentyfikuj wymagane DTO i niestandardowe typy ViewModel dla każdego komponentu widoku. Szczegółowo wyjaśnij te nowe typy, dzieląc ich pola i powiązane typy.
6. Zidentyfikuj potencjalne zmienne stanu i niestandardowe hooki, wyjaśniając ich cel i sposób ich użycia
7. Wymień wymagane wywołania API i odpowiadające im akcje frontendowe
8. Zmapuj każdej historii użytkownika do konkretnych szczegółów implementacji, komponentów lub funkcji
9. Wymień interakcje użytkownika i ich oczekiwane wyniki
10. Wymień warunki wymagane przez API i jak je weryfikować na poziomie komponentów
11. Zidentyfikuj potencjalne scenariusze błędów i zasugeruj, jak sobie z nimi poradzić
12. Wymień potencjalne wyzwania związane z wdrożeniem tego widoku i zasugeruj możliwe rozwiązania

Po przeprowadzeniu analizy dostarcz plan wdrożenia w formacie Markdown z następującymi sekcjami:

1. Przegląd: Krótkie podsumowanie widoku i jego celu.
2. Routing widoku: Określenie ścieżki, na której widok powinien być dostępny.
3. Struktura komponentów: Zarys głównych komponentów i ich hierarchii.
4. Szczegóły komponentu: Dla każdego komponentu należy opisać:
 - Opis komponentu, jego przeznaczenie i z czego się składa
 - Główne elementy HTML i komponenty dzieci, które budują komponent
 - Obsługiwane zdarzenia
 - Warunki walidacji (szczegółowe warunki, zgodnie z API)
 - Typy (DTO i ViewModel) wymagane przez komponent
 - Propsy, które komponent przyjmuje od rodzica (interfejs komponentu)
5. Typy: Szczegółowy opis typów wymaganych do implementacji widoku, w tym dokładny podział wszelkich nowych typów lub modeli widoku według pól i typów.
6. Zarządzanie stanem: Szczegółowy opis sposobu zarządzania stanem w widoku, określenie, czy wymagany jest customowy hook.
7. Integracja API: Wyjaśnienie sposobu integracji z dostarczonym punktem końcowym. Precyzyjnie wskazuje typy żądania i odpowiedzi.
8. Interakcje użytkownika: Szczegółowy opis interakcji użytkownika i sposobu ich obsługi.
9. Warunki i walidacja: Opisz jakie warunki są weryfikowane przez interfejs, których komponentów dotyczą i jak wpływają one na stan interfejsu
10. Obsługa błędów: Opis sposobu obsługi potencjalnych błędów lub przypadków brzegowych.
11. Kroki implementacji: Przewodnik krok po kroku dotyczący implementacji widoku.

Upewnij się, że Twój plan jest zgodny z PRD, historyjkami użytkownika i uwzględnia dostarczony stack technologiczny.

Ostateczne wyniki powinny być w języku polskim i zapisane w pliku o nazwie .ai/{view-name}-view-implementation-plan.md. Nie uwzględniaj żadnej analizy i planowania w końcowym wyniku.

Oto przykład tego, jak powinien wyglądać plik wyjściowy (treść jest do zastąpienia):

```markdown
# Plan implementacji widoku [Nazwa widoku]

## 1. Przegląd
[Krótki opis widoku i jego celu]

## 2. Routing widoku
[Ścieżka, na której widok powinien być dostępny]

## 3. Struktura komponentów
[Zarys głównych komponentów i ich hierarchii]

## 4. Szczegóły komponentów
### [Nazwa komponentu 1]
- Opis komponentu [opis]
- Główne elementy: [opis]
- Obsługiwane interakcje: [lista]
- Obsługiwana walidacja: [lista, szczegółowa]
- Typy: [lista]
- Propsy: [lista]

### [Nazwa komponentu 2]
[...]

## 5. Typy
[Szczegółowy opis wymaganych typów]

## 6. Zarządzanie stanem
[Opis zarządzania stanem w widoku]

## 7. Integracja API
[Wyjaśnienie integracji z dostarczonym endpointem, wskazanie typów żądania i odpowiedzi]

## 8. Interakcje użytkownika
[Szczegółowy opis interakcji użytkownika]

## 9. Warunki i walidacja
[Szczegółowy opis warunków i ich walidacji]

## 10. Obsługa błędów
[Opis obsługi potencjalnych błędów]

## 11. Kroki implementacji
1. [Krok 1]
2. [Krok 2]
3. [...]
```

Rozpocznij analizę i planowanie już teraz. Twój ostateczny wynik powinien składać się wyłącznie z planu wdrożenia w języku polskim w formacie markdown, który zapiszesz w pliku ..claude/docs/{view-name}-view-implementation-plan.md i nie powinien powielać ani powtarzać żadnej pracy wykonanej w podziale implementacji.