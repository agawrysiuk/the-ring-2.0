package pl.agawrysiuk.gui.decks;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.service.monitor.DatabaseMonitor;
import pl.agawrysiuk.service.saver.DeckSaver;

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
        //todo change it to check on the backend
        List<String> missing = monitor.checkExistingCardsMatch(
                getDeckList(deckInfo)
                        .stream()
                        .map(this::getCardName)
                        .collect(Collectors.toList()));
        if (missing.isEmpty()) {
            log.info("All cards exist. Saving deck to sql.");
            saveDeck(getDeckList(deckInfo));

            deckView.getInfoText().setText("Saving deck done.");
            log.info("Saving deck done.");
        } else {
            handleCardNotFound(missing);
        }
    }

    private void saveDeck(List<String> deckList) {
        DeckSaver deckSaver = new DeckSaver();
        deckSaver.saveDeck(deckList);
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
