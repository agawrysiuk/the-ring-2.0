package pl.agawrysiuk.service.saver;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.scryfall.utils.ScryfallUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class CardSaver extends Saver {

    private final String ID_PLACEHOLDER = "%ID%";
    private final String SCRYFALL_ID_PLACEHOLDER = "%SCRYFALL_ID%";
    private final String TITLE_PLACEHOLDER = "%TITLE%";
    private final String SET_PLACEHOLDER = "%SET%";
    private final String JSON_PLACEHOLDER = "%JSON%";

    private final String INSERT_INTO_CARD = "INSERT INTO CARD VALUES("
            + ID_PLACEHOLDER + ","
            + SCRYFALL_ID_PLACEHOLDER + ","
            + TITLE_PLACEHOLDER + ","
            + SET_PLACEHOLDER + ","
            + JSON_PLACEHOLDER + ");";

    public void saveToSql(CardDto toSave) {
        try {
            saveCard(toSave);
            log.info("Saved {} to sql file", toSave.getTitle());
        } catch (IOException e) {
            log.info("Couldn't write {}, to file", toSave.getTitle());
            e.printStackTrace();
        }
    }

    private void saveCard(CardDto selected) throws IOException {
        String fileName = createFileName(selected.getTitle());
        FileUtils.writeStringToFile(
                new File(fileName),
                createCardSql(selected),
                Charset.defaultCharset(),
                true);
    }

    private String createFileName(String cardName) {
        return createSqlFilePrefix()
                + "__insert_"
                + cardName
                    .toLowerCase()
                    .replaceAll("[^a-z]", "_")
                    .replaceAll("[_]{2,}", "_")
                + "_into_cards.sql";
    }

    private String createCardSql(CardDto selected) throws IOException {
        String sql = INSERT_INTO_CARD;
        sql = sql.replace(ID_PLACEHOLDER, "SEQ_CARD.NEXTVAL");
        sql = sql.replace(SCRYFALL_ID_PLACEHOLDER, wrap(ScryfallUtils.getId(selected.getJson())));
        sql = sql.replace(TITLE_PLACEHOLDER, wrap(selected.getTitle()));
        sql = sql.replace(SET_PLACEHOLDER, wrap(ScryfallUtils.getSetName(selected.getJson())));
        sql = sql.replace(JSON_PLACEHOLDER, wrap(ScryfallUtils.encodeImagesInJson(selected.getJson())));
        sql = sql.concat(System.lineSeparator());
        return sql;
    }

    private String wrap(String string) {
        return "'" + string.replace("'", "''") + "'";
    }
}
