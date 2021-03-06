package com.mateyinc.marko.matey.internet;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.DataContract.ProfileEntry;
import com.mateyinc.marko.matey.data.IdGenerator;
import com.mateyinc.marko.matey.data.ServerStatus;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.operations.NewsfeedOp;
import com.mateyinc.marko.matey.internet.operations.OperationType;
import com.mateyinc.marko.matey.internet.operations.Operations;
import com.mateyinc.marko.matey.internet.operations.UserProfileOp;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.MModel;
import com.mateyinc.marko.matey.model.Reply;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Helper class used for recording user interaction with models
 */
public class OperationManager implements OperationProvider {
    private static final String TAG = OperationManager.class.getSimpleName();

    public static final int CACHE_SIZE_MB = 100; // Max cache size on disk for storing data
    // For broadcast IntentFilters
    public static final String BULLETIN_LIST_LOAD_FAILED = "com.mateyinc.marko.matey.internet.home.bulletins_load_failed";
    public static final String BULLETIN_LIST_LOADED = "com.mateyinc.marko.matey.internet.home.bulletins_loaded";
    public static final String EXTRA_ITEM_DOWNLOADED_COUNT = "com.mateyinc.marko.matey.internet.home.bulletins_loaded_count";

    // One day in milliseconds
    public static final int ONE_DAY = 86400000;
    // One minute in milliseconds
    public static final int ONE_MIN = 60000;

    /**
     * Number of bulletins to startDownloadAction from the server
     */
    public static int NUM_OF_BULLETIN_TO_DOWNLOAD = 40; // TODO - define how much bulletin will be downloaded at once;

    /**
     * The current page of bulletins in the database
     */
    public static int mCurrentPage = 0;

    private IdGenerator mIdGenerator;

    public final ImageLoader mImageLoader;

    // Global instance fields
    private static OperationManager mInstance;
    private Context mAppContext;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(10);
    private RequestQueue mRequestQueue;
    private Response.Listener<String> mSuccessListener;
    private Response.ErrorListener mErrorListener;


