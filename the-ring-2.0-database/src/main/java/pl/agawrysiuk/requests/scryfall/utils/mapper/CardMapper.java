package pl.agawrysiuk.requests.scryfall.utils.mapper;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.agawrysiuk.dto.CardDto;
import pl.agawrysiuk.requests.scryfall.utils.ScryfallUtils;
import pl.agawrysiuk.requests.scryfall.utils.exception.CardDownloadException;

import java.util.ArrayList;
import java.util.List;

import static pl.agawrysiuk.requests.scryfall.utils.Field.*;

@Slf4j
public class CardMapper {

    private List<CardDto> cardList;

    public CardMapper() {
        this.cardList = new ArrayList<>();
    }

    public List<CardDto> map(List<String> jsonList) throws CardDownloadException {
        for(int i = 0; i < jsonList.size(); i ++) {
            mapJsonPageToCardList(jsonList.get(i));
        }
        return cardList;
    }

    private void mapJsonPageToCardList(String json) throws CardDownloadException {
        JSONObject downloaded = new JSONObject(json);
        if (ScryfallUtils.checkError(downloaded)) {
            throw new CardDownloadException(downloaded.getString(DETAILS));
        }
        createCardList(downloaded);
    }

    private void createCardList(JSONObject downloaded) {
        JSONArray array = downloaded.getJSONArray(DATA);
        for (int i = 0; i < array.length(); i++) {
            if(array.getJSONObject(i).get(OBJECT).equals(CARD_OBJECT)) {
                try {
                    cardList.add(buildCard(array.getJSONObject(i)));
                    log.info("Downloaded card {}", downloaded.getString(NAME));
                } catch (JSONException e) {
                    log.info("Card image not found for {}", array.getJSONObject(i).toString());
                }
            }
        }
    }

    private CardDto buildCard(JSONObject jsonObject) throws JSONException {
        return CardDto.builder()
                .title(jsonObject.getString(NAME))
                .setTitle(jsonObject.getString(SET_NAME))
                .image(jsonObject.getJSONObject(IMAGE_LIST).getString(IMAGE_NORMAL))
                .json(jsonObject.toString())
                .build();
    }
}
