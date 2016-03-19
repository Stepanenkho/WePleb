package com.epitech.wepleb.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.epitech.wepleb.R;
import com.epitech.wepleb.activities.WelcomeActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ProfileFragment extends BaseFragment {

    static final int GALLERY_PICTURE_REQUEST_CODE = 1;
    static final int REQUEST_IMAGE_CAPTURE = 2;

    private ParseUser mUser;
    private TextView mUsernameText;
    private TextView mDisconnect;
    private TextView mPassword;
    private ImageView mPictureImage;
    private ImageLoader imageLoader;
    private ImageView mQrCode;


    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mUsernameText = (TextView) view.findViewById(R.id.fragment_profile_username);
        mDisconnect = (TextView) view.findViewById(R.id.fragment_profile_disconnect);
        mPassword = (TextView) view.findViewById(R.id.fragment_profile_password);
        mQrCode = (ImageView) view.findViewById(R.id.fragment_profile_qrcode);

        mUser = ParseUser.getCurrentUser();
        mUsernameText.setText(mUser.getUsername());

        mQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View vqrcode = inflater.inflate(R.layout.dialog_profile_qrcode, null);
                builder.setView(vqrcode);
                ImageView imageView = (ImageView) vqrcode.findViewById(R.id.dialog_profile_image_qrcode);
                imageView.setImageBitmap(encodeToQrCode(mUser.getObjectId(), 100, 100));
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
        mPictureImage = (ImageView) view.findViewById(R.id.fragment_profile_picture);
        ParseFile picture = mUser.getParseFile("avatar");
        if (picture != null) {
            imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(picture.getUrl(), mPictureImage);
        }

        mPictureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View view = inflater.inflate(R.layout.dialog_profile_picture, null);
                builder.setView(view)
                        .setPositiveButton("Prendre une photo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                            }
                        })
                        .setNegativeButton("Galerie", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(galleryIntent, GALLERY_PICTURE_REQUEST_CODE);
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        mPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View view = inflater.inflate(R.layout.dialog_profile_password, null);
                builder.setView(view)
                        .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EditText input = (EditText) view.findViewById(R.id.dialog_profile_password);
                                mUser.setPassword(input.getText().toString().trim());
                                mUser.saveInBackground(new SaveCallback() {
                                    public void done(com.parse.ParseException e) {
                                        if (e == null) {
                                            if (ProfileFragment.this.getContext() != null)
                                                Toast.makeText(ProfileFragment.this.getContext(), "Succès !", Toast.LENGTH_SHORT).show();
                                        } else {
                                            if (ProfileFragment.this.getContext() != null)
                                                Toast.makeText(ProfileFragment.this.getContext(), "Une erreur est survenue.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        mDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                if (ProfileFragment.this.getContext() != null)
                    Toast.makeText(ProfileFragment.this.getContext(), "Déconnecté !", Toast.LENGTH_SHORT).show();

                ParseObject parseInstallation = ParseInstallation.getCurrentInstallation();
                parseInstallation.remove("user");
                parseInstallation.saveInBackground();

                startWelcomeActivity();
                getActivity().finishAffinity();
            }
        });

        mUsernameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = getActivity().getLayoutInflater();
                final View view = inflater.inflate(R.layout.dialog_profile_username, null);
                builder.setView(view)
                        .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EditText input = (EditText) view.findViewById(R.id.dialog_profile_username);
                                if (ProfileFragment.this.getContext() != null)
                                    Toast.makeText(ProfileFragment.this.getContext(), input.getText().toString(), Toast.LENGTH_SHORT).show();
                                mUser.setUsername(input.getText().toString().trim());
                                mUser.saveInBackground(new SaveCallback() {
                                    public void done(com.parse.ParseException e) {
                                        if (e == null) {
                                            mUsernameText.setText(mUser.getUsername());
                                        } else {
                                            if (ProfileFragment.this.getContext() != null)
                                                Toast.makeText(ProfileFragment.this.getContext(), "Une erreur est survenue.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_PICTURE_REQUEST_CODE) {
                if (data == null) {
                    if (ProfileFragment.this.getContext() != null)
                        Toast.makeText(ProfileFragment.this.getContext(), "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                    return;
                }
                mPictureImage.setImageURI(data.getData());
                try {
                    Bitmap picture = MediaStore.Images.Media.getBitmap(ProfileFragment.this.getContext().getContentResolver(), data.getData());
                    Bitmap avatar = ThumbnailUtils.extractThumbnail(picture, 256, 256);

                    ByteArrayOutputStream avatarStream = new ByteArrayOutputStream();
                    avatar.compress(Bitmap.CompressFormat.JPEG, 100, avatarStream);
                    final ParseFile avatarFile = new ParseFile(avatarStream.toByteArray());
                    avatarFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                e.printStackTrace();
                                if (ProfileFragment.this.getContext() != null)
                                    Toast.makeText(ProfileFragment.this.getContext(), "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            final ParseUser user = ParseUser.getCurrentUser();
                            user.put("avatar", avatarFile);
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        e.printStackTrace();
                                        if (ProfileFragment.this.getContext() != null)
                                            Toast.makeText(ProfileFragment.this.getContext(), "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    if (ProfileFragment.this.getContext() != null)
                        Toast.makeText(ProfileFragment.this.getContext(), "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (ProfileFragment.this.getContext() != null)
                        Toast.makeText(ProfileFragment.this.getContext(), "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                Bitmap picture = (Bitmap) extras.get("data");
                Bitmap avatar = ThumbnailUtils.extractThumbnail(picture, 256, 256);

                ByteArrayOutputStream avatarStream = new ByteArrayOutputStream();
                avatar.compress(Bitmap.CompressFormat.JPEG, 100, avatarStream);
                final ParseFile avatarFile = new ParseFile(avatarStream.toByteArray());
                avatarFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            e.printStackTrace();
                            if (ProfileFragment.this.getContext() != null)
                                Toast.makeText(ProfileFragment.this.getContext(), "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        final ParseUser user = ParseUser.getCurrentUser();
                        user.put("avatar", avatarFile);
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    e.printStackTrace();
                                    if (ProfileFragment.this.getContext() != null)
                                        Toast.makeText(ProfileFragment.this.getContext(), "Une erreur est survenue", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        }
    }

    private void startWelcomeActivity() {
        if (getContext() != null) {
            final Intent welcomeIntent = new Intent(getContext(), WelcomeActivity.class);
            welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(welcomeIntent);
        }
    }

    public static Bitmap encodeToQrCode(String text, int width, int height) {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = null;
        try {
            matrix = writer.encode(text, BarcodeFormat.QR_CODE, 100, 100);
        } catch (WriterException ex) {
            ex.printStackTrace();
        }
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }
}
