package com.mateyinc.marko.matey.data_and_managers;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.mateyinc.marko.matey.activity.home.BulletinsFragment;
import com.mateyinc.marko.matey.data_and_managers.DataContract.NotificationEntry;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Message;
import com.mateyinc.marko.matey.model.Notification;
import com.mateyinc.marko.matey.model.UserProfile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;

/**
 * The manager for data entries
 */
public class DataManager {
    private static final String TAG = DataManager.class.getSimpleName();

    public static final int BULLETIN_NO_MAX = 20; // Def for max number of bulletins downloaded and saved on disk

    /**
     * Number of bulletins to download from the server
     */
    public static int NO_OF_BULLETIN_TO_DOWNLOAD = 40; // TODO - define how much bulletin will be downloaded at once;

    /**
     * the current count of the friends list in the database
     */
    public static final int mFriendsListCount = 120; // for dummy data is 120

    /**
     * The current page of bulletins in the database
     */
    public static int mCurrentPage = 0;

    // One day in milliseconds
    public static final int ONE_DAY = 86400000;
    // One minute in milliseconds
    public static final int ONE_MIN = 60000;

    public final ArrayList<Notification> mNotificationList = new ArrayList<>();
    public final ArrayList<Message> mMessageList;

    // JSON array names
    public static final String REPLY_APPRVS = "replyapproves";
    public static final String REPLIES_LIST = "replieslist";

    // For broadcast IntentFilters
    public static final String BULLETIN_LIST_LOADED = "com.mateyinc.marko.matey.internet.home.bulletins_loaded";
    public static final String EXTRA_ITEM_DOWNLOADED_COUNT = "com.mateyinc.marko.matey.internet.home.bulletins_loaded_count";

    // Global instance fields
    private static final Object mLock = new Object(); // for synchronised blocks
    private static DataManager mInstance = null;
    private final Context mAppContext;

    public static DataManager getInstance(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new DataManager(context.getApplicationContext());
                Log.d("DataManager", "New instance of manager created.");
            }
            return mInstance;
        }
    }

    private DataManager(Context context) {
        mAppContext = context;
        mMessageList = new ArrayList<>();
    }

    // UserProfile methods /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    /**
     * Helper method to handle the insertion of a new user profile to the db and the server
     *
     * @param userId       id of the user
     * @param userName     name of the user
     * @param userLastName last name of the user
     * @param lastMsgId    last message id that the user has sent to the current user
     * @return the row ID of the added user profile.
     */
    public long addUserProfile(int userId, String userName, String userLastName, int lastMsgId) {
//        long userProfId;

        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.ProfileEntry.CONTENT_URI,
                new String[]{DataContract.ProfileEntry._ID},
                DataContract.ProfileEntry._ID + " = " + userId,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            setUserProfile(userId, userName, userLastName, lastMsgId);
//            int userProfIdIndex = cursor.getColumnIndex(DataContract.MessageEntry._ID);
//            userProfId = cursor.getLong(userProfIdIndex);
        } else {
            ContentValues userValues = new ContentValues();

            userValues.put(DataContract.ProfileEntry._ID, userId);
            userValues.put(DataContract.ProfileEntry.COLUMN_NAME, userName);
            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, userLastName);
            userValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, lastMsgId);

            Uri insertedUri = mAppContext.getContentResolver().insert(
                    DataContract.ProfileEntry.CONTENT_URI,
                    userValues
            );

            if (null == insertedUri) {
                Log.e(TAG, "Error inserting UserProfile: ID=" + userId + "; Name=" + userName + "; Last name=" + userLastName);
            } else {
                Log.d("DataManager", "UserProfile added: ID=" + userId +
                        "; Name=" + userName + "; LastName=" + userLastName + "; LastMsgId=" + lastMsgId);
            }

//            userProfId = ContentUris.parseId(insertedUri);
        }

        if (cursor != null)
            cursor.close();

        return userId;
