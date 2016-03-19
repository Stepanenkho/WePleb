package com.epitech.wepleb.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.epitech.wepleb.R;
import com.epitech.wepleb.activities.ChatActivity;
import com.epitech.wepleb.activities.ProfileActivity;
import com.epitech.wepleb.adapters.ParseRecyclerQueryAdapter;
import com.epitech.wepleb.helpers.DividerItemDecoration;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsFragment extends BaseFragment {

    private FrameLayout mProgressBar;
    private RecyclerView mContactsList;
    private ParseRecyclerQueryAdapter<ParseObject> mContactsAdapter;
    private ParseUser mUser;

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = (FrameLayout) view.findViewById(R.id.fragment_progress_bar);
        mContactsList = (RecyclerView) view.findViewById(R.id.fragment_contacts_list);
        mContactsList.addItemDecoration(new DividerItemDecoration(ContactsFragment.this.getContext(), 1));

        mContactsAdapter = new ParseRecyclerQueryAdapter<ParseObject>(getContext(), new ParseRecyclerQueryAdapter.QueryFactory() {
            @Override
            public ParseQuery create() {
                final ParseQuery<ParseObject> usersQuery = new ParseQuery<>("Contacts");
                usersQuery.whereEqualTo("user1", ParseUser.getCurrentUser());
                usersQuery.include("user2");
                return usersQuery;
            }
        }) {

            class ContactViewHolder extends RecyclerView.ViewHolder {
                TextView username;
                ImageView profile;
                TextView mood;

                ContactViewHolder(View itemView) {
                    super(itemView);
                    username = (TextView) itemView.findViewById(R.id.item_contact_username);
                    profile = (ImageView) itemView.findViewById(R.id.item_contact_profile_picture);
                    mood = (TextView) itemView.findViewById(R.id.item_contact_mood);
                    mood.setVisibility(View.GONE);
                }
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                final View itemView = LayoutInflater.from(ContactsFragment.this.getContext()).inflate(R.layout.item_contact, null);
                final ContactViewHolder viewHolder = new ContactViewHolder(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ParseObject contact = getItem(viewHolder.getAdapterPosition());
                        ParseObject user = contact.getParseUser("user2");
                        startChatActivity(user);
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
                final ContactViewHolder viewHolder = (ContactViewHolder) holder;
                final ParseObject contact = getItem(position);
                final ParseObject user = contact.getParseUser("user2");
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
                        }
                    }
                });
                viewHolder.username.setText(user.getString("username"));
                if (user.getString("mood") == null)
                    viewHolder.mood.setVisibility(View.GONE);
                else {
                    viewHolder.mood.setText(user.getString("mood"));
                    viewHolder.mood.setVisibility(View.VISIBLE);
                }
                viewHolder.profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        contactUser.fetchInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject object, ParseException e) {
                                if (e != null)
                                    e.printStackTrace();
                                else {
                                    mUser = (ParseUser) object;
                                    Intent profileIntent = new Intent(ContactsFragment.this.getContext(), ProfileActivity.class);
                                    profileIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    profileIntent.putExtra(ChatActivity.EXTRA_PROFILE_ID, mUser.getObjectId());
                                    startActivity(profileIntent);
                                }
                            }
                        });

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
        mContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void startChatActivity(ParseObject user) {
        final Intent chatIntent = new Intent(getContext(), ChatActivity.class);
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        chatIntent.putExtra(ChatActivity.EXTRA_PROFILE_ID, user.getObjectId());
        startActivity(chatIntent);
    }
}
