package com.epitech.wepleb.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.epitech.wepleb.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class ProfileActivity extends BaseActivity implements View.OnClickListener {

    public static final String EXTRA_PROFILE_ID = "EXTRA_PROFILE_ID";

    private TextView mUsername;
    private ImageView mPicture;
    private Button mChatButton;
    private Button mAddButton;
    private ParseUser mContact;
    private ParseUser mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mPicture = (ImageView) findViewById(R.id.activity_profile_picture);
        mUsername = (TextView) findViewById(R.id.activity_profile_username);
        mChatButton = (Button) findViewById(R.id.activity_profile_messages);
        mAddButton = (Button) findViewById(R.id.activity_profile_contact);

        mChatButton.setOnClickListener(this);
        mAddButton.setOnClickListener(this);

        mUser = ParseUser.getCurrentUser();
        mContact = (ParseUser) ParseObject.createWithoutData("_User", getIntent().getStringExtra(EXTRA_PROFILE_ID));
        mContact.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null) {
                    Snackbar.make(findViewById(android.R.id.content), "Utilisateur non reconnu.", Snackbar.LENGTH_LONG)
                            .setActionTextColor(Color.RED)
                            .show();
                    finish();
                    e.printStackTrace();
                }
                else {
                    mContact = (ParseUser) object;
                    mUsername.setText(mContact.getUsername());
                    ParseQuery<ParseUser> tmpQuery = new ParseQuery<ParseUser>("Contacts");
                    tmpQuery.whereEqualTo("user2", mContact);
                    tmpQuery.whereEqualTo("user1", mUser);
                    tmpQuery.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            if (e != null)
                                e.printStackTrace();
                            else {
                                if (objects.size() != 0) {
                                    mAddButton.setVisibility(View.GONE);
                                }
                            }
                        }
                    });
                    ParseFile picture = mContact.getParseFile("avatar");
                    String url = picture == null ? null : picture.getUrl();
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(url, mPicture);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_profile_messages:
                startChatActivity(mContact);
                break;
            case R.id.activity_profile_contact:
                addContact();
                break;
        }
    }

    private void addContact() {
        ParseObject newContact = ParseObject.create("Contacts");
        newContact.put("user1", mUser);
        newContact.put("user2", mContact);
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

    private void startChatActivity(ParseObject user) {
        final Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        chatIntent.putExtra(ChatActivity.EXTRA_PROFILE_ID, user.getObjectId());
        startActivity(chatIntent);
    }
}
