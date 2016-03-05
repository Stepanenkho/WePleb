package com.epitech.wepleb.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.epitech.wepleb.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class MainActivity extends BaseActivity {

    private TextView mUsernameText;

    private ParseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsernameText = (TextView) findViewById(R.id.activity_main_username);

        mUser = ParseUser.getCurrentUser();
        populateView();
        fetchContacts();
    }

    public void populateView() {
        mUsernameText.setText(mUser.getUsername());
    }

    public void fetchContacts() {
        /*
        ParseQuery<ParseUser> parseQuery = new ParseQuery<>("_User");
        parseQuery.whereEqualTo("username", mUser.getUsername());

        parseQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {

                } else if (objects != null && objects.size() > 0){
                    ParseUser user = objects.get(0);
                    mUsernameText.setText("Found " + user.getUsername());
                }
            }
        });
        */
    }
}
