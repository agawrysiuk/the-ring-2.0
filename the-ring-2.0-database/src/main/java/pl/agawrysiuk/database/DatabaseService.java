package pl.agawrysiuk.database;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.agawrysiuk.model.Card;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DatabaseService {

    private final CardRepository cardRepository;

    public List<Card> getCards() {
        return cardRepository.findAll();
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
