package pl.agawrysiuk.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import pl.agawrysiuk.dto.SetDto;
import pl.agawrysiuk.scryfall.utils.ScryfallUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@Slf4j
public class SetSaver extends Saver {

    private final String ID_PLACEHOLDER = "%ID%";
    private final String SCRYFALL_ID_PLACEHOLDER = "%SCRYFALL_ID%";
    private final String CODE_PLACEHOLDER = "%CODE%";
    private final String TITLE_PLACEHOLDER = "%TITLE%";
    private final String JSON_PLACEHOLDER = "%JSON%";

    private final String INSERT_INTO_SET = "INSERT INTO SET VALUES("
            + ID_PLACEHOLDER + ","
            + SCRYFALL_ID_PLACEHOLDER + ","
            + CODE_PLACEHOLDER + ","
            + TITLE_PLACEHOLDER + ","
            + JSON_PLACEHOLDER + ");";

    public void saveToSql(List<SetDto> downloadedSets) {
        try {
            saveSets(downloadedSets);
            log.info("Saved downloaded sets to sql file.");
        } catch (IOException e) {
            log.info("Couldn't write downloaded sets to file.");
            e.printStackTrace();
        }
    }

    private void saveSets(List<SetDto> downloadedSets) throws IOException {
        FileUtils.writeStringToFile(
                new File(createFileName()),
                createSetsSql(downloadedSets),
                Charset.defaultCharset(),
                true);
    }

    private String createFileName() {
        return createSqlFilePrefix() + "__insert_into_set.sql";
    }

    private String createSetsSql(List<SetDto> selected) {
        String fullSql = "";
        for (SetDto set : selected) {
            String sql = INSERT_INTO_SET;
            sql = sql.replace(ID_PLACEHOLDER, "SEQ_SET.NEXTVAL");
            sql = sql.replace(SCRYFALL_ID_PLACEHOLDER, wrap(ScryfallUtils.getId(set.getJson())));
            sql = sql.replace(CODE_PLACEHOLDER, wrap(set.getCode()));
            sql = sql.replace(TITLE_PLACEHOLDER, wrap(set.getTitle()));
            sql = sql.replace(JSON_PLACEHOLDER, wrap(set.getJson()));
            sql = sql.concat(System.lineSeparator());
            fullSql = fullSql.concat(sql);
        }
        return fullSql;
    }

    private String wrap(String string) {
        return "'" + string.replace("'", "''") + "'";
    }
}
