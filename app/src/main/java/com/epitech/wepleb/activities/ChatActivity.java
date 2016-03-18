package com.epitech.wepleb.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.epitech.wepleb.R;
import com.epitech.wepleb.adapters.ParseRecyclerQueryAdapter;
import com.epitech.wepleb.helpers.PlebSharedPreferences;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends BaseActivity implements ParseRecyclerQueryAdapter.OnQueryLoadListener<ParseObject> {

    public static final String EXTRA_DISCUSSION_ID = "EXTRA_DICUSSION_ID";
    public static final String EXTRA_PROFILE_ID = "EXTRA_PROFILE_ID";

    private Toolbar mToolbar;
    private RecyclerView mList;
    private EditText mChatInput;
    private TextView mSend;

    private ParseRecyclerQueryAdapter<ParseObject> mAdapter;

    private ParseObject mDiscussion;
    private ParseObject mUser;
    private int messageCount = 0;
    private PlebSharedPreferences plebSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        plebSharedPreferences = new PlebSharedPreferences(this);
        mToolbar = (Toolbar) findViewById(R.id.activity_chat_toolbar);
        setSupportActionBar(mToolbar);
        mList = (RecyclerView) findViewById(R.id.activity_chat_list);
        mChatInput = (EditText) findViewById(R.id.activity_chat_input);
        mSend = (TextView) findViewById(R.id.activity_chat_send);

        setActionbar();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String discussionId = extras.getString(EXTRA_DISCUSSION_ID);
            String profileId = extras.getString(EXTRA_PROFILE_ID);

            if (discussionId != null) {
                loadDiscussion(discussionId);
            } else if (profileId != null) {
                loadDiscussionWithProfile(profileId);
            }
        }
    }

    private void startAutoRefresh() {
        final Handler handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                mAdapter.reload();
                handler.postDelayed(this, 5000);
            }
        };

        handler.postDelayed(r, 5000);
    }

    private void loadDiscussionWithProfile(String profileId) {
        mUser = ParseObject.createWithoutData("_User", profileId);

        ParseQuery<ParseObject> query1 = new ParseQuery<>("Discussions");
        query1.whereEqualTo("user1", ParseUser.getCurrentUser());
        query1.whereEqualTo("user2", mUser);
        ParseQuery<ParseObject> query2 = new ParseQuery<>("Discussions");
        query2.whereEqualTo("user1", mUser);
        query2.whereEqualTo("user2", ParseUser.getCurrentUser());
        List<ParseQuery<ParseObject>> orQueries = new ArrayList<>();
        orQueries.add(query1);
        orQueries.add(query2);

        ParseQuery<ParseObject> discussionQuery = ParseQuery.or(orQueries);
        discussionQuery.include("user1");
        discussionQuery.include("user2");
        discussionQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    Toast.makeText(ChatActivity.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                    finish();
                }

                if (objects.size() > 0) {
                    mDiscussion = objects.get(0);
                    if (mDiscussion.getParseUser("user1").getObjectId().equals(ParseUser.getCurrentUser().getObjectId()))
                        mUser = mDiscussion.getParseUser("user2");
                    else
                        mUser = mDiscussion.getParseUser("user1");
                    initializeMessagesList();
                    populateView();
                } else {
                    createDiscussion();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem search = menu.getItem(R.id.action_search);
        search.setVisible(false);
        MenuItem qrCode = menu.getItem(R.id.action_qrcode_add);
        qrCode.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_add:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.dialog_profile_username, null);
                TextView title = (TextView) view.findViewById(R.id.dialog_profile_title);
                title.setText("Passphrase");
                final EditText mPassphrase = (EditText) view.findViewById(R.id.dialog_profile_username);
                builder.setView(view)
                        .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // SharedPreference add Passphrase
                                plebSharedPreferences.setPassphrase(mPassphrase.getText().toString());
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
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadDiscussion(String discussionId) {
        final ParseQuery<ParseObject> discussionQuery = new ParseQuery<>("Discussions");
        discussionQuery.whereEqualTo("objectId", discussionId);
        discussionQuery.include("user1");
        discussionQuery.include("user2");
        discussionQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e != null || objects == null || objects.size() < 1) {
                    if (e != null)
                        e.printStackTrace();
                    Toast.makeText(ChatActivity.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                mDiscussion = objects.get(0);
                if (mDiscussion.getParseUser("user1").getObjectId().equals(ParseUser.getCurrentUser().getObjectId()))
                    mUser = mDiscussion.getParseUser("user2");
                else
                    mUser = mDiscussion.getParseUser("user1");
                initializeMessagesList();
                populateView();
            }
        });
    }

    private void createDiscussion() {
        final ParseObject discussion = ParseObject.create("Discussions");

        discussion.put("user1", ParseUser.getCurrentUser());
        discussion.put("user2", mUser);
        discussion.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    Toast.makeText(ChatActivity.this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                loadDiscussion(discussion.getObjectId());
            }
        });
    }

    private void initializeMessagesList() {
        mAdapter = new ParseRecyclerQueryAdapter<ParseObject>(ChatActivity.this, new ParseRecyclerQueryAdapter.QueryFactory() {
            @Override
            public ParseQuery create() {
                final ParseQuery<ParseObject> messagesQuery = new ParseQuery<>("Messages");
                messagesQuery.whereEqualTo("discussion", mDiscussion);
                messagesQuery.include("user");
                return messagesQuery;
            }
        }) {

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                final View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_message, parent, false);
                final MessageItemViewHolder viewHolder = new MessageItemViewHolder(itemView);
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                final MessageItemViewHolder viewHolder = (MessageItemViewHolder) holder;
                final ParseObject message = getItem(position);
                final ParseUser user = message.getParseUser("user");
                ParseFile pictureFile = user.getParseFile("avatar");

                String pictureUrl = pictureFile != null ? pictureFile.getUrl() : null;

                ParseObject previousMessage = null;
                if (position > 0)
                    previousMessage = getItem(position - 1);

                if (user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                    viewHolder.root.setGravity(Gravity.RIGHT);
                    viewHolder.pictureLeft.setVisibility(View.INVISIBLE);

                    if (previousMessage != null && previousMessage.getParseUser("user").getObjectId().equals(user.getObjectId())) {
                        viewHolder.pictureRight.setVisibility(View.INVISIBLE);
                    } else {
                        viewHolder.pictureRight.setVisibility(View.VISIBLE);
                        ImageLoader.getInstance().displayImage(pictureUrl, viewHolder.pictureRight);
                    }

                    viewHolder.pictureRight.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(ProfileActivity.EXTRA_PROFILE_ID, user.getObjectId());
                            mContext.startActivity(intent);
                        }
                    });

                    viewHolder.message.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.rounded_grey));
                } else {
                    viewHolder.root.setGravity(Gravity.LEFT);
                    viewHolder.pictureRight.setVisibility(View.INVISIBLE);

                    if (previousMessage != null && previousMessage.getParseUser("user").getObjectId().equals(user.getObjectId())) {
                        viewHolder.pictureLeft.setVisibility(View.INVISIBLE);
                    } else {
                        viewHolder.pictureLeft.setVisibility(View.VISIBLE);
                        ImageLoader.getInstance().displayImage(pictureUrl, viewHolder.pictureLeft);
                    }

                    viewHolder.pictureLeft.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra(ProfileActivity.EXTRA_PROFILE_ID, user.getObjectId());
                            mContext.startActivity(intent);
                        }
                    });

                    viewHolder.message.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.rounded_primary));
                }

                viewHolder.message.setText(message.getString("message"));
            }

            class MessageItemViewHolder extends RecyclerView.ViewHolder {

                public LinearLayout root;
                public ImageView pictureLeft;
                public TextView message;
                public ImageView pictureRight;

                public MessageItemViewHolder(View itemView) {
                    super(itemView);

                    root = (LinearLayout) itemView.findViewById(R.id.item_message);
                    pictureLeft = (ImageView) itemView.findViewById(R.id.item_message_user_left);
                    message = (TextView) itemView.findViewById(R.id.item_message_text);
                    pictureRight = (ImageView) itemView.findViewById(R.id.item_message_user_right);
                }
            }
        };

        mAdapter.addOnQueryLoadListener(this);
        mAdapter.reload();
        mList.setAdapter(mAdapter);
        mList.setLayoutManager(new LinearLayoutManager(this));
        startAutoRefresh();
    }

    private void setActionbar() {
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            mToolbar.getNavigationIcon().setColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public void setKeyboardVisibilityListener(final KeyboardVisibilityListener keyboardVisibilityListener) {
        mList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int mPreviousHeight;

            @Override
            public void onGlobalLayout() {
                int newHeight = mList.getHeight();
                if (mPreviousHeight != 0) {
                    if (mPreviousHeight > newHeight) {
                        // Height decreased: keyboard was shown
                        keyboardVisibilityListener.onKeyboardVisibilityChanged(true);
                    } else if (mPreviousHeight < newHeight) {
                        // Height increased: keyboard was hidden
                        keyboardVisibilityListener.onKeyboardVisibilityChanged(false);
                    } else {
                        // No change
                    }
                }
                mPreviousHeight = newHeight;
            }
        });
    }

    private void populateView() {
        if (mDiscussion != null) {
            mToolbar.setTitle(mUser.getString("username"));

            setKeyboardVisibilityListener(new KeyboardVisibilityListener() {
                @Override
                public void onKeyboardVisibilityChanged(boolean keyboardVisible) {
                    if (keyboardVisible && mAdapter.getItemCount() > 0) {
                        mList.scrollToPosition(mAdapter.getItemCount() - 1);
                    }
                }
            });

            mSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage(mChatInput.getText().toString());
                }
            });
        }
    }

    private void sendMessage(String input) {
        if (input != null && !input.isEmpty() && mDiscussion != null) {
            //mChatInput.setEnabled(false);
            mSend.setEnabled(false);
            final ParseObject message = ParseObject.create("Messages");
            message.put("user", ParseUser.getCurrentUser());
            message.put("discussion", mDiscussion);
            message.put("message", input);
            message.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    //mChatInput.setEnabled(true);
                    mSend.setEnabled(true);

                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }

                    mChatInput.setText("");
                    mAdapter.addItem(message);
                    mAdapter.notifyDataSetChanged();
                    mList.scrollToPosition(mAdapter.getItemCount() - 1);
                }
            });
            mDiscussion.put("last_message", message);
            mDiscussion.saveInBackground();
        }

    }

    @Override
    public void onLoaded(ParseRecyclerQueryAdapter adapter, List<ParseObject> objects, Exception e) {
        if (e != null) {
            e.printStackTrace();
            return;
        }

        if (messageCount != objects.size()) {
            if (messageCount != 0) {
                mList.smoothScrollToPosition(mAdapter.getItemCount() - 1);
            } else {
                mList.scrollToPosition(mAdapter.getItemCount() - 1);
            }
            messageCount = objects.size();
        }
    }

    /*
    @Override
    public void onNotificationEvent(NotificationEvent event) {
        if (event.type != null && event.type.equals("message"))
            return;
        Crouton.makeText(this, event.message, Style.INFO).show();
    }
    */

    @Override
    public void onLoading(ParseRecyclerQueryAdapter adapter) {

    }

    public interface KeyboardVisibilityListener {
        void onKeyboardVisibilityChanged(boolean keyboardVisible);
    }

    /*
    @Subscribe
    public void onNewMessageEvent(final NewMessageEvent event) {
        if (event.id.equals(mDiscussion.getObjectId())) {
            mAdapter.loadParseData(0, true);
        } else {
            Crouton.makeText(this, event.message, Style.INFO).show();
        }
        /*
        ParseObject conversation = ParseObject.create("Conversation");
        conversation.setObjectId(event.id);
        conversation.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }

            }
        });
        */

    //}

}
