package pl.agawrysiuk.service;

import pl.agawrysiuk.dto.CardDto;

import java.util.Collections;
import java.util.List;

public class CardDownloader {

    private static final String SCRYFALL_URL_PREFIX = "https://api.scryfall.com/cards/search?q=";
    private static final String SCRYFALL_URL_SUFFIX = "&unique=prints";

    public List<CardDto> downloadCards() {
        //todo connect to scryfall, get the data, convert to carddto list
        return Collections.emptyList();
    }
}
