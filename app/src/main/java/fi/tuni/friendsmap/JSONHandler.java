package fi.tuni.friendsmap;

import org.json.JSONException;
import org.json.JSONObject;

import fi.tuni.friendsmap.entity.User;

/**
 * Class thats functions are used when we are working with JSON.
 */
public class JSONHandler {

    /**
     * Gets an user entity as a JSON object that can be used for http requests.
     *
     * @param user User to be returned as JSONOBJECT.
     * @param includeId true if we want the JSONBJECT to contain the id of the user.
     * @return REturns the created JSONObject.
     */
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

    /**
     * Returns a JSONObject that can be used for
     * login to this application backend.
     *
     * @param loginDetails Login details as string, aka username.
     * @return The created JSONObject, if something wrong returns null.
     */
    public JSONObject getLoginDetailsSignupJSON(String loginDetails) {
        JSONObject outputObj = new JSONObject();
        try {
            outputObj.put("username", loginDetails);
            return outputObj;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
