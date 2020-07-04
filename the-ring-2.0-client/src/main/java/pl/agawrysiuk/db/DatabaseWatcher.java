package pl.agawrysiuk.db;

import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.model.Card;

import java.util.List;
import java.util.stream.Collectors;

public class DatabaseWatcher {

    private Database database;

    public DatabaseWatcher(Database database) {
        this.database = database;
    }

    public List<String> cardsPresent(List<String> toCheck) {
        toCheck.retainAll(getDatabaseCardTitles());
        return toCheck;
    }

    private List<String> getDatabaseCardTitles() {
        return database.getDatabaseCards().stream()
                .map(Card::getTitle)
                .collect(Collectors.toList());
    }

    public void addMissingCards(List<CardDto> missingCards) {
        database.addCards(missingCards);
        database.saveDatabase();
    }
}
