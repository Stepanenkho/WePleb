package com.epitech.wepleb.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.epitech.wepleb.R;
import com.epitech.wepleb.fragments.BaseFragment;
import com.epitech.wepleb.fragments.ContactsFragment;
import com.epitech.wepleb.fragments.MessagesFragment;
import com.epitech.wepleb.fragments.ProfileFragment;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String MESSAGES_FRAGMENT_TAG = "MESSAGES_FRAGMENT_TAG";
    private static final String CONTACTS_FRAGMENT_TAG = "CONTACTS_FRAGMENT_TAG";
    private static final String PROFILE_FRAGMENT_TAG = "PROFILE_FRAGMENT_TAG";

    private Context mContext;

    private Toolbar toolbar;
    private View mContentView;
    private RadioButton mMessagesButton;
    private RadioButton mContactsButton;
    private RadioButton mProfileButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
        setSupportActionBar(toolbar);
        mContentView = findViewById(R.id.activity_main_container);
        mMessagesButton = (RadioButton) findViewById(R.id.activity_main_tab_messages);
        mContactsButton = (RadioButton) findViewById(R.id.activity_main_tab_contacts);
        mProfileButton = (RadioButton) findViewById(R.id.activity_main_tab_profile);

        mMessagesButton.setOnClickListener(this);
        mContactsButton.setOnClickListener(this);
        mProfileButton.setOnClickListener(this);

        saveInstallation();
        placeFragment(MESSAGES_FRAGMENT_TAG);
    }

    /*
    public void populateView() {
        mUsernameText.setText(mUser.getUsername());
    }
    */

    private void saveInstallation() {
        if (ParseUser.getCurrentUser() != null) {
            ParseObject parseInstallation = ParseInstallation.getCurrentInstallation();
            parseInstallation.put("user", ParseUser.getCurrentUser());
            parseInstallation.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

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
                if (mContext != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    LayoutInflater inflater = getLayoutInflater();
                    View view = inflater.inflate(R.layout.dialog_profile_username, null);
                    TextView title = (TextView) view.findViewById(R.id.dialog_profile_title);
                    title.setText("Ajouter un contact");
                    final EditText mUsernameText = (EditText) view.findViewById(R.id.dialog_profile_username);
                    builder.setView(view)
                            .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ParseQuery<ParseUser> userQuery = new ParseQuery<ParseUser>("_User");
                                    String mUsername = mUsernameText.getText().toString();
                                    userQuery.whereEqualTo("username", mUsername);

                                    userQuery.findInBackground(new FindCallback<ParseUser>() {
                                        @Override
                                        public void done(List<ParseUser> objects, ParseException e) {
                                            if (e != null) {
                                                Toast.makeText(mContext, "Pas de pleb trouvé", Toast.LENGTH_SHORT).show();
                                            } else if (objects != null && objects.size() > 0) {
                                                final ParseUser user = objects.get(0);
                                                // Check if contact already exist
                                                ParseQuery<ParseUser> tmpQuery = new ParseQuery<ParseUser>("Contacts");
                                                tmpQuery.whereEqualTo("user2", user);
                                                tmpQuery.whereEqualTo("user1", ParseUser.getCurrentUser());
                                                tmpQuery.findInBackground(new FindCallback<ParseUser>() {
                                                    @Override
                                                    public void done(List<ParseUser> objects, ParseException e) {
                                                        if (e != null)
                                                            e.printStackTrace();
                                                        else {
                                                            if (objects.size() != 0)
                                                                Toast.makeText(mContext, "Le pleb est déjà dans votre liste de contacts", Toast.LENGTH_SHORT).show();
                                                            else {
                                                                // Contact does not exist yet
                                                                ParseObject newContact = ParseObject.create("Contacts");
                                                                newContact.put("user1", ParseUser.getCurrentUser());
                                                                newContact.put("user2", user);
                                                                newContact.saveInBackground(new SaveCallback() {
                                                                    @Override
                                                                    public void done(ParseException e) {
                                                                        if (e != null)
                                                                            e.printStackTrace();
                                                                        else
                                                                            Snackbar.make(findViewById(android.R.id.content), "Contact ajouté", Snackbar.LENGTH_LONG)
                                                                                    .setActionTextColor(Color.RED)
                                                                                    .show();
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            })
                            .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .create()
                            .show();
                }
                break;
            case R.id.action_qrcode_add:
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setPrompt("Scanner pour ajouter un amis !");
                integrator.setOrientationLocked(false);
                integrator.setBeepEnabled(false);
                integrator.initiateScan();
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
                toolbar.setTitle("Messages");
                placeFragment(MESSAGES_FRAGMENT_TAG);
                break;
            case R.id.activity_main_tab_contacts:
                toolbar.setTitle("Contacts");
                placeFragment(CONTACTS_FRAGMENT_TAG);
                break;
            case R.id.activity_main_tab_profile:
                toolbar.setTitle("Profil");
                placeFragment(PROFILE_FRAGMENT_TAG);
                break;
        }
    }

    public void placeFragment(final String fragmentTag) {
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (fragment == null) {
            switch (fragmentTag) {
                case MESSAGES_FRAGMENT_TAG:
                    fragment = MessagesFragment.newInstance();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
                profileIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                profileIntent.putExtra(ChatActivity.EXTRA_PROFILE_ID, result.getContents());
                startActivity(profileIntent);
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
