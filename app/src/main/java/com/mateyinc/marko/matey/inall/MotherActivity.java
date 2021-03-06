package com.mateyinc.marko.matey.inall;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.model.UserProfile;
import com.mateyinc.marko.matey.storage.SecurePreferences;

abstract public class MotherActivity extends AppCompatActivity {

    protected Toolbar toolbar;

    // Static params used for session management
    /** Id of current user */
    public static long user_id;
    /** Id of the device retrieved from the server */
    public static String device_id;
    /** Access token used to authorise with the server */
    public static String access_token;
    /** {@link UserProfile} of current user **/
    public static UserProfile mCurrentUserProfile;


    private final Object mLock = new Object();
    private SecurePreferences mSecurePreferences;

    /**
     * True if GCM_token and device_id are present on the the device thus
     * indicating if the device is registered on the server
     */
    public boolean mDeviceReady = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public SecurePreferences getSecurePreferences() {
        synchronized (mLock) {
            if (mSecurePreferences == null) {
                mSecurePreferences = ((MyApplication) getApplication()).getSecurePreferences();
            }
            return mSecurePreferences;
        }
    }

    protected void setSupportActionBar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    /**
     * For activities that uses childActionBar and that have back button
     */
    protected void setChildSupportActionBar(){
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.findViewById(R.id.ibBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


}
