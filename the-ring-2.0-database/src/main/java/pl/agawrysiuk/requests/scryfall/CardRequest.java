package pl.agawrysiuk.requests.scryfall;

import lombok.experimental.UtilityClass;
import pl.agawrysiuk.requests.Request;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@UtilityClass
public class CardRequest extends Request {

    private final String SCRYFALL_URL_PREFIX = "https://api.scryfall.com/cards/search?q=";
    private final String SCRYFALL_URL_SUFFIX = "&unique=prints";

    public List<String> getCards(String cardname) throws IOException, InterruptedException {
        return download(URI.create(build(cardname)));
    }

    private String build(String cardName) {
        return SCRYFALL_URL_PREFIX.concat(cardName.replace(" ", "+")).concat(SCRYFALL_URL_SUFFIX);
    }
}
