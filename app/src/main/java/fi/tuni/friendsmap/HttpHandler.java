package fi.tuni.friendsmap;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import fi.tuni.friendsmap.entity.User;
import fi.tuni.friendsmap.entity.UserLocation;

public class HttpHandler {

    private Context context;

    private RequestQueue requestQueue;

    private JSONHandler jsonHandler;

    private String baseUrl;

    public HttpHandler(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
        jsonHandler = new JSONHandler();
        baseUrl = context.getResources().getString(R.string.http_base_url);
    }

    public void signUp(User user, final VolleyCallBackWithParams<JSONObject> callBack) throws JSONException {
        JSONObject requestObject = jsonHandler.getJsonObjectFromUser(user, false);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.PUT,
                baseUrl + "users/", requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callBack.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.onError(error);
                    }
                });
        requestQueue.add(jsonObjReq);
    }

    public void updateUserAndItsLocation(User user) throws JSONException {
        JSONObject requestObject = jsonHandler.getJsonObjectFromUser(user, true);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                baseUrl + "users/", requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        System.out.println(response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });

        requestQueue.add(jsonObjReq);
    }

    public void deleteUsersLocation(User user, VolleyCallBackWithParams<JSONObject> volleyCallBackWithParams) {
        JSONObject requestObject = jsonHandler.getJsonObjectFromUser(user, true);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                baseUrl + "users/deletelocation", requestObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        volleyCallBackWithParams.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        volleyCallBackWithParams.onError(error);
                    }
                });

        requestQueue.add(jsonObjReq);
    }

    public void updateAllUsersAndLocations(List<User> usersList, final VolleyCallBack callBack) {

        JsonArrayRequest stringRequest = new JsonArrayRequest(
                Request.Method.GET,
                baseUrl + "users/",
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        System.out.println(response);

                        usersList.clear();
                        for(int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject responseObject = response.getJSONObject(i);

                                UserLocation userLocation = new UserLocation(
                                        responseObject.getDouble("latitude"),
                                        responseObject.getDouble("longitude"),
                                        responseObject.getString("locationInfo"));

                                User user = new User(
                                        responseObject.getLong("id"),
                                        responseObject.getString("username"),
                                        userLocation
                                );

                                usersList.add(user);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        /* Calling the callback function so we can do stuff after updating the user list */
                        callBack.onSuccess();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });

        requestQueue.add(stringRequest);
    }

    public interface VolleyCallBack {
        void onSuccess();
        void onError();
    }

    public interface VolleyCallBackWithParams<T> {
        void onSuccess(T responseType);
        void onError(VolleyError error);
    }
}
