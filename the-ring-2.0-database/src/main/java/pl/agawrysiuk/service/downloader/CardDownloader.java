package pl.agawrysiuk.service.downloader;

import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.scryfall.CardRequest;
import pl.agawrysiuk.scryfall.utils.mapper.CardMapper;
import pl.agawrysiuk.scryfall.utils.exception.CardDownloadException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CardDownloader {

    public List<CardDto> download(String cardName) {
        try {
            List<String> response = CardRequest.getCards(cardName);
            return new CardMapper().map(response);
        } catch (IOException | InterruptedException | CardDownloadException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
