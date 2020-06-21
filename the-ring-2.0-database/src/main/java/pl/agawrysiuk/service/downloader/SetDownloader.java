package pl.agawrysiuk.service.downloader;

import pl.agawrysiuk.dto.SetDto;
import pl.agawrysiuk.scryfall.SetRequest;
import pl.agawrysiuk.scryfall.utils.mapper.SetMapper;
import pl.agawrysiuk.scryfall.utils.exception.CardDownloadException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SetDownloader {

    public List<SetDto> download() {
        try {
            List<String> response = SetRequest.getSets();
            return new SetMapper().map(response);
        } catch (IOException | InterruptedException | CardDownloadException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
