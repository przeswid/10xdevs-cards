package com.ten.devs.cards.cards.flashcards.application.command;

import an.awesome.pipelinr.Command;
import com.ten.devs.cards.cards.flashcards.domain.Flashcard;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardRepository;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardSnapshot;
import com.ten.devs.cards.cards.flashcards.domain.FlashcardSource;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetFlashcardsResponse;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetFlashcardsResponse.FlashcardSummary;
import com.ten.devs.cards.cards.flashcards.presentation.response.GetFlashcardsResponse.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Handler for GetFlashcardsCommand
 * Retrieves user's flashcards with pagination and filtering
 */
@Component
@RequiredArgsConstructor
class GetFlashcardsCommandHandler implements Command.Handler<GetFlashcardsCommand, GetFlashcardsResponse> {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final FlashcardRepository flashcardRepository;

    @Override
    public GetFlashcardsResponse handle(GetFlashcardsCommand command) {
        List<Flashcard> allFlashcards = flashcardRepository.findByUserId(command.userId());

        List<FlashcardSnapshot> filteredSnapshots = allFlashcards.stream()
                .map(Flashcard::toSnapshot)
                .filter(snapshot -> matchesSourceFilter(snapshot, command.source()))
                .sorted(getComparator(command.sort()))
                .toList();

        int page = command.page() != null ? command.page() : DEFAULT_PAGE;
        int size = command.size() != null ? Math.min(command.size(), MAX_SIZE) : DEFAULT_SIZE;

        long totalElements = filteredSnapshots.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, filteredSnapshots.size());

        List<FlashcardSummary> pageContent = fromIndex < filteredSnapshots.size()
                ? filteredSnapshots.subList(fromIndex, toIndex).stream()
                    .map(this::toSummary)
                    .toList()
                : List.of();

        PageInfo pageInfo = new PageInfo(page, size, totalElements, totalPages);

        return new GetFlashcardsResponse(pageContent, pageInfo);
    }

    private boolean matchesSourceFilter(FlashcardSnapshot snapshot, String sourceFilter) {
        if (sourceFilter == null || sourceFilter.isBlank()) {
            return true;
        }
        try {
            FlashcardSource filterSource = FlashcardSource.valueOf(sourceFilter.toUpperCase());
            return snapshot.source() == filterSource;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private Comparator<FlashcardSnapshot> getComparator(String sort) {
        if (sort == null || sort.isBlank()) {
            return Comparator.comparing(FlashcardSnapshot::createdAt).reversed();
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();
        boolean ascending = parts.length < 2 || !"desc".equalsIgnoreCase(parts[1].trim());

        Comparator<FlashcardSnapshot> comparator = switch (field.toLowerCase()) {
            case "updatedat" -> Comparator.comparing(FlashcardSnapshot::updatedAt);
            case "frontcontent" -> Comparator.comparing(FlashcardSnapshot::frontContent);
            default -> Comparator.comparing(FlashcardSnapshot::createdAt);
        };

        return ascending ? comparator : comparator.reversed();
    }

    private FlashcardSummary toSummary(FlashcardSnapshot snapshot) {
        return new FlashcardSummary(
                snapshot.id(),
                snapshot.frontContent(),
                snapshot.backContent(),
                snapshot.source().name(),
                snapshot.createdAt(),
                snapshot.updatedAt()
        );
    }
}