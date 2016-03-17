package com.epitech.wepleb.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.epitech.wepleb.R;

public class WelcomeActivity extends BaseActivity implements View.OnClickListener {

    private Button mLoginButton;
    private Button mSignupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        mLoginButton = (Button) findViewById(R.id.activity_welcome_login);
        mSignupButton = (Button) findViewById(R.id.activity_welcome_signup);

        mLoginButton.setOnClickListener(this);
        mSignupButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_welcome_login:
                startLoginActivity();
                break;
            case R.id.activity_welcome_signup:
                startSignupActivity();
                break;
        }
    }

    public void startLoginActivity() {
        final Intent loginIntent = new Intent(this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(loginIntent);
    }

    public void startSignupActivity() {
        final Intent signupIntent = new Intent(this, SignupActivity.class);
        signupIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(signupIntent);
    }
}
