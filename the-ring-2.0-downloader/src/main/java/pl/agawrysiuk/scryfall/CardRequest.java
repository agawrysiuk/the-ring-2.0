package pl.agawrysiuk.scryfall;

import lombok.experimental.UtilityClass;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.scryfall.utils.CardDownloadException;
import pl.agawrysiuk.scryfall.utils.ResponseMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@UtilityClass
public class CardRequest {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    private static final String SCRYFALL_URL_PREFIX = "https://api.scryfall.com/cards/search?q=";
    private static final String SCRYFALL_URL_SUFFIX = "&unique=prints";

    public List<CardDto> send(String cardname) throws IOException, InterruptedException, CardDownloadException {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(build(cardname)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return ResponseMapper.map(response.body());
    }

    private String build(String cardName) {
        return SCRYFALL_URL_PREFIX.concat(cardName.replace(" ", "+")).concat(SCRYFALL_URL_SUFFIX);
    }
}
