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

public class SignupActivity extends AppCompatActivity {

    /* UI references */
    private AutoCompleteTextView usernameField;

    private HttpHandler httpHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        httpHandler = new HttpHandler(this);

        usernameField = (AutoCompleteTextView) findViewById(R.id.username);
    }

    public void signupBtnClicked(View v) throws JSONException {
        String username = usernameField.getText().toString();

        boolean cancel = false;

        if(!isUserNameValid(username)) {
            usernameField.setError("Username must be minimum of 4 characters long!");
            cancel = true;
        }

        if(!cancel) {
            User signupUser = new User(-1, username, new UserLocation(-1, -1, ""));

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

    public void goToLoginBtnClicked(View v) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    public boolean isUserNameValid(String username) {
        return username.length() > 3;
    }
}