    /**
     * Method for retrieving OperationManager singleton
     *
     * @param context the context of calling class
     * @return the OperationManager instance
     */
    public static synchronized OperationManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new OperationManager(context);
//                mInstance.addNullBulletin();
            Log.d(TAG, "New instance of OperationManager created.");
        }

        return mInstance;
    }

    private OperationManager(Context context) {
        mIdGenerator = new IdGenerator(context);
        mAppContext = context.getApplicationContext();
        mRequestQueue = Volley.newRequestQueue(context);

        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }


    //  General methods ////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    @Override
    public void submitRequest(Request request) {
        mRequestQueue.add(request);
    }

    @Override
    public void submitRunnable(Runnable runnable) {
        mExecutor.submit(runnable);
    }

    @Override
    public long generateId() {
        return mIdGenerator.generateId();
    }

    @Override
    public String getAccessToken() {
        return MotherActivity.access_token;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public void uploadFailedData(final String modelType, final MModel model, final Context context) {
        submitRunnable(new Runnable() {
            @Override
            public void run() {
                if (modelType.equals(Bulletin.class.getSimpleName())) {
                    Bulletin b = (Bulletin) model;
                    b.setServerStatus(ServerStatus.STATUS_UPLOADING);
                    b.save(context);
                }
            }
        });
    }


    // UserProfileOps methods /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////

    private LinkedList<UserProfile> suggestedFriends;

    public LinkedList<UserProfile> getSuggestedFriendsList() {
        return suggestedFriends;
    }

    public void setSuggestedFriends(LinkedList<UserProfile> list) {
        suggestedFriends = list;
    }


    /**
     * Method for adding list of user profile to the database
     *
     * @param list        the list of user profiles ready for the database
     * @param areFollowed indicates if these profiles are followed by the current user
     */
    public void addUserProfiles(ArrayList<UserProfile> list, boolean areFollowed) {
        Vector<ContentValues> cVVector = new Vector<ContentValues>(OperationManager.NUM_OF_BULLETIN_TO_DOWNLOAD);

        for (UserProfile profile : list) {
            ContentValues userValues = new ContentValues();

            userValues.put(ProfileEntry._ID, profile.getUserId());
            userValues.put(ProfileEntry.COLUMN_NAME, profile.getFirstName());
            userValues.put(ProfileEntry.COLUMN_LAST_NAME, profile.getLastName());
            userValues.put(ProfileEntry.COLUMN_EMAIL, profile.getEmail());
            userValues.put(ProfileEntry.COLUMN_PROF_PIC, profile.getProfilePictureLink());
            userValues.put(ProfileEntry.COLUMN_FOLLOWING, profile.isFriend() ? 1 : 0);
            userValues.put(ProfileEntry.COLUMN_LAST_MSG_ID, profile.getLastMsgId());
            if (areFollowed)
                userValues.put(ProfileEntry.COLUMN_FOLLOWING, true);

            cVVector.add(userValues);
            Log.d(TAG, "Bulletin added: " + userValues.toString());
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            inserted = mAppContext.getContentResolver().bulkInsert(ProfileEntry.CONTENT_URI, cvArray);
            // TODO - delete old data
        }
        Log.d(TAG, inserted + " user profile added.");
    }

    public Context getContext() {
        return mAppContext;
    }

    /// Bulletins methods   ////
    ////////////////////////////

    /**
     * Method for downloading and parsing news feed from the server, and all data around it
     *
     * @param requestNewData boolean to indicate is new data should be downloaded and old cleared.
     * @param context        the Context used for notifying when the parsing result is complete
     */
    public void downloadNewsFeed(boolean requestNewData, MotherActivity context) {
        NewsfeedOp newsfeedOp = new NewsfeedOp(this, context);
        if (requestNewData)
            newsfeedOp.setOperationType(OperationType.DOWNLOAD_NEWS_FEED_NEW);
        newsfeedOp.addFailedListener(mErrorListener);
        newsfeedOp.addSuccessListener(mSuccessListener);
        newsfeedOp.startDownloadAction();
    }

    /**
     * Helper method for downloading news feed from the server to the database;
     * Downloads {@value OperationManager#NUM_OF_BULLETIN_TO_DOWNLOAD} bulletins from the server;
     * Automatically determines from what bulletin position to startDownloadAction by calling {@link DataAccess#getNumOfBulletinsInDb(Context)} ()}
     *
     * @param context the context of activity which is calling this method
     */
    public void downloadNewsFeed(final MotherActivity context) {
        downloadNewsFeed(false, context);
    }

    /**
     * Helper method for posting new bulletin with attachments.
     * <p><u>NOTE:</u> Method is async.</p>
     *
     * @param subject     bulletin's subject
     * @param message     bulletin's text
     * @param attachments bulletin's attachment list that contains file paths.
     */
    public void postNewBulletin(String subject, String message, @Nullable List<String> attachments, final Context context) {
        final Bulletin b = new Bulletin(MotherActivity.user_id, MotherActivity.mCurrentUserProfile.getFirstName(),
                MotherActivity.mCurrentUserProfile.getLastName(), subject, message, new Date());
        b.setAttachments(attachments);
        b.setId(mIdGenerator.generateId());

        submitRunnable(new Runnable() {
            @Override
            public void run() {
                b.save(context);
            }
        });
    }

    /**
     * Helper method for posting new bulletin without attachments.
     * <p><u>NOTE:</u> Method is async.</p>
     *
     * @param subject bulletin's subject.
     * @param message bulletin's text.
     */
    public void postNewBulletin(String subject, String message, Context context) {
        postNewBulletin(subject, message, null, context);
    }

    /**
     * Helper method for liking/unliking bulletin
     * <p><u>NOTE:</u> Method is async.</p>
     *
     * @param b       bulletitn to be liked/unliked
     * @param context context for database communication
     */
    public void newPostLike(final Bulletin b, final Context context) {
        submitRunnable(new Runnable() {
            @Override
            public void run() {
                if (b != null)
                    b.like(context);
            }
        });
    }

    /**
     * Helper method for liking/unliking bulletin
     * <p><u>NOTE:</u> Method is async.</p>
     *
     * @param bulletinPosition position of bulletin in database
     * @param context          context for database communication
     */
    public void newPostLike(int bulletinPosition, Context context) {
        Bulletin b = DataAccess.getBulletin(bulletinPosition, context);
        newPostLike(b, context);

    }


    //// Reply methods         /////
    ////////////////////////////////

    /**
     * Helper method for liking/unliking reply
     * <p><u>NOTE:</u> Method is async.</p>
     *
     * @param replyPosition position of reply in database
     * @param context       context for database communication
     */
    public void newReplyLike(final int replyPosition, final Context context) {
        submitRunnable(new Runnable() {
            @Override
            public void run() {
                Reply r = DataAccess.getReply(replyPosition, context);
                if (r != null)
                    r.like(context);
            }
        });
    }

    /**
     * Helper method for posting new reply on bulletin
     *
     * @param postId  id of bulletin that is beign replied on
     * @param r       {@link Reply} new reply object
     * @param context context used for database communication
     */
    public void postNewReply(Reply r, long postId, Context context) {
        r.reply(context, postId);
    }

    /**
     * Helper method for posting new reply on bulletin
     *
     * @param bulletin {@link Bulletin} bulletin to reply on
     * @param text     reply text
     * @param context  context used for database communication
     */
    public void postNewReply(String text, Bulletin bulletin, Context context) {
        Reply r = new Reply();
        r.setUserId(MotherActivity.user_id);
        r.setUserFirstName(MotherActivity.mCurrentUserProfile.getFirstName());
        r.setUserLastName(MotherActivity.mCurrentUserProfile.getFirstName());
        r.setReplyText(text);
        r.setPostId(bulletin.getPostID());

        postNewReply(r, bulletin.getPostID(), context);
    }

    /**
     * Helper method for posting new reply on bulletin
     *
     * @param postId  id of bulletin to reply on
     * @param text    reply text
     * @param context context used for database communication
     */
    public void postNewReply(String text, long postId, Context context) {
        Reply r = new Reply();
        r.setUserId(MotherActivity.user_id);
        r.setUserFirstName(MotherActivity.mCurrentUserProfile.getFirstName());
        r.setUserLastName(MotherActivity.mCurrentUserProfile.getFirstName());
        r.setReplyText(text);
        r.setPostId(postId);

        postNewReply(r, postId, context);
    }

    /**
     * Helper method for posting new reply on reply
     *
     * @param reply   {@link Reply} reply to reply on
     * @param r       {@link Reply} new reply object
     * @param context context used for database communication
     */
    public void postNewReply(Reply r, Reply reply, Context context) {
        r.reply(context, reply);
    }

    //// User profile methods ///////
    /////////////////////////////////

    /**
     * Helper method for downloading user profile data from the server;
     *
     * @param userId id of the user profile
     */
    public void downloadUserProfile(long userId, MotherActivity context) {
        UserProfileOp op = new UserProfileOp(context);
        setListeners(op);
        op.setOperationType(OperationType.DOWNLOAD_USER_PROFILE);
        op.setUserId(userId).startDownloadAction();
    }

    /**
     * Helper method for downloading user followers.
     *
     * @param offset  starting position of followers;
     * @param count   offset indicates how much will be downloaded;
     * @param context {@link MotherActivity} context used for download/save ops;
     */
    public void downloadFollowers(int offset, int count, long id, MotherActivity context) {
        UserProfileOp op = new UserProfileOp(context);
        setListeners(op);
        op.setOperationType(OperationType.DOWNLOAD_FOLLOWERS);
        op.setCount(count).setOffset(offset).setUserId(id)
                .startDownloadAction();
    }

    /**
     * Helper method to use when new user profile has been followed.
     * Updates followed user profile database.
     *
     * @param userId  id of the user being followed;
     * @param context {@link MotherActivity} context used for upload/save operations;
     */
    public void followNewUser(long userId, MotherActivity context) {
        UserProfileOp operation = new UserProfileOp(context);
        setListeners(operation);
        operation.setOperationType(OperationType.FOLLOW_USER_PROFILE);
        operation.setUserId(userId).startUploadAction();
    }

    /**
     * Helper method to use when user profile has been unfollowed.
     * Updates unfollowed user profile database.
     *
     * @param userId  id of unfollowed user
     * @param context {@link MotherActivity} context used for upload/save operations;
     */
    public void unfollowUser(long userId, MotherActivity context) {
        UserProfileOp operation = new UserProfileOp(context);
        setListeners(operation);
        operation.setOperationType(OperationType.UNFOLLOW_USER_PROFILE);
        operation.setUserId(userId).startUploadAction();
    }

    /**
     * Method for adding custom success download/upload listener
     *
     * @param listener listener to be added
     */
    public void addSuccessListener(Response.Listener<String> listener) {
        mSuccessListener = listener;
    }

    /**
     * Method for adding custom error download/upload listener
     *
     * @param listener listener to be added
     */
    public void addErrorListener(Response.ErrorListener listener) {
        mErrorListener = listener;
    }

    /**
     * Method for setting operation listeners if they exits, otherwise they will be set by default
     *
     * @param operation {@link Operations} to set listeners on
     */
    private void setListeners(Operations operation) {
        if (null != mSuccessListener)
            operation.addSuccessListener(mSuccessListener);

        if (null != mErrorListener)
            operation.addFailedListener(mErrorListener);
    }
}

