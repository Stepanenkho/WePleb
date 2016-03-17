package com.epitech.wepleb.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.epitech.wepleb.R;
import com.epitech.wepleb.adapters.ParseRecyclerQueryAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by Eric on 16/03/2016.
 */
public class SearchActivity extends BaseActivity implements View.OnClickListener {

    private Context mContext;

    private Toolbar toolbar;

    private EditText mSearchBar;
    private ImageButton mBackButton;
    private Button mAddButton;

    private ProgressBar mProgressBar;
    private RecyclerView mContactsList;
    private ParseRecyclerQueryAdapter<ParseObject> mContactsAdapter;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mContext = getApplicationContext();
        toolbar = (Toolbar) findViewById(R.id.activity_search_toolbar);
        setSupportActionBar(toolbar);
        mSearchBar = (EditText) findViewById(R.id.search_bar);
        mBackButton = (ImageButton) findViewById(R.id.search_back);
        mAddButton = (Button) findViewById(R.id.add_button);
        mProgressBar = (ProgressBar) findViewById(R.id.activity_progress_bar);

        mUsername = "";

        mBackButton.setOnClickListener(this);
        mAddButton.setOnClickListener(this);
        mSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                mProgressBar.setVisibility(View.VISIBLE);
                mAddButton.setText("Chercher l'id WePleb de : " + s);
                mUsername = s.toString();
                mContactsAdapter.setQueryFactory(new ParseRecyclerQueryAdapter.QueryFactory() {
                    @Override
                    public ParseQuery create() {
                        final ParseQuery<ParseObject> contactsQuery = new ParseQuery<>("Contacts");
                        final ParseQuery<ParseObject> usersQuery = new ParseQuery<>("_User");
                        usersQuery.whereStartsWith("username", mUsername);
                        contactsQuery.whereMatchesQuery("user2", usersQuery);
                        contactsQuery.whereEqualTo("user1", ParseUser.getCurrentUser());
                        return contactsQuery;
                    }
                });
                mContactsAdapter.reload();
            }
        });

        mContactsList = (RecyclerView) findViewById(R.id.rv_contacts_list);
        mContactsList.setHasFixedSize(true);
        if (mContext != null) {
            mContactsAdapter = new ParseRecyclerQueryAdapter<ParseObject>(mContext, new ParseRecyclerQueryAdapter.QueryFactory() {
                @Override
                public ParseQuery create() {
                    final ParseQuery<ParseObject> contactsQuery = new ParseQuery<>("Contacts");
                    contactsQuery.whereEqualTo("user1", ParseUser.getCurrentUser());
                    return contactsQuery;
                }
            }) {

                class ContactViewHolder extends RecyclerView.ViewHolder {
                    TextView username;

                    ContactViewHolder(View itemView) {
                        super(itemView);
                        username = (TextView) itemView.findViewById(R.id.item_contact_username);
                    }
                }

                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    final View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_contact, null);
                    final ContactViewHolder viewHolder = new ContactViewHolder(itemView);
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ParseObject contact = getItem(viewHolder.getAdapterPosition());
                            ParseObject user = contact.getParseUser("user2");
                            startProfileActivity(user);
                        }
                    });
                    return viewHolder;
                }

                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                    final ContactViewHolder viewHolder = (ContactViewHolder) holder;
                    final ParseObject contact = getItem(position);
                    final ParseUser user = contact.getParseUser("user2");
                    try {
                        String username = user.fetchIfNeeded().getUsername();
                        viewHolder.username.setText(username);
                    } catch (com.parse.ParseException e) {
                        e.printStackTrace();
                    }
                }
            };

            mContactsAdapter.addOnQueryLoadListener(new ParseRecyclerQueryAdapter.OnQueryLoadListener<ParseObject>() {
                @Override
                public void onLoading(ParseRecyclerQueryAdapter adapter) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoaded(ParseRecyclerQueryAdapter adapter, List<ParseObject> objects, Exception e) {
                    mProgressBar.setVisibility(View.GONE);
                }
            });
            mContactsList.setAdapter(mContactsAdapter);
            mContactsList.setLayoutManager(new LinearLayoutManager(mContext));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_back:
                finish();
                break;
            case R.id.add_button:
                if (mContext != null) {
                    if (mUsername == "")
                        Toast.makeText(this, "Le champ de recherche ne doit être vide", Toast.LENGTH_SHORT).show();
                    else if (mUsername.equals(ParseUser.getCurrentUser().getUsername()))
                        Toast.makeText(this, "Vous ne pouvez vous ajouter vous-même", Toast.LENGTH_SHORT).show();
                    else {
                        final ParseQuery<ParseUser> userQuery = new ParseQuery<>("_User");
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
                                                            mContactsAdapter.setQueryFactory(new ParseRecyclerQueryAdapter.QueryFactory() {
                                                                @Override
                                                                public ParseQuery create() {
                                                                    final ParseQuery<ParseObject> contactsQuery = new ParseQuery<>("Contacts");
                                                                    final ParseQuery<ParseObject> usersQuery = new ParseQuery<>("_User");
                                                                    usersQuery.whereStartsWith("username", mUsername);
                                                                    contactsQuery.whereMatchesQuery("user2", usersQuery);
                                                                    contactsQuery.whereEqualTo("user1", ParseUser.getCurrentUser());
                                                                    return contactsQuery;
                                                                }
                                                            });
                                                            mContactsAdapter.reload();
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
                }
                break;
            default:
                break;
        }
    }

    private void startProfileActivity(ParseObject user) {
        if (mContext != null) {
            final Intent profileIntent = new Intent(mContext, ProfileActivity.class);
            profileIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            profileIntent.putExtra(ProfileActivity.EXTRA_PROFILE_ID, user.getObjectId());
            startActivity(profileIntent);
        }
    }
}
