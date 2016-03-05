package com.epitech.wepleb.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.epitech.wepleb.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends BaseActivity implements View.OnClickListener {

    private EditText mUsernameField;
    private EditText mPasswordField;
    private EditText mPasswordConfirmField;
    private Button mSignupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mUsernameField = (EditText) findViewById(R.id.activity_signup_username);
        mPasswordField = (EditText) findViewById(R.id.activity_signup_password);
        mPasswordConfirmField = (EditText) findViewById(R.id.activity_signup_password_confirm);
        mSignupButton = (Button) findViewById(R.id.activity_signup_button);

        mSignupButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_signup_button:
                String username = mUsernameField.getText().toString();
                String password = mPasswordField.getText().toString();
                String passwordConfirmation = mPasswordConfirmField.getText().toString();

                if (username.length() > 0 &&checkPassword(password, passwordConfirmation))
                    signupWithCredentials(username, password);
                break;
        }
    }

    public boolean checkPassword(String password, String passwordConfirmation) {
        return password.equals(passwordConfirmation);
    }

    public void signupWithCredentials(String username, String password) {
        ParseUser newUser = new ParseUser();
        newUser.setUsername(username);
        newUser.setPassword(password);

        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    switch (e.getCode()) {
                        case ParseException.EMAIL_TAKEN:
                            Toast.makeText(SignupActivity.this, "Cette adresse email est déjà utilisé", Toast.LENGTH_SHORT).show();
                            break;
                        case ParseException.USERNAME_TAKEN:
                            Toast.makeText(SignupActivity.this, "Ce nom d'utilisateur est déjà utilisé", Toast.LENGTH_SHORT).show();
                            break;
                        case ParseException.INVALID_EMAIL_ADDRESS:
                            Toast.makeText(SignupActivity.this, "Cette adresse email n'est pas valide", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(SignupActivity.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                            break;
                    }
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
