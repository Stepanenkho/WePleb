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

import com.epitech.wepleb.R;
import com.epitech.wepleb.activities.ChatActivity;
import com.epitech.wepleb.activities.ProfileActivity;
import com.epitech.wepleb.adapters.ParseRecyclerQueryAdapter;
import com.epitech.wepleb.helpers.DividerItemDecoration;
import com.epitech.wepleb.helpers.PlebSharedPreferences;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends BaseFragment {

    private FrameLayout mProgressBar;
    private RecyclerView mMessagesList;
    private ParseRecyclerQueryAdapter<ParseObject> mMessagesAdapter;
    private PlebSharedPreferences plebSharedPreferences;


    public static MessagesFragment newInstance() {
        return new MessagesFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getContext() != null)
            plebSharedPreferences = new PlebSharedPreferences(getContext());
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = (FrameLayout) view.findViewById(R.id.fragment_messages_progress_bar);
        mMessagesList = (RecyclerView) view.findViewById(R.id.fragment_messages_list);
        mMessagesList.addItemDecoration(new DividerItemDecoration(MessagesFragment.this.getContext(), 1));

        mMessagesAdapter = new ParseRecyclerQueryAdapter<ParseObject>(getContext(), new ParseRecyclerQueryAdapter.QueryFactory() {
            @Override
            public ParseQuery create() {
                ParseQuery<ParseObject> query1 = new ParseQuery<>("Discussions");
                query1.whereEqualTo("user1", ParseUser.getCurrentUser());
                ParseQuery<ParseObject> query2 = new ParseQuery<>("Discussions");
                query2.whereEqualTo("user2", ParseUser.getCurrentUser());
                List<ParseQuery<ParseObject>> orQueries = new ArrayList<>();
                orQueries.add(query1);
                orQueries.add(query2);

                ParseQuery<ParseObject> discussionQuery = ParseQuery.or(orQueries);
                discussionQuery.include("user1");
                discussionQuery.include("user2");
                discussionQuery.include("last_message");
                return discussionQuery;
            }
        }) {

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                final View itemView = LayoutInflater.from(MessagesFragment.this.getContext()).inflate(R.layout.item_contact, null);
                final MessageViewHolder viewHolder = new MessageViewHolder(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ParseObject discussion = getItem(viewHolder.getAdapterPosition());
                        startChatActivity(discussion);
                    }
                });
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
                final MessageViewHolder viewHolder = (MessageViewHolder) holder;
                final ParseObject discussion = getItem(position);
                final ParseObject user = discussion.getParseUser("user1").getObjectId().equals(ParseUser.getCurrentUser().getObjectId()) ? discussion.getParseUser("user2") : discussion.getParseUser("user1");

                ParseFile picture = user.getParseFile("avatar");
                String url = picture == null ? null : picture.getUrl();
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(url, viewHolder.profile);

                viewHolder.username.setText(user.getString("username"));
                ParseObject lastMessage = discussion.getParseObject("last_message");
                if (lastMessage != null) {
                    viewHolder.lastMessage.setVisibility(View.VISIBLE);
                    try {
                        String translated = AESCrypt.decrypt(plebSharedPreferences.getPassphrase(lastMessage.getParseObject("discussion").getObjectId()), lastMessage.getString("message"));
                        viewHolder.lastMessage.setText(lastMessage.getParseUser("user").getObjectId()
                                .equals(ParseUser.getCurrentUser().getObjectId()) ?
                                "Vous : " + translated :
                                translated);
                    } catch (GeneralSecurityException e) {
                        e.printStackTrace();
                        viewHolder.lastMessage.setText(lastMessage.getParseUser("user").getObjectId()
                                .equals(ParseUser.getCurrentUser().getObjectId()) ?
                                "Vous : " + lastMessage.getString("message") :
                                lastMessage.getString("message"));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        viewHolder.lastMessage.setText(lastMessage.getParseUser("user").getObjectId()
                                .equals(ParseUser.getCurrentUser().getObjectId()) ?
                                "Vous : " + lastMessage.getString("message") :
                                lastMessage.getString("message"));
                    }
                } else {
                    viewHolder.lastMessage.setVisibility(View.GONE);
                }
                viewHolder.profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent = new Intent(MessagesFragment.this.getContext(), ProfileActivity.class);
                        profileIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        profileIntent.putExtra(ChatActivity.EXTRA_PROFILE_ID, user.getObjectId());
                        startActivity(profileIntent);
                    }
                });
            }

            class MessageViewHolder extends RecyclerView.ViewHolder {
                TextView username;
                ImageView profile;
                TextView lastMessage;

                MessageViewHolder(View itemView) {
                    super(itemView);
                    username = (TextView) itemView.findViewById(R.id.item_contact_username);
                    profile = (ImageView) itemView.findViewById(R.id.item_contact_profile_picture);
                    lastMessage = (TextView) itemView.findViewById(R.id.item_contact_mood);
                }
            }
        };
        mMessagesAdapter.addOnQueryLoadListener(new ParseRecyclerQueryAdapter.OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoading(ParseRecyclerQueryAdapter adapter) {
                mProgressBar.setVisibility(View.VISIBLE);
                mMessagesList.setVisibility(View.GONE);
            }

            @Override
            public void onLoaded(ParseRecyclerQueryAdapter adapter, List<ParseObject> objects, Exception e) {
                mProgressBar.setVisibility(View.GONE);
                mMessagesList.setVisibility(View.VISIBLE);
            }
        });

        mMessagesList.setAdapter(mMessagesAdapter);
        mMessagesList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void startChatActivity(ParseObject discussion) {
        final Intent chatIntent = new Intent(getContext(), ChatActivity.class);
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        chatIntent.putExtra(ChatActivity.EXTRA_DISCUSSION_ID, discussion.getObjectId());
        startActivity(chatIntent);
    }
}
