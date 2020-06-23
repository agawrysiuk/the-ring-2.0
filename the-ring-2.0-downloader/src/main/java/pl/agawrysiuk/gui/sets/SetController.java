package pl.agawrysiuk.gui.sets;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.dto.SetDto;
import pl.agawrysiuk.service.downloader.SetDownloader;
import pl.agawrysiuk.service.saver.SetSaver;

import java.util.List;

@Getter
@Slf4j
public class SetController {

    public void searchAndSaveToSqlAllSets() {
        log.info("Downloading all sets.");
        SetDownloader downloader = new SetDownloader();
        List<SetDto> downloadedSets = downloader.download();
        SetSaver saver = new SetSaver();
        saver.saveToSql(downloadedSets);
        log.info("Downloading sets done.");
    }
}
