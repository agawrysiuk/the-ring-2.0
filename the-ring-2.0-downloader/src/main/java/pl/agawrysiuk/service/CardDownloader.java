package pl.agawrysiuk.service;

import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.scryfall.CardRequest;
import pl.agawrysiuk.scryfall.utils.exception.CardDownloadException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CardDownloader {

    public List<CardDto> downloadCard(String cardName) {
        List<CardDto> list;
        try {
            list = CardRequest.sendRequest(cardName);
        } catch (IOException | InterruptedException | CardDownloadException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return list;
    }
}
