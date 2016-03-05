package com.epitech.wepleb.activities;

import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseUser;

public class DispatcherActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ParseUser.getCurrentUser() != null) {
            startMainActivity();
        } else {
            startWelcomeActivity();
        }
    }

    public void startMainActivity() {
        final Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(mainIntent);
        finishAffinity();
    }

    public void startWelcomeActivity() {
        final Intent welcomeIntent = new Intent(this, WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(welcomeIntent);
        finishAffinity();
    }
}
