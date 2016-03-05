package com.epitech.wepleb.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.epitech.wepleb.R;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText mUsernameField;
    private EditText mPasswordField;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsernameField = (EditText) findViewById(R.id.activity_login_username);
        mPasswordField = (EditText) findViewById(R.id.activity_login_password);
        mLoginButton = (Button) findViewById(R.id.activity_login_button);

        mLoginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_login_button:
                final String username = mUsernameField.getText().toString();
                final String password = mPasswordField.getText().toString();

                loginWithCredentials(username, password);
                break;
        }
    }

    public void loginWithCredentials(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Toast.makeText(LoginActivity.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                } else {
                    startMainActivity();
                }
            }
        });
    }

    public void startMainActivity() {
        final Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(mainIntent);
        finishAffinity();
    }
}
