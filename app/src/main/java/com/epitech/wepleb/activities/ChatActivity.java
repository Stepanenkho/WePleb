package com.epitech.wepleb.activities;

import android.os.Bundle;

import com.epitech.wepleb.R;

public class ChatActivity extends BaseActivity {

    public static final String EXTRA_DICUSSION_ID = "EXTRA_DICUSSION_ID";
    public static final String EXTRA_PROFILE_ID = "EXTRA_PROFILE_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
    }
}
