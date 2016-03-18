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

    public void setPassphrase(String pass) {
        editor.putString("passphrase", pass);
        editor.commit();
    }

    public String getPassphrase() {
        return sharedPreferences.getString("passphrase", null);
    }
}
