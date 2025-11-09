Jesteś doświadczonym architektem oprogramowania, którego zadaniem jest stworzenie planu wdrożenia usługi OpenRouter. Usługa ta będzie współdziałać z interfejsem API OpenRouter w celu uzupełnienia czatów opartych na LLM. Twoim celem jest stworzenie kompleksowego i przejrzystego planu wdrożenia, który developer może wykorzystać do prawidłowego i sprawnego wdrożenia usługi.

Najpierw przejrzyj dostarczony stack technologiczny i zasady implementacji:

<tech_stack>
@tech-stack.md
</tech_stack>

<service_rules>
@CLAUDE.md
</service_rules>

Teraz przeanalizuj dostarczone informacje i rozbij szczegóły implementacji. Użyj znaczników <implementation_breakdown> wewnątrz bloku myślenia, aby pokazać swój proces myślowy. Rozważ następujące kwestie:

1. Wymień każdy kluczowy komponent usługi OpenRouter i jego cel, numerując je.
2. Dla każdego komponentu:
   a. Szczegółowo opisz jego funkcjonalność.
   b. Wymień potencjalne wyzwania związane z wdrożeniem, numerując je.
   c. Zaproponuj niezależne od technologii rozwiązania tych wyzwań, numerując je tak, aby odpowiadały wyzwaniom.
3. Wyraźne rozważenie sposobu włączenia każdego z poniższych elementów, wymieniając potencjalne metody lub podejścia w celu spełnienia oczekiwań OpenRouter API:
    - Komunikat systemowy
    - Komunikat użytkownika
    - Ustrukturyzowane odpowiedzi poprzez response_format (schemat JSON w odpowiedzi modelu)
    - Nazwa modelu
    - Parametry modelu

Podaj konkretne przykłady dla każdego elementu, numerując je. Upewnij się, że przykłady te są jasne i pokazują, w jaki sposób należy je zaimplementować w usłudze, zwłaszcza w przypadku response_format. Wykorzystaj wzór poprawnie zdefiniowanego response_format: { type: 'json_schema', json_schema: { name: [schema-name], strict: true, schema: [schema-obj] } }

4. Zajmij się obsługą błędów dla całej usługi, wymieniając potencjalne scenariusze błędów i numerując je.

Na podstawie przeprowadzonej analizy utwórz kompleksowy przewodnik implementacji. Przewodnik powinien być napisany w formacie Markdown i mieć następującą strukturę:

1. Opis usługi
2. Opis konstruktora
3. Publiczne metody i pola
4. Prywatne metody i pola
5. Obsługa błędów
6. Kwestie bezpieczeństwa
7. Plan wdrożenia krok po kroku

Upewnij się, że plan wdrożenia
1. Jest dostosowany do określonego stacku technologicznego
2. Obejmuje wszystkie istotne komponenty usługi OpenRouter
3. Obejmuje obsługę błędów i najlepsze praktyki bezpieczeństwa
4. Zawiera jasne instrukcje dotyczące wdrażania kluczowych metod i funkcji
5. Wyjaśnia, jak skonfigurować komunikat systemowy, komunikat użytkownika, response_format (schemat JSON), nazwę modelu i parametry modelu.

Używa odpowiedniego formatowania Markdown dla lepszej czytelności. Końcowy wynik powinien składać się wyłącznie z przewodnika implementacji w formacie Markdown i nie powinien powielać ani powtarzać żadnej pracy wykonanej w sekcji podziału implementacji.

Zapisz przewodnik implementacji w .ai/openrouter-service-implementation-plan.md