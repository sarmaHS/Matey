package com.mateyinc.marko.matey.model;

import android.os.Parcelable;

public abstract class MModel implements Parcelable {

    // Keys for JSON data from the server
    static final String KEY_ID = "activity_id";
    static final String KEY_USER_ID = "user_id";
    static final String KEY_SOURCE_ID = "source_id";
    static final String KEY_PARENT_ID = "parent_id";
    static final String KEY_PARENT_TYPE = "parent_type";
    static final String KEY_ACTIVITY_TYPE = "activity_type";
    static final String KEY_DATE_ADDED = "activity_time";
    static final String KEY_FIRSTNAME = "first_name";
    static final String KEY_LASTNAME = "last_name";
    static final String KEY_PROFILE_PIC = "profile_picture";

    protected int mServerStatus = 0;

    public int getServerStatus() {
        return mServerStatus;
    }

    public void setServerStatus(int status) {
        mServerStatus = status;
    }
}