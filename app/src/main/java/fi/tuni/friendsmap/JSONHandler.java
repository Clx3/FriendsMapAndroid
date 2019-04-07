package fi.tuni.friendsmap;

import org.json.JSONException;
import org.json.JSONObject;

import fi.tuni.friendsmap.entity.User;

public class JSONHandler {

    public JSONObject getJsonObjectFromUser(User user, boolean includeId) {
        JSONObject outputObject = new JSONObject();

        try {
            if(includeId)
                outputObject.put("id", user.getUserId());

            outputObject.put("username", user.getUsername());
            outputObject.put("latitude", user.getLocation().getLatitude());
            outputObject.put("longitude", user.getLocation().getLongitude());
            outputObject.put("locationInfo", "Meh");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return outputObject;
    }

}
