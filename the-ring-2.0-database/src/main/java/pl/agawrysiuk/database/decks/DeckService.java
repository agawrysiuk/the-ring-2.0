package pl.agawrysiuk.database.decks;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.agawrysiuk.dto.DeckSimpleDto;
import pl.agawrysiuk.model.Card;
import pl.agawrysiuk.model.Deck;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;

    public List<DeckSimpleDto> getDecks() {
        List<Deck> decks = deckRepository.findAll();
        return decks.stream()
                .map(this::createDeckSimpleDto)
                .collect(Collectors.toList());
    }
    private DeckSimpleDto createDeckSimpleDto(Deck deck) {
        return DeckSimpleDto.builder()
                .title(deck.getTitle())
                .cards(getCardTitles(deck))
                .build();
    }

    private Map<String, Long> getCardTitles(Deck deck) {
        return deck.getCards().stream()
                .map(Card::getTitle)
                .collect(Collectors.groupingBy(Function.identity(),Collectors.counting()));

    }
}
