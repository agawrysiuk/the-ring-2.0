package pl.agawrysiuk.database;

import lombok.experimental.UtilityClass;
import pl.agawrysiuk.request.Request;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@UtilityClass
public class DatabaseUtils extends Request {

    private final String CARDS_LINK = "http://localhost:8090/cards";
    private final String DECKS_LINK = "http://localhost:8090/decks";

    public String getDatabaseCards() throws InterruptedException, IOException, URISyntaxException {
        return getResponse(new URI(CARDS_LINK));
    }

    public String getDatabaseDecks() throws InterruptedException, IOException, URISyntaxException {
        return getResponse(new URI(DECKS_LINK));
    }

}
