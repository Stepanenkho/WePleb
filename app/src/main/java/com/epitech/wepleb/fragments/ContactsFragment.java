package com.epitech.wepleb.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.epitech.wepleb.R;
import com.epitech.wepleb.activities.ChatActivity;
import com.epitech.wepleb.adapters.ParseRecyclerQueryAdapter;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class ContactsFragment extends BaseFragment {

    private ProgressBar mProgressBar;
    private RecyclerView mContactsList;
    private ParseRecyclerQueryAdapter<ParseObject> mContactsAdapter;


    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_progress_bar);
        mContactsList = (RecyclerView) view.findViewById(R.id.fragment_contacts_list);
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

                ContactViewHolder(View itemView) {
                    super(itemView);
                    username = (TextView) itemView.findViewById(R.id.item_contact_username);
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
                        Toast.makeText(ContactsFragment.this.getContext(), "Hello", Toast.LENGTH_SHORT).show();
                        startChatActivity(user);
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                final ContactViewHolder viewHolder = (ContactViewHolder) holder;
                final ParseObject contact = getItem(position);
                final ParseObject user = contact.getParseUser("user2");
                viewHolder.username.setText(user.getString("username"));
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
        mContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void startChatActivity(ParseObject user) {
        final Intent chatIntent = new Intent(getContext(), ChatActivity.class);
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        chatIntent.putExtra(ChatActivity.EXTRA_PROFILE_ID, user.getObjectId());
        startActivity(chatIntent);
    }
}