//        return userProfId;
    }

    public void addUserProfile(UserProfile profile) {
        addUserProfile(profile.getUserId(), profile.getFirstName(), profile.getLastName(), profile.getLastMsgId());
    }

    /**
     * Method for changing user profile in db
     *
     * @param userId       id of the user
     * @param userName     name of the user
     * @param userLastName last name of the user
     * @param lastMsgId    last message id that the user has sent to the current user
     */
    public void setUserProfile(int userId, String userName, String userLastName, int lastMsgId) {
        ContentValues userValues = new ContentValues();

        userValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, lastMsgId);
        userValues.put(DataContract.ProfileEntry.COLUMN_NAME, userName);
        userValues.put(DataContract.ProfileEntry.COLUMN_LAST_NAME, userLastName);


        int numOfUpdated = mAppContext.getContentResolver().update(DataContract.ProfileEntry.CONTENT_URI, userValues,
                DataContract.ProfileEntry._ID + " = ?", new String[]{Integer.toString(userId)});

        if (numOfUpdated != 1) {
            Log.e(TAG, "Error setting UserProfile: ID=" + userId + "; Name=" + userName + "; Last name=" + userLastName + "; Number of updated rows=" + numOfUpdated);
        } else
            Log.d("DataManager", "UserProfile changed: ID=" + userId +
                    "; Name=" + userName + "; LastName=" + userLastName + "; LastMsgId=" + lastMsgId);
    }

    /**
     * Returns user profile from db
     *
     * @param index position of user profile in database
     * @return new instance of UserProfile from database
     */
    public UserProfile getUserProfile(int index) {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.ProfileEntry.CONTENT_URI,
                new String[]{DataContract.ProfileEntry._ID, DataContract.ProfileEntry.COLUMN_NAME,
                        DataContract.ProfileEntry.COLUMN_LAST_NAME, DataContract.ProfileEntry.COLUMN_LAST_MSG_ID},
                null,
                null,
                null);

        UserProfile profile = null;
        try {
            cursor.moveToPosition(index);

            profile = new UserProfile();
            profile.setUserId(cursor.getInt(0));
            profile.setFirstName(cursor.getString(1));
            profile.setLastName(cursor.getString(2));
            profile.setLastMsgId(cursor.getInt(3));

        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        } finally {
            try {
                cursor.close();
            } catch (Exception e) {
            }
        }

        return profile;
    }
    ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////


    // Notification methods ////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    /**
     * Helper method to handle insertion of a new notification in the database.
     *
     * @param senderId   profile id of user that generated the notification
     * @param senderName profile name of user
     * @param body       notification text
     * @param time       that notification has been created
     * @param linkId     id of the post that generated the notification
     * @return the row ID of the added notification.
     */

    public long addNotif(int senderId, String senderName, String body, String time, String linkId) {
        long notifId;

        Cursor cursor = mAppContext.getContentResolver().query(
                NotificationEntry.CONTENT_URI,
                new String[]{DataContract.MessageEntry._ID},
                NotificationEntry.COLUMN_NOTIF_LINK_ID + " = " + "\"" + linkId + "\"",// TODO - change selection
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            setNotification(senderId, senderName, body, time, linkId);
            int notifIdIndex = cursor.getColumnIndex(DataContract.MessageEntry._ID);
            notifId = cursor.getLong(notifIdIndex);
        } else {
            ContentValues values = new ContentValues();

            values.put(NotificationEntry.COLUMN_SENDER_ID, senderId);
            values.put(NotificationEntry.COLUMN_SENDER_NAME, senderName);
            values.put(NotificationEntry.COLUMN_NOTIF_TEXT, body);
            values.put(NotificationEntry.COLUMN_NOTIF_TIME, time);
            values.put(NotificationEntry.COLUMN_NOTIF_LINK_ID, linkId);

            Uri insertedUri = mAppContext.getContentResolver().insert(
                    NotificationEntry.CONTENT_URI,
                    values);

            notifId = ContentUris.parseId(insertedUri);

            if (null == insertedUri) {
                Log.d("DataManager", "Error inserting notification: sender id=" + senderId +
                        "; Sender name=" + senderName + "; Text=" + body + "; Time=" + time);
            } else {
                Log.d("DataManager", "Notification added: sender id=" + senderId +
                        "; Sender name=" + senderName + "; Text=" + body + "; Time=" + time);
            }
        }
        if (cursor != null)
            cursor.close();

        return notifId;
    }

    private void setNotification(int senderId, String senderName, String body, String time, String linkId) {
        // TODO - finish method
    }
    ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////


    // Message methods /////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    /**
     * Helper method to handle insertion of a new message in the database.
     *
     * @param senderId   profile id of user that generated the message
     * @param senderName profile name of user
     * @param body       of the message
     * @param time       that message has been created
     * @param isRead     if message is read-1 or not-0
     * @return the row ID of the added message.
     */
    public long addMessage(int senderId, String senderName, String body, String time, boolean isRead) {
        long msgId;

        // TODO - check if exist in db then add it
// First, check if the location with this city name exists in the db
        Cursor msgCursor = mAppContext.getContentResolver().query(
                DataContract.MessageEntry.CONTENT_URI,
                new String[]{DataContract.MessageEntry._ID},
                DataContract.MessageEntry.COLUMN_MSG_BODY + " = " + "\"" + body + "\"",// TODO - change selection
                null,
                null);

        if (msgCursor != null && msgCursor.moveToFirst()) {
            int msgIdIndex = msgCursor.getColumnIndex(DataContract.MessageEntry._ID);
            msgId = msgCursor.getLong(msgIdIndex);

        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues msgValues = new ContentValues();
            ContentValues profileValues = new ContentValues();


            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            msgValues.put(DataContract.MessageEntry.COLUMN_SENDER_ID, senderId);
            msgValues.put(DataContract.MessageEntry.COLUMN_SENDER_NAME, senderName);
            msgValues.put(DataContract.MessageEntry.COLUMN_MSG_BODY, body);
            msgValues.put(DataContract.MessageEntry.COLUMN_MSG_TIME, time);
            msgValues.put(DataContract.MessageEntry.COLUMN_IS_READ, isRead ? 1 : 0);

            // Finally, insert data into the database.
            Uri insertedUri = mAppContext.getContentResolver().insert(
                    DataContract.MessageEntry.CONTENT_URI,
                    msgValues
            );

            // The resulting URI contains the ID for the row.  Extract the msgId from the Uri.
            msgId = ContentUris.parseId(insertedUri);

            profileValues.put(DataContract.ProfileEntry.COLUMN_LAST_MSG_ID, msgId);
            mAppContext.getContentResolver().update(DataContract.ProfileEntry.CONTENT_URI, profileValues,
                    DataContract.ProfileEntry._ID + " = ?", new String[]{Integer.toString(senderId)});

        }

        if (msgCursor != null)
            msgCursor.close();

        return msgId;
    }
    ////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////


    // Bulletins methods ///////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    /**
     * Method for adding list of Bulletin to database
     * @param list to be added
     */
    public void addBulletins(ArrayList<Bulletin> list) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(DataManager.NO_OF_BULLETIN_TO_DOWNLOAD);

        for (Bulletin b : list) {
            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry.COLUMN_POST_ID, b.getPostID());
            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, b.getUserID());
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, b.getFirstName());
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, b.getLastName());
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, b.getMessage());
            values.put(DataContract.BulletinEntry.COLUMN_DATE, b.getDate().toString());
            values.put(DataContract.BulletinEntry.COLUMN_REPLIES, parseRepliesToJSON(b.getReplies()));
            values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(b.getAttachments()));

            cVVector.add(values);
            Log.d(TAG, "Bulletin added: " + b.toString());
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mAppContext.getContentResolver().bulkInsert(DataContract.BulletinEntry.CONTENT_URI, cvArray);

            // TODO - delete old data
        }
        Log.d(TAG, inserted + " bulletins added");
    }

    /**
     * Inserting new bulletin into the database
     *
     * @param b bulletin to insert
     */
    public void addBulletin(Bulletin b) {
        addBulletin(b.getPostID(), b.getUserID(), b.getFirstName(), b.getLastName(), b.getMessage(),
                b.getDate().toString(), b.getReplies(), b.getAttachments());
    }

    /**
     * Method for adding empty Bulletin to first row into the database, called on first launch of the app
     */
    public void addNullBulletin() {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                new String[]{DataContract.BulletinEntry.COLUMN_POST_ID},
                DataContract.BulletinEntry.COLUMN_POST_ID + " = " + -1,
                null,
                null);

        if (cursor != null && !cursor.moveToFirst()) {
            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, -1);
            values.put(DataContract.BulletinEntry.COLUMN_POST_ID, -1);
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_DATE, "nn");
            values.put(DataContract.BulletinEntry.COLUMN_REPLIES, "");
            values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, "");

            mAppContext.getContentResolver().insert(
                    DataContract.BulletinEntry.CONTENT_URI,
                    values
            );

            Log.d(TAG, "Null bulletin added.");
        }

        if (cursor != null)
            cursor.close();
    }

    /**
     * Helper method to handle the insertion of new bulletin
     *
     * @param postId       bulletin id from the server
     * @param userId       id of user that posted
     * @param userName     name of user
     * @param userLastName last name of user
     * @param text         post text
     * @param date         post date
     * @param replies      list of rost replies
     * @param attachments  list of post attachments
     * @return the row ID of the added bulletin.
     */
    public void addBulletin(int postId, int userId, String userName, String userLastName, String text, String date,
                            LinkedList<Bulletin.Reply> replies, LinkedList<Bulletin.Attachment> attachments) {

        Cursor msgCursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                new String[]{DataContract.BulletinEntry.COLUMN_POST_ID},
                DataContract.BulletinEntry.COLUMN_POST_ID + " = " + postId,
                null,
                null);

        if (msgCursor != null && msgCursor.moveToFirst()) {
            setBulletin(postId, userId, userName, userLastName, text, date, replies, attachments);
        } else {
            ContentValues values = new ContentValues();

            values.put(DataContract.BulletinEntry.COLUMN_POST_ID, postId);
            values.put(DataContract.BulletinEntry.COLUMN_USER_ID, userId);
            values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, userName);
            values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, userLastName);
            values.put(DataContract.BulletinEntry.COLUMN_TEXT, text);
            values.put(DataContract.BulletinEntry.COLUMN_DATE, date);
            values.put(DataContract.BulletinEntry.COLUMN_REPLIES, parseRepliesToJSON(replies));
            values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(attachments));

            Uri insertedUri = mAppContext.getContentResolver().insert(
                    DataContract.BulletinEntry.CONTENT_URI,
                    values
            );

            if (insertedUri == null) {
                Log.e(TAG, "Error inserting Bulletin: ID=" + postId + "; UserID=" + userId + "; Text=" + text.substring(0, 30) + "...");
            } else {
                String debugtext = "Bulletin added: ID=" + postId +
                        "; Name=" + userName + "; LastName=" + userLastName + "; Text=" + text.substring(0, 30)
                        + "...; Date=" + date;
                debugtext += "; Num of replies=";
                debugtext += replies == null ? '0' : Integer.toString(replies.size());
                debugtext += "; Num of attachments=";
                debugtext += attachments == null ? '0' : Integer.toString(attachments.size());
                Log.d(TAG, debugtext);
            }
        }
        if (msgCursor != null)
            msgCursor.close();
    }

    /**
     * Method for changing bulletin in db
     */
    public void setBulletin(int postId, int userId, String userName, String userLastName, String text, String date,
                            LinkedList<Bulletin.Reply> replies, LinkedList<Bulletin.Attachment> attachments) {

        ContentValues values = new ContentValues();

        values.put(DataContract.BulletinEntry.COLUMN_USER_ID, userId);
        values.put(DataContract.BulletinEntry.COLUMN_FIRST_NAME, userName);
        values.put(DataContract.BulletinEntry.COLUMN_LAST_NAME, userLastName);
        values.put(DataContract.BulletinEntry.COLUMN_TEXT, text);
        values.put(DataContract.BulletinEntry.COLUMN_DATE, date);
        values.put(DataContract.BulletinEntry.COLUMN_REPLIES, parseRepliesToJSON(replies));
        values.put(DataContract.BulletinEntry.COLUMN_ATTACHMENTS, parseAttachmentsToJSON(attachments));


        int numOfUpdatedRows = mAppContext.getContentResolver().update(DataContract.BulletinEntry.CONTENT_URI, values,
                DataContract.BulletinEntry.COLUMN_POST_ID + " = ?", new String[]{Integer.toString(postId)});

        if (numOfUpdatedRows != 1) {
            Log.e(TAG, "Error setting bulletin: PostID=" + postId + "; UserID=" + userId + "; Number of rows updated=" + numOfUpdatedRows);
        } else {
            String debugtext = "Bulletin added: ID=" + postId +
                    "; Name=" + userName + "; LastName=" + userLastName + "; Text=" + text.substring(0, 30)
                    + "...; Date=" + date;
            debugtext += "; Num of replies=";
            debugtext += replies == null ? '0' : Integer.toString(replies.size());
            debugtext += "; Num of attachments=";
            debugtext += attachments == null ? '0' : Integer.toString(attachments.size());

            Log.d("BulletinManager", debugtext);
        }
    }

    /**
     * Method for parsing Reply list to JSON String format;
     * @param replies LinkedList of Reply objects
     * @return JSON formatted string
     */
    private String parseRepliesToJSON(LinkedList<Bulletin.Reply> replies) {
        if (replies == null || replies.size() == 0)
            return "";

        JSONObject jObject = new JSONObject();
        try {
            JSONArray jArray = new JSONArray();
            for (Bulletin.Reply r : replies) {
                JSONObject replyJson = new JSONObject();
                replyJson.put(Bulletin.Reply.REPLY_ID, r.replyId);
                replyJson.put(Bulletin.Reply.FIRST_NAME, r.userFirstName);
                replyJson.put(Bulletin.Reply.LAST_NAME, r.userLastName);
                replyJson.put(Bulletin.Reply.USER_ID, r.userId);
                replyJson.put(Bulletin.Reply.TEXT, r.replyText);
                replyJson.put(Bulletin.Reply.DATE, r.replyDate);

                JSONArray replyApprvs = new JSONArray();
                for (UserProfile profile : r.replyApproves) {
                    JSONObject apprvJson = new JSONObject();
                    apprvJson.put(UserProfile.USER_ID, profile.getUserId());
                    apprvJson.put(UserProfile.FIRST_NAME, profile.getFirstName());
                    apprvJson.put(UserProfile.LAST_NAME, profile.getLastName());
                    apprvJson.put(UserProfile.LAST_MSG_ID, profile.getLastMsgId());

                    replyApprvs.put(apprvJson);
                }
                replyJson.put(REPLY_APPRVS, replyApprvs);

                jArray.put(replyJson);
            }
            jObject.put(REPLIES_LIST, jArray);
        } catch (JSONException jse) {
            Log.e(TAG, jse.getLocalizedMessage(), jse);
        }

        return jObject.toString();
    }

    /**
     * Method for parsing Reply list to JSON String format;
     * @param attachments LinkedList of Attachemnt objects
     * @return JSON formatted string
     */
    private String parseAttachmentsToJSON(LinkedList<Bulletin.Attachment> attachments) {
        if (attachments == null || attachments.size() == 0)
            return "";
        return ""; // TODO - finish method
    }

    /**
     * Method for getting the bulletin from the database
     * @param index the position of the bulletin in the database
     * @param cursor the provided cursor for the database
     * @return the new instance of Bulletin from the database
     */
    public Bulletin getBulletin(int index, Cursor cursor) {
        Bulletin bulletin = new Bulletin();
        try {
            cursor.moveToPosition(index);

            bulletin.setUserID(cursor.getInt(BulletinsFragment.COL_USER_ID));
            bulletin.setPostID(cursor.getInt(BulletinsFragment.COL_POST_ID));
            bulletin.setFirstName(cursor.getString(2));
            bulletin.setLastName(cursor.getString(3));
            bulletin.setMessage(cursor.getString(4));
            bulletin.setDate(cursor.getString(5));
            bulletin.setRepliesFromJSON(cursor.getString(6));
            bulletin.setAttachmentsFromJSON(cursor.getString(7));
        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        }
        return bulletin;
    }

    /**
     * Method for getting the bulletin from the database with the default cursor
     * @param index the position of the bulletin in the database
     * @return the new instance of Bulletin from the database
     */
    public Bulletin getBulletin(int index) {
        Cursor cursor = mAppContext.getContentResolver().query(
                DataContract.BulletinEntry.CONTENT_URI,
                BulletinsFragment.BULLETIN_COLUMNS,
                null,
                null,
                null);

        Bulletin bulletin = null;
        try {
            bulletin = getBulletin(index, cursor);
        } catch (NullPointerException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return null;
        }

        return bulletin;
    }

    public void addReplyApprove(int mBulletinPos, int replyId) {
        // TODO - finish method
    }

    public void removeReplyApprove(int mBulletinPos, int replyId) {
        // TODO - finish method
    }

    public void addReplyToBulletin(int mBulletinPos, Bulletin.Reply r) {
        // TODO - finish method
    }
}
