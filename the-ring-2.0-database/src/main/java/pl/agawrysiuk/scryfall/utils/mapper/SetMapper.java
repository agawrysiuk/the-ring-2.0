package pl.agawrysiuk.scryfall.utils.mapper;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.agawrysiuk.dto.SetDto;
import pl.agawrysiuk.scryfall.utils.ScryfallUtils;
import pl.agawrysiuk.scryfall.utils.exception.CardDownloadException;

import java.util.ArrayList;
import java.util.List;

import static pl.agawrysiuk.scryfall.utils.Field.*;

@Slf4j
public class SetMapper {

    private List<SetDto> setList;

    public SetMapper() {
        this.setList = new ArrayList<>();
    }

    public List<SetDto> map(List<String> jsonList) throws CardDownloadException {
        for(int i = 0; i < jsonList.size(); i ++) {
            mapJsonPageToSetList(jsonList.get(i));
        }
        return setList;
    }

    private void mapJsonPageToSetList(String json) throws CardDownloadException {
        JSONObject downloaded = new JSONObject(json);
        if (ScryfallUtils.checkError(downloaded)) {
            throw new CardDownloadException(downloaded.getString(DETAILS));
        }
        createSetList(downloaded);
    }

    private void createSetList(JSONObject downloaded) {
        JSONArray array = downloaded.getJSONArray(DATA);
        for (int i = 0; i < array.length(); i++) {
            if(array.getJSONObject(i).get(OBJECT).equals(SET_OBJECT)) {
                try {
                    setList.add(buildSet(array.getJSONObject(i)));
                    log.info("Downloaded set {}", array.getJSONObject(i).getString(NAME));
                } catch (JSONException e) {
                    log.info("Something went wrong while parsing set {}", array.getJSONObject(i).toString());
                }
            }
        }
    }

    private SetDto buildSet(JSONObject jsonObject) throws JSONException {
        return SetDto.builder()
                .code(jsonObject.getString(CODE))
                .title(jsonObject.getString(NAME))
                .json(jsonObject.toString())
                .build();
    }
}
