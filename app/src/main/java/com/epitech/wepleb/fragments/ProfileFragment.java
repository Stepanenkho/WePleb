package com.epitech.wepleb.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class ProfileFragment extends BaseFragment {

    private ParseUser mUser;
    private TextView mUsernameText;
    private TextView mDisconnect;
    private ImageView mPictureImage;


    public static ProfileFragment newInstance() {return new ProfileFragment();}

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
        mUser = ParseUser.getCurrentUser();
        mUsernameText.setText(mUser.getUsername());

        mDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseUser.logOut();
                Toast.makeText(ProfileFragment.this.getContext(), "Déconnecté !", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(ProfileFragment.this.getContext(), input.getText().toString(), Toast.LENGTH_SHORT).show();
                                mUser.setUsername(input.getText().toString().trim());
                                mUser.saveInBackground(new SaveCallback() {
                                    public void done(com.parse.ParseException e) {
                                        // TODO Auto-generated method stub
                                        if (e == null) {
                                            mUsernameText.setText(mUser.getUsername());
                                        } else {
                                            Toast.makeText(ProfileFragment.this.getContext(), "Error in updating the information.", Toast.LENGTH_SHORT).show();
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

    private void startWelcomeActivity() {
        final Intent welcomeIntent = new Intent(getContext(), WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(welcomeIntent);
    }
}
