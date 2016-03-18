package com.epitech.wepleb.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.epitech.wepleb.R;
import com.epitech.wepleb.adapters.ParseRecyclerQueryAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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

    private FrameLayout mProgressBar;
    private RecyclerView mContactsList;
    private ParseRecyclerQueryAdapter<ParseObject> mContactsAdapter;
    private String mUsername;
    private ParseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mContext = getApplicationContext();
        toolbar = (Toolbar) findViewById(R.id.activity_search_toolbar);
        setSupportActionBar(toolbar);
        mSearchBar = (EditText) findViewById(R.id.search_bar);
        if (mContext != null)
            mSearchBar.getBackground().mutate().setColorFilter(ContextCompat.getColor(mContext, R.color.white), PorterDuff.Mode.SRC_ATOP);

        mBackButton = (ImageButton) findViewById(R.id.search_back);
        mAddButton = (Button) findViewById(R.id.add_button);
        mProgressBar = (FrameLayout) findViewById(R.id.activity_progress_bar);

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
                mAddButton.setText("chercher le contact : " + s.toString());
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
                    ImageView profile;

                    ContactViewHolder(View itemView) {
                        super(itemView);
                        username = (TextView) itemView.findViewById(R.id.item_contact_username);
                        profile = (ImageView) itemView.findViewById(R.id.item_contact_profile_picture);
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
                    final ParseUser contactUser = (ParseUser) ParseObject.createWithoutData("_User", user.getObjectId());
                    contactUser.fetchInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            if (e != null)
                                e.printStackTrace();
                            else {
                                mUser = (ParseUser) object;
                                ParseFile picture = mUser.getParseFile("avatar");
                                String url = picture == null ? null : picture.getUrl();
                                ImageLoader imageLoader = ImageLoader.getInstance();
                                imageLoader.displayImage(url, viewHolder.profile);
                                try {
                                    viewHolder.username.setText(mUser.fetchIfNeeded().getUsername());
                                } catch (ParseException e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    });
                }
            };

            mContactsAdapter.addOnQueryLoadListener(new ParseRecyclerQueryAdapter.OnQueryLoadListener<ParseObject>() {
                @Override
                public void onLoading(ParseRecyclerQueryAdapter adapter) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mContactsList.setVisibility(View.GONE);
                }

                @Override
                public void onLoaded(ParseRecyclerQueryAdapter adapter, List<ParseObject> objects, Exception e) {
                    mProgressBar.setVisibility(View.GONE);
                    mContactsList.setVisibility(View.VISIBLE);
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
                    if (mUsername.matches(""))
                        Toast.makeText(this, "Le champ de recherche ne doit être vide", Toast.LENGTH_SHORT).show();
                    else if (mUsername.equals(ParseUser.getCurrentUser().getUsername()))
                        Toast.makeText(this, "Vous ne pouvez vous rechercher vous-même", Toast.LENGTH_SHORT).show();
                    else {
                        final ParseQuery<ParseUser> userQuery = new ParseQuery<>("_User");
                        userQuery.whereEqualTo("username", mUsername);
                        userQuery.findInBackground(new FindCallback<ParseUser>() {
                            @Override
                            public void done(List<ParseUser> plebs, ParseException e) {
                                if (e != null)
                                    e.printStackTrace();
                                else if (plebs != null && plebs.size() == 0)
                                    Toast.makeText(mContext, "Pas de pleb trouvé", Toast.LENGTH_SHORT).show();
                                else if (plebs != null && plebs.size() > 0) {
                                    startProfileActivity(plebs.get(0));
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
