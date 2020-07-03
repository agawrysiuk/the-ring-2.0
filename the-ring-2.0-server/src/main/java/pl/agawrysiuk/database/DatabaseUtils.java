package pl.agawrysiuk.database;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@UtilityClass
public class DatabaseUtils {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private final String DATABASE_HOST = "http://localhost:8090/cards";

    public String getDatabaseCards() throws InterruptedException, IOException, URISyntaxException {
        return getCards();
    }

    private String getCards() throws IOException, InterruptedException, URISyntaxException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(new URI(DATABASE_HOST))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
