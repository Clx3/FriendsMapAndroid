package fi.tuni.friendsmap;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import fi.tuni.friendsmap.entity.User;

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

    public void updateUserAndItsLocation(User user) throws JSONException {
        JSONObject requestObject = jsonHandler.getJsonObjectFromUser(user);
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
}
