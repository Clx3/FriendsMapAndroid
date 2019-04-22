package fi.tuni.friendsmap;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Login activity for FriendsMap application.
 */
public class LoginActivity extends AppCompatActivity  {

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private View mProgressView;
    private View mLoginFormView;

    /**
     * RequestQueue for making HTTP requests.
     */
    protected RequestQueue requestQueue;

    /**
     * JSONHandler instance used by this activity.
     */
    private JSONHandler jsonHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        jsonHandler = new JSONHandler();

        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        requestQueue = Volley.newRequestQueue(this);
    }

    /**
     * Called when the go to signup button is clicked to
     * go to the signup page.
     *
     * @param v
     */
    public void goToSignupBtnClicked(View v) {
        startActivity(new Intent(this, SignupActivity.class));
    }

    /**
     * Attempts login to the Friends map application.
     * First it checks that the form is filled correctly, and
     * if so it makes a http request for the backend and if it is
     * successfull, we go to the main activity of friendsmap.
     */
    private void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_too_short_username));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            String baseUrl = getResources().getString(R.string.http_base_url);

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                    baseUrl + "users/login/", jsonHandler.getLoginDetailsSignupJSON(mUsernameView.getText().toString()),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println(response.toString());
                            LoginActivity.this.showProgress(false);

                            try {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.putExtra("userId", response.getLong("id"));
                                intent.putExtra("username", response.getString("username"));
                                intent.putExtra("latitude", response.getDouble("latitude"));
                                intent.putExtra("longitude", response.getDouble("longitude"));
                                intent.putExtra("locationInfo", response.getString("locationInfo"));
                                startActivity(intent);
                            } catch (Exception e) {
                                Toast.makeText(LoginActivity.this, "Critical error.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            LoginActivity.this.showProgress(false);
                            LoginActivity.this.mUsernameView.setError("Invalid username");
                        }
                    });

            requestQueue.add(jsonObjReq);
        }
    }

    private boolean isUsernameValid(String username) {
        return username.length() > 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}

