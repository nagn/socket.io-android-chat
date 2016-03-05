package com.github.nkzawa.socketio.androidchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A login screen that offers login via username.
 */
public class LoginActivity extends Activity {

    private EditText mUsernameView;
    private EditText mPartynameView;

    private String mUsername;
    private String mPartyname;

    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();

        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username_input);
        mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptJoin();
                    return true;
                }
                return false;
            }
        });

        // Set up party form
        mPartynameView = (EditText) findViewById(R.id.party_id_input);

        Button joinButton = (Button) findViewById(R.id.sign_in_button);
        joinButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptJoin();
            }
        });

        Button hostButton = (Button) findViewById(R.id.host_button);
        hostButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptHost();
            }
        });
        mSocket.on("host error", onHostError);
        mSocket.on("host", onHost);
        mSocket.on("join", onJoin);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSocket.off("host error", onHostError);
        mSocket.off("host", onHost);
        mSocket.off("join", onJoin);
    }

    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptJoin() {
        // Reset errors.
        mUsernameView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();
        String partyname = mPartynameView.getText().toString().trim();

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            return;
        }

        // Check for a valid partyname.
        if (TextUtils.isEmpty(partyname)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mPartynameView.setError(getString(R.string.error_field_required));
            mPartynameView.requestFocus();
            return;
        }

        mUsername = username;
        mPartyname = partyname;

        // perform the user login attempt.
        mSocket.emit("join party", mPartyname);
    }

    private void attemptHost() {
        // Reset errors.
        mUsernameView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString().trim();
        String partyname = mPartynameView.getText().toString().trim();

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mUsernameView.setError(getString(R.string.error_field_required));
            mUsernameView.requestFocus();
            return;
        }

        // Check for a valid partyname.
        if (TextUtils.isEmpty(partyname)) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mPartynameView.setError(getString(R.string.error_field_required));
            mPartynameView.requestFocus();
            return;
        }

        mUsername = username;
        mPartyname = partyname;

        // perform the user login attempt.
        mSocket.emit("create party", partyname);
    }

    private Emitter.Listener onJoin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("username", mUsername);
            intent.putExtra("numUsers", numUsers);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private Emitter.Listener onHost = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("ONHOST DEBUG", "OnHost!");
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("username", "test");
            intent.putExtra("numUsers", numUsers);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private Emitter.Listener onHostError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("ONHOST DEBUG", "Error!!");
            JSONObject data = (JSONObject) args[0];

            String error;
            try {
                error = data.getString("error");
            } catch (JSONException e) {
                return;
            }
            switch (error) {
                case ("Party already in use"):
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPartynameView.setError(getString(R.string.error_field_party_in_use));
                            mPartynameView.requestFocus();
                        }
                    });
                    break;
                case ("Party does not exist"):
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPartynameView.setError(getString(R.string.error_field_party_does_not_exist));
                            mPartynameView.requestFocus();
                        }
                    });
                    break;
            }
        }
    };
}



