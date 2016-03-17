package com.epitech.wepleb.activities;

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

    private Toolbar toolbar;

    private EditText mSearchBar;
    private ImageButton mBackButton;
    private Button mAddButton;

    private RecyclerView mContactsList;
    private ParseRecyclerQueryAdapter<ParseObject> mContactsAdapter;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        toolbar = (Toolbar) findViewById(R.id.activity_search_toolbar);
        setSupportActionBar(toolbar);
        mSearchBar = (EditText) findViewById(R.id.search_bar);
        mBackButton = (ImageButton) findViewById(R.id.search_back);
        mAddButton = (Button) findViewById(R.id.add_button);

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
        mContactsAdapter = new ParseRecyclerQueryAdapter<ParseObject>(getApplicationContext(), new ParseRecyclerQueryAdapter.QueryFactory() {
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
                final View itemView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.item_contact, null);
                final ContactViewHolder viewHolder = new ContactViewHolder(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ParseObject contact = getItem(viewHolder.getAdapterPosition());
                        ParseObject user = contact.getParseUser("username");
                        Toast.makeText(getApplicationContext(), "Hello", Toast.LENGTH_SHORT).show();
                        startChatActivity(user);
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

        mContactsList.setAdapter(mContactsAdapter);
        mContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_back:
                finish();
                break;
            case R.id.add_button:
                if (mUsername == "")
                    Toast.makeText(this, "Le champ de recherche ne doit être vide", Toast.LENGTH_SHORT).show();
                else {
                    ParseQuery<ParseUser> userQuery = new ParseQuery<>("_User");
                    userQuery.whereEqualTo("username", mUsername);

                    userQuery.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            if (e != null) {

                            } else if (objects != null && objects.size() > 0) {
                                ParseUser user = objects.get(0);
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
                    });
                }
                break;
            default:
                break;
        }
    }

    private void startChatActivity(ParseObject user) {
        final Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        chatIntent.putExtra(ChatActivity.EXTRA_PROFILE_ID, user.getObjectId());
        startActivity(chatIntent);
    }
}
