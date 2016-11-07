package com.mateyinc.marko.matey.activity.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.NetworkManager;
import com.mateyinc.marko.matey.internet.profile.UserProfileAs;
import com.mateyinc.marko.matey.model.UserProfile;
import com.mateyinc.marko.matey.storage.SecurePreferences;

import java.lang.ref.WeakReference;

public class ProfileActivity extends MotherActivity {
    private String TAG = ProfileActivity.class.getSimpleName();

    public static final String PROFILE_DOWNLOADED = "com.mateyinc.marko.matey.activity.profile.profile_downloaded";

    /** Intent extra param which has {@link com.mateyinc.marko.matey.model.UserProfile#userId}
     * used to download data from the server or from database; */
    public static final String EXTRA_PROFILE_ID = "com.mateyinc.marko.matey.activity.profile.user_id";

    private TextView tvName,tvNumberOfMates;
    private ImageView ivProfilePic;
    private long mUserId;
    private UserProfile mUserProfile;
    private BroadcastReceiver mBroadcastReceiver;
    private UserProfileAs mUserProfileAs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_profile);

        init();
        downloadData();
    }


    private void init() {
        ivProfilePic = (ImageView) findViewById(R.id.ivProfilePic);
        tvName = (TextView) findViewById(R.id.tvName);
        tvNumberOfMates = (TextView)findViewById(R.id.tvNumOfMates);
        mUserProfile = new UserProfile();

        // If intent doesn't have extra profile id, then ProfileActivity is called for the current user profile
        if (getIntent().hasExtra(EXTRA_PROFILE_ID))
            mUserId = getIntent().getLongExtra(EXTRA_PROFILE_ID, -1);
        else{
            mUserId = DataManager.getInstance(ProfileActivity.this).getCurrentUserProfile().getUserId();
        }

        mUserProfileAs = new UserProfileAs(this, new WeakReference<>(mUserProfile),mUserId);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setData();
            }
        };
    }

    private void setData() {
        Log.d("ProfileActivity", "Data is set.");
        //mImageLoader.displayImage(mUserProfile.getProfilePictureLink(), ivProfilePic);
        NetworkManager.getInstance(this).downloadImage(ivProfilePic, mUserProfile.getProfilePictureLink());
        tvName.setText(mUserProfile.getFirstName() + " " + mUserProfile.getLastName());
        tvNumberOfMates.setText(mUserProfile.getNumOfFriends());
    }

    private void downloadData() {
        // Start downloading data
        SecurePreferences preferences = getSecurePreferences();
        mUserProfileAs.execute(preferences.getString("user_id"),
                preferences.getString("uid"),
                preferences.getString("device_id"),
                Long.toString(mUserId));

    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.registerReceiver(mBroadcastReceiver, new IntentFilter(PROFILE_DOWNLOADED));

        // Trying to set the data if the it's downloaded before broadcast receiver is initiated
        try {
            setData();
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
    }

    @Override
    protected void onPause() {
        if (mBroadcastReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);

        // Stop downloading data if it isn't complete
        if (mUserProfileAs.getStatus() == AsyncTask.Status.RUNNING) {
            mUserProfileAs.cancel(true);
            Log.d(ProfileActivity.class.getSimpleName(), "userProfile info downloading stopped.");
        }
        super.onPause();
    }

}
