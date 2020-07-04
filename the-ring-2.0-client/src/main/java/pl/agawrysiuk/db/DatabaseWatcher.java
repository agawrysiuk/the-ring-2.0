package pl.agawrysiuk.db;

import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.dto.DeckSimpleDto;
import pl.agawrysiuk.model.Card;

import java.util.List;
import java.util.stream.Collectors;

public class DatabaseWatcher {

    private Database database;

    public DatabaseWatcher(Database database) {
        this.database = database;
    }

    public List<String> cardsPresent(List<String> toCheck) {
        List<String> existing = getDatabaseCardTitles();
        return toCheck.stream()
                .filter(card -> !existing.contains(card))
                .collect(Collectors.toList());
    }

    private List<String> getDatabaseCardTitles() {
        return database.getDatabaseCards().stream()
                .map(Card::getTitle)
                .collect(Collectors.toList());
    }

    public void addMissingCards(List<CardDto> missingCards) {
        database.addCards(missingCards);
    }

    public void addMissingDecksIfNeeded(List<DeckSimpleDto> simpleDecks) {
        database.addDecksIfNeeded(simpleDecks);
    }

    public void saveDatabase() {
        database.saveDatabase();
    }
}
