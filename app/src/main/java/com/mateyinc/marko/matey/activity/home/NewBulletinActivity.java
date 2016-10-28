package com.mateyinc.marko.matey.activity.home;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.DataManager;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.SessionManager;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.Date;


public class NewBulletinActivity extends MotherActivity {

    private EditText etNewPostMsg;
    private TextView tvPost;
    private ImageButton ibBack;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_bulletin);

        init();
    }

    private void init() {
        // Settings the app bar via custom toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        etNewPostMsg = (EditText) findViewById(R.id.etNewBulletinMsg);
        ibBack = (ImageButton) findViewById(R.id.ibBack);
        tvPost = (TextView) findViewById(R.id.tvPost);
        tvPost.setEnabled(false); // Can't post until something is typed in

        setClickListeners();
    }

    private void setClickListeners() {
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        etNewPostMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                enableButton(s);

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableButton(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                enableButton(s);

            }

            private void enableButton(CharSequence s) {
                if (s == null || s.length() == 0) {
                    tvPost.setEnabled(false);
                } else {
                    tvPost.setEnabled(true);
                }
            }
        });

        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataManager dataManager = DataManager.getInstance(NewBulletinActivity.this);

                UserProfile profile = dataManager.getCurrentUserProfile();
                Bulletin b = new Bulletin();
                b.setUserID(profile.getUserId());
                b.setPostID(dataManager.getNewActivityId());
                b.setDate(new Date());
                b.setFirstName(profile.getFirstName());
                b.setLastName(profile.getLastName());
                b.setMessage(etNewPostMsg.getText().toString());

                SessionManager.getInstance(NewBulletinActivity.this).postNewBulletin(b, dataManager);

                finish();
            }
        });


    }

}
