package pl.agawrysiuk.database;

import lombok.experimental.UtilityClass;
import pl.agawrysiuk.request.Request;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@UtilityClass
public class DatabaseUtils extends Request {

    private final String DATABASE_HOST = "http://localhost:8090/cards";

    public String getDatabaseCards() throws InterruptedException, IOException, URISyntaxException {
        return getResponse(new URI(DATABASE_HOST));
    }
}
