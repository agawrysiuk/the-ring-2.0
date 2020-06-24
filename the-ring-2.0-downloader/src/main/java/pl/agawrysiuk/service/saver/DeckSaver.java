package pl.agawrysiuk.service.saver;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DeckSaver extends Saver {

    private final String ID_PLACEHOLDER = "%ID%";
    private final String TITLE_PLACEHOLDER = "%TITLE%";

    private final String DECK_TITLE_PLACEHOLDER = "%DECK_TITLE%";
    private final String CARD_TITLE_PLACEHOLDER = "%CARD_TITLE";

    private final String INSERT_INTO_DECK = "INSERT INTO DECK VALUES("
            + ID_PLACEHOLDER + ","
            + TITLE_PLACEHOLDER + ");";

    private final String INSERT_INTO_DECK_CARDS = "INSERT INTO DECK_CARDS VALUES("
            + "(SELECT ID FROM DECK WHERE TITLE =" + DECK_TITLE_PLACEHOLDER + " )" + ","
            + "(SELECT ID FROM CARD WHERE TITLE =" + CARD_TITLE_PLACEHOLDER + " )" + ");";

    public void saveDeck(List<String> deckList) {
        String deckTitle = deckList.get(0).split(" ", 2 )[1];
        try {
            saveDeckToSql(deckTitle);
            Thread.sleep(2000); // for different flyway migration version
            saveDeckCardsCorrelationToSql(deckList, deckTitle);
        } catch (IOException | InterruptedException e) {
            log.info("Couldn't write {}, to file", deckTitle);
            e.printStackTrace();
        }
    }

    private void saveDeckCardsCorrelationToSql(List<String> deckList, String deckTitle) throws IOException {
        String fileName = createDeckCardsCorrelationFileName(deckTitle);
        FileUtils.writeStringToFile(
                new File(fileName),
                createDeckCardsCorrelationSql(deckList, deckTitle),
                Charset.defaultCharset(),
                true);
    }

    private void saveDeckToSql(String title) throws IOException {
        String fileName = createDeckFileName(title);
        FileUtils.writeStringToFile(
                new File(fileName),
                createDeckSql(title),
                Charset.defaultCharset(),
                true);
    }

    private String createDeckFileName(String title) {
        return createSqlFilePrefix()
                + "__insert_"
                + title
                .toLowerCase()
                .replaceAll("[^a-z]", "_")
                .replaceAll("[_]{2,}", "_")
                + "_into_deck.sql";
    }

    private String createDeckCardsCorrelationFileName(String title) {
        return createSqlFilePrefix()
                + "__insert_"
                + title
                .toLowerCase()
                .replaceAll("[^a-z]", "_")
                .replaceAll("[_]{2,}", "_")
                + "_correlation_into_deck_cards.sql";
    }

    private String createDeckSql(String title) {
        String sql = INSERT_INTO_DECK;
        sql = sql.replace(ID_PLACEHOLDER, "SEQ_CARD.NEXTVAL");
        sql = sql.replace(TITLE_PLACEHOLDER, wrap(title));
        return sql;
    }

    private String createDeckCardsCorrelationSql(List<String> deckList, String deckTitle) {
        return deckList.stream()
                .map(card -> addCardToCorrelationSql(card, deckTitle))
                .collect(Collectors.joining());
    }

    private String addCardToCorrelationSql(String cardInfo, String title) {
        int cardCount = Integer.parseInt(cardInfo.split(" ", 2 )[0]);
        String cardTitle = cardInfo.split(" ", 2 )[1];
        String sql = "";
        for (int i = 0; i < cardCount; i++) {
            sql = sql
                    .concat(INSERT_INTO_DECK_CARDS
                    .replace(DECK_TITLE_PLACEHOLDER, wrap(title))
                    .replace(CARD_TITLE_PLACEHOLDER, wrap(cardTitle)))
                    .concat(System.lineSeparator());
        }
        return sql;
    }
}
