package pl.agawrysiuk.requests;

import pl.agawrysiuk.request.Request;
import pl.agawrysiuk.requests.scryfall.utils.ScryfallUtils;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public abstract class ScryfallRequest extends Request {

    public static List<String> download(URI uri) throws IOException, InterruptedException {
        boolean hasMore = true;
        List<String> pages = new ArrayList<>();
        while (hasMore) {
            String response = getResponse(uri);
            pages.add(response);
            hasMore = ScryfallUtils.hasMore(response);
            if(hasMore) {
                uri = URI.create(ScryfallUtils.getNextPage(response));
            }
        }
        return pages;
    }
}
