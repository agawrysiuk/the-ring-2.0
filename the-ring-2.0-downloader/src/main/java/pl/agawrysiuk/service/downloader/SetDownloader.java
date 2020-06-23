package pl.agawrysiuk.service.downloader;

import pl.agawrysiuk.dto.SetDto;
import pl.agawrysiuk.requests.scryfall.SetRequest;
import pl.agawrysiuk.requests.scryfall.utils.exception.CardDownloadException;
import pl.agawrysiuk.requests.scryfall.utils.mapper.SetMapper;

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
