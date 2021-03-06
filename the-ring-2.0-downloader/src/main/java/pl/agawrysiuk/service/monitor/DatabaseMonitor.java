package pl.agawrysiuk.service.monitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import pl.agawrysiuk.requests.internal.InternalRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
public class DatabaseMonitor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> checkExistingCardsMatch(List<String> requestList) {
        try {
            String pages = InternalRequest.getCards(requestList);
            return map(pages);
        } catch (IOException | InterruptedException e) {
            log.info("A problem occurred while downloading cards from database. Downloading stopped.");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<String> map(String toMap) {
        try {
            return objectMapper.readValue(toMap, new TypeReference<List<String>>(){});
        } catch (IOException e) {
            log.info("A problem occurred while parsing {} to Card.class", toMap);
            throw new RuntimeException(e);
        }
    }
}
