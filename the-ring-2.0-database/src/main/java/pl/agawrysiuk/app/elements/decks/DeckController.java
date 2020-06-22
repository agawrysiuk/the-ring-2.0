package pl.agawrysiuk.app.elements.decks;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.model.Card;
import pl.agawrysiuk.service.monitor.CardNotFoundException;
import pl.agawrysiuk.service.monitor.DatabaseMonitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class DeckController {

    @Setter
    private DeckView deckView;

    public void searchAndSaveDeck(String deckInfo) {
        log.info("Checking for cards.");
        DatabaseMonitor monitor = new DatabaseMonitor();
        List<Card> cards = monitor.getExistingCards();
        checkCards(deckInfo, cards);
    }

    private void checkCards(String deckInfo, List<Card> cards) {
        List<String> deckList = getDeckList(deckInfo);
        List<String> missing = new ArrayList<>();
        for (String cardInfo : deckList) {
            try {
                String cardName = getCardName(cardInfo);
                cards.stream()
                        .filter(card -> card.getTitle().equals(cardName))
                        .findFirst()
                        .orElseThrow(() -> new CardNotFoundException(cardName));
            } catch (CardNotFoundException e) {
                missing.add(e.getMessage());
            }
        }
        if (missing.isEmpty()) {
            log.info("Saving deck done.");
        } else {
            handleCardNotFound(missing);
        }
    }

    private List<String> getDeckList(String deckInfo) {
        return deckInfo.lines()
                .filter(line -> !line.isBlank())
                .filter(line -> Character.isDigit(line.charAt(0)))
                .collect(Collectors.toList());
    }

    private String getCardName(String cardInfo) {
        return Arrays.stream(cardInfo.split(" ",2))
                .skip(1)
                .collect(Collectors.joining());
    }

    private void handleCardNotFound(List<String> missing) {
        log.info("Saving deck failed. Cards are missing.");
        deckView.getInfoText().setText(collectToText(missing));
    }

    private String collectToText(List<String> missing) {
        return "Cards missing:\n\n" + missing.stream().map(card -> card.concat("\n")).collect(Collectors.joining());
    }
}
