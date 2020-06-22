package pl.agawrysiuk.app.elements.decks;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.dto.SetDto;
import pl.agawrysiuk.service.downloader.SetDownloader;
import pl.agawrysiuk.service.saver.SetSaver;

import java.util.List;

@Getter
@Slf4j
public class DeckController {

    public void searchAndSaveDeck(String deckInfo) {
        log.info("Checking for cards.");
//        SetDownloader downloader = new SetDownloader();
//        List<SetDto> downloadedSets = downloader.download();
//        SetSaver saver = new SetSaver();
//        saver.saveToSql(downloadedSets);
        log.info("Saving deck done.");
    }
}
