package com.epitech.wepleb.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.epitech.wepleb.R;
import com.epitech.wepleb.fragments.BaseFragment;
import com.epitech.wepleb.fragments.ContactsFragment;
import com.epitech.wepleb.fragments.ProfileFragment;
import com.parse.ParseUser;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String MESSAGES_FRAGMENT_TAG = "MESSAGES_FRAGMENT_TAG";
    private static final String CONTACTS_FRAGMENT_TAG = "CONTACTS_FRAGMENT_TAG";
    private static final String PROFILE_FRAGMENT_TAG = "PROFILE_FRAGMENT_TAG";


    //private TextView mUsernameText;
    private Toolbar toolbar;
    private View mContentView;
    private View mMessagesButton;
    private View mContactsButton;
    private View mProfileButton;

    private ParseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        mContentView = findViewById(R.id.activity_main_container);
        mMessagesButton = findViewById(R.id.activity_main_tab_messages);
        mContactsButton = findViewById(R.id.activity_main_tab_contacts);
        mProfileButton = findViewById(R.id.activity_main_tab_profile);

        mUser = ParseUser.getCurrentUser();
        mMessagesButton.setOnClickListener(this);
        mContactsButton.setOnClickListener(this);
        mProfileButton.setOnClickListener(this);
    }

    /*
    public void populateView() {
        mUsernameText.setText(mUser.getUsername());
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.action_add:
                Toast.makeText(this, "Disponible prochainement", Toast.LENGTH_SHORT)
                        .show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_main_tab_messages:
                // TODO Set title
                placeFragment(MESSAGES_FRAGMENT_TAG);
                break;
            case R.id.activity_main_tab_contacts:
                // TODO Set title
                placeFragment(CONTACTS_FRAGMENT_TAG);
                break;
            case R.id.activity_main_tab_profile:
                // TODO Set title
                placeFragment(PROFILE_FRAGMENT_TAG);
                break;
        }
    }

    public void placeFragment(final String fragmentTag) {
        BaseFragment fragment = (BaseFragment)getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment == null) {
            switch (fragmentTag) {
                case MESSAGES_FRAGMENT_TAG:
                    //fragment = MessagesFragment.newInstance();
                    Toast.makeText(MainActivity.this, "Bientot disponible !", Toast.LENGTH_SHORT).show();
                    break;
                case CONTACTS_FRAGMENT_TAG:
                    fragment = ContactsFragment.newInstance();
                    break;
                case PROFILE_FRAGMENT_TAG:
                    fragment = ProfileFragment.newInstance();
                    break;
                default:
                    break;
            }
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(mContentView.getId(), fragment, fragmentTag)
                    .commitAllowingStateLoss();
        }
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
