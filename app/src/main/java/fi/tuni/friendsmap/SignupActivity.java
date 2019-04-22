package fi.tuni.friendsmap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import fi.tuni.friendsmap.entity.User;
import fi.tuni.friendsmap.entity.UserLocation;

/**
 * Signup activity for FriendsMap, where user is able to
 * create an account.
 */
public class SignupActivity extends AppCompatActivity {

    /* UI references */
    private AutoCompleteTextView usernameField;

    /**
     * HttpHandler used by this activity.
     */
    private HttpHandler httpHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        httpHandler = new HttpHandler(this);

        usernameField = (AutoCompleteTextView) findViewById(R.id.username);
    }

    /**
     * Creates an account by making a http request if
     * everything is valid.
     *
     * @param
     * @throws JSONException
     */
    public void signupBtnClicked(View v) throws JSONException {
        String username = usernameField.getText().toString();

        boolean cancel = false;

        if(!isUserNameValid(username)) {
            usernameField.setError("Username must be minimum of 4 characters long!");
            cancel = true;
        }

        if(!cancel) {
            User signupUser = new User(-1, username, new UserLocation(-1000, -1000, ""));

            httpHandler.signUp(signupUser, new HttpHandler.VolleyCallBackWithParams<JSONObject>() {
                @Override
                public void onSuccess(JSONObject responseType) {
                    Toast.makeText(SignupActivity.this, "Signup succesful!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(VolleyError error) {
                    error.printStackTrace();
                    if(error.networkResponse.statusCode == 409) {
                        Toast.makeText(SignupActivity.this, "This username is already in use! Try another one.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * Switches activty to login.
     * @param v
     */
    public void goToLoginBtnClicked(View v) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public boolean isUserNameValid(String username) {
        return username.length() > 3;
    }
}
