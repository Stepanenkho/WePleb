package com.epitech.wepleb.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.epitech.wepleb.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class ProfileActivity extends BaseActivity implements View.OnClickListener {

    public static final String EXTRA_PROFILE_ID = "EXTRA_PROFILE_ID";

    private TextView mUsername;
    private ImageView mPicture;
    private Button mChatButton;
    private ParseUser mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mPicture = (ImageView) findViewById(R.id.activity_profile_picture);
        mUsername = (TextView) findViewById(R.id.activity_profile_username);
        mChatButton = (Button) findViewById(R.id.activity_profile_messages);

        mChatButton.setOnClickListener(this);

        mUser = (ParseUser) ParseObject.createWithoutData("_User", getIntent().getStringExtra(EXTRA_PROFILE_ID));
        mUser.fetchInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e != null)
                    e.printStackTrace();
                else {
                    mUser = (ParseUser) object;
                    mUsername.setText(mUser.getUsername());
                    ParseFile picture = mUser.getParseFile("avatar");
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
                startChatActivity(mUser);
                break;

        }
    }

    private void startChatActivity(ParseObject user) {
        final Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        chatIntent.putExtra(ChatActivity.EXTRA_PROFILE_ID, user.getObjectId());
        startActivity(chatIntent);
    }
}
