package pl.agawrysiuk.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.scryfall.utils.ScryfallUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

@Slf4j
public class CardSaver {

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

    private final String fileName = createFileName();

    private String createFileName() {
        LocalDateTime now = LocalDateTime.now();
        return "V1_"
                + now.getYear()
                + addLeadingZerosIfNeeded(String.valueOf(now.getMonthValue()))
                + addLeadingZerosIfNeeded(String.valueOf(now.getDayOfMonth()))
                + addLeadingZerosIfNeeded(String.valueOf(now.getHour()))
                + addLeadingZerosIfNeeded(String.valueOf(now.getMinute()))
                + addLeadingZerosIfNeeded(String.valueOf(now.getSecond()))
                + "__insert_into_cards.sql";
    }

    private String addLeadingZerosIfNeeded(String string) {
        return string.length() == 1 ? "0".concat(string) : string;
    }

    public void saveToSql(CardDto selected) {
        try {
            saveCard(selected);
            log.info("Saved {} to sql file", selected.getTitle());
        } catch (IOException e) {
            log.info("Couldn't write {}, to file", selected.getTitle());
            e.printStackTrace();
        }
    }

    private void saveCard(CardDto selected) throws IOException {
        FileUtils.writeStringToFile(
                new File(fileName),
                createCardSql(selected),
                Charset.defaultCharset(),
                true);
    }

    private String createCardSql(CardDto selected) {
        String sql = INSERT_INTO_CARD;
        sql = sql.replace(ID_PLACEHOLDER, "SEQ_CARD.NEXTVAL");
        sql = sql.replace(SCRYFALL_ID_PLACEHOLDER, wrap(ScryfallUtils.getId(selected.getJson())));
        sql = sql.replace(TITLE_PLACEHOLDER, wrap(selected.getTitle()));
        sql = sql.replace(SET_PLACEHOLDER, wrap(ScryfallUtils.getId(selected.getJson())));
        sql = sql.replace(JSON_PLACEHOLDER, wrap(selected.getJson())); // todo add image serialize
        sql = sql.concat(System.lineSeparator());
        return sql;
    }

    private String wrap(String string) {
        return "'" + string.replace("'","''") + "'";
    }
}
