package com.epitech.wepleb.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class PlebSharedPreferences {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context mContext;

    public PlebSharedPreferences(Context context) {
        this.mContext = context;
        sharedPreferences = this.mContext.getApplicationContext().getSharedPreferences("PhysicalLogicalEncryptionBackups", 0);
        editor = sharedPreferences.edit();
    }

    public void setPassphrase(String objectID, String pass) {
        editor.putString(objectID, pass);
        editor.commit();
    }

    public String getPassphrase(String objectID) {
        return sharedPreferences.getString(objectID,"");
    }
}
