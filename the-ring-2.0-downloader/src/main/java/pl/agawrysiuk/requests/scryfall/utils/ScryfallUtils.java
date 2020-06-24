package pl.agawrysiuk.requests.scryfall.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

import static pl.agawrysiuk.requests.scryfall.utils.Field.*;

@UtilityClass
public class ScryfallUtils {

    public boolean hasMore(String json) {
        JSONObject jsonObject = new JSONObject(json);
        return checkError(jsonObject)
                ? false
                : jsonObject.getBoolean(HAS_MORE);
    }

    public String getNextPage(String json) {
        return new JSONObject(json).getString(NEXT_PAGE);
    }

    public boolean checkError(JSONObject jsonObject) {
        return jsonObject.getString(OBJECT).equals(ERROR);
    }

    public String getId(String json) {
        return new JSONObject(json).getString(ID);
    }

    public String getName(String json) {
        return new JSONObject(json).getString(NAME);
    }

    public String getSetName(String json) {
        return new JSONObject(json).getString(SET_NAME);
    }

    public String encodeImagesInJson(String json) throws IOException {
        JSONObject full = new JSONObject(json);
        JSONObject imageList = full.getJSONObject(IMAGE_LIST);
        imageList
                .put(IMAGE_SMALL, encodeImage(imageList.getString(IMAGE_SMALL)))
                .put(IMAGE_NORMAL, encodeImage(imageList.getString(IMAGE_NORMAL)))
                .put(IMAGE_LARGE, encodeImage(imageList.getString(IMAGE_LARGE)))
                .put(IMAGE_PNG, encodeImage(imageList.getString(IMAGE_PNG)))
                .put(IMAGE_ART_CROP, encodeImage(imageList.getString(IMAGE_ART_CROP)))
                .put(IMAGE_BORDER_CROP, encodeImage(imageList.getString(IMAGE_BORDER_CROP)));
        full.put(IMAGE_LIST, imageList);
        return full.toString();
    }

    private String encodeImage(String urlPath) throws IOException {
        File file = new File("temporary");
        FileUtils.copyURLToFile(new URL(urlPath),file);
        byte[] fileContent = FileUtils.readFileToByteArray(file);
        FileUtils.deleteQuietly(file);
        return Base64.getEncoder().encodeToString(fileContent);
    }
}
