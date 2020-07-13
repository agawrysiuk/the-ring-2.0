package pl.agawrysiuk.display.utils;

import lombok.experimental.UtilityClass;
import org.json.JSONObject;
import pl.agawrysiuk.db.Database;

import java.util.Base64;

@UtilityClass
public class JSONObjectUtils {

    public byte[] getEncodedImageFromCardTitle(String title) {
        JSONObject jsonObject = new JSONObject(Database.getInstance().getNewDatabaseCards().get(title).getJson());
        return Base64.getDecoder().decode(jsonObject.getJSONObject("image_uris").getString("art_crop"));
    }
}
