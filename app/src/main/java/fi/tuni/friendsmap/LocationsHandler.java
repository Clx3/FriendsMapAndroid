package fi.tuni.friendsmap;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.tuni.friendsmap.entity.User;
import fi.tuni.friendsmap.entity.UserLocation;

public class LocationsHandler {

    private Context context;

    private HttpHandler httpHandler;

    private List<User> usersAndLocationsList;


    public LocationsHandler(Context context, HttpHandler httpHandler) {
        this.context = context;
        this.httpHandler = httpHandler;
        usersAndLocationsList = new ArrayList<>();
    }

    public JSONObject getUserAndLocationAsJson(User user) {
        JSONObject outputObj = new JSONObject();
        return null;
    }

    public void updateAllUsersAndLocations(HttpHandler.VolleyCallBack callBack) {
        httpHandler.updateAllUsersAndLocations(usersAndLocationsList, callBack);
    }

    public void setMainUsersLocation() {}

    public void deleteUsersLocation(User user) {
        httpHandler.deleteUsersLocation(user, new HttpHandler.VolleyCallBackWithParams<JSONObject>() {
            @Override
            public void onSuccess(JSONObject response) {

                user.setLocation(new UserLocation(-1000, -1000, ""));

                Toast.makeText(context, "Location deleted succesfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VolleyError error) {
                Toast.makeText(context, "Location deletion failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public List<User> getUsersAndLocationsList() {
        return usersAndLocationsList;
    }

    public void setUsersAndLocationsList(List<User> usersAndLocationsList) {
        this.usersAndLocationsList = usersAndLocationsList;
    }
}
