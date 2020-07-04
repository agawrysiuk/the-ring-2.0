package pl.agawrysiuk.database.cards;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.model.Card;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;

    public List<Card> getCards() {
        return cardRepository.findAll();
    }

    public List<CardDto> getClientMissingCards(List<String> missing) {
        List<Card> cards = cardRepository.findByTitleIn(missing);
        return cards.stream()
                .map(card -> CardDto.builder().title(card.getTitle()).json(card.getJson()).build())
                .collect(Collectors.toList());
    }

    public List<String> checkCardsDatabase(List<String> cardsToCheck) {
        List<Card> cards = cardRepository.findByTitleIn(cardsToCheck);
        return checkMissing(cardsToCheck, cards);
    }

    private List<String> checkMissing(List<String> requestList, List<Card> databaseList) {
        return requestList.stream()
                .filter(title -> !isCardPresent(title, databaseList))
                .collect(Collectors.toList());
    }

    private boolean isCardPresent(String title, List<Card> databaseList) {
        return databaseList.stream()
                .map(Card::getTitle)
                .anyMatch(card -> card.equals(title));
    }
}
