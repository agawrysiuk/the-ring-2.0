package pl.agawrysiuk.scryfall.utils;

public class CardDownloadException extends Exception {

    public CardDownloadException(String message) {
        super("Something went wrong while downloading a card.");
    }
}
