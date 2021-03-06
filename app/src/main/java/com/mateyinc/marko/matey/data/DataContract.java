/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mateyinc.marko.matey.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;

/**
 * Defines table and column names for the  database.
 */
public class DataContract {

    // Name for the content mProvider
    public static final String CONTENT_AUTHORITY = "com.mateyinc.marko.matey";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content mProvider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.mateyinc.marko.matey/messages/ is a valid path for
    // looking at messages data.
    public static final String PATH_MESSAGES = "messages";
    public static final String PATH_NOTIFICATIONS = "notifications";
    public static final String PATH_PROFILES = "profiles";
    public static final String PATH_BULLETINS = "bulletins";
    public static final String PATH_BULLETIN_REPLIES = "bulletin_replies";
    public static final String PATH_REPLY_REPLIES = "reply_replies";
    public static final String PATH_APPROVES = "approves";
    public static final String PATH_NOT_UPLOADED = "not_uploaded";



    /* Inner class that defines the table contents of the messages table */
    public static final class MessageEntry implements MBaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MESSAGES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MESSAGES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MESSAGES;

        // Table name
        public static final String TABLE_NAME = "messages";

        // Columns
        public static final String COLUMN_SENDER_ID = "msg_sender_id";
        public static final String COLUMN_SENDER_NAME = "msg_sender_name";
        public static final String COLUMN_MSG_BODY = "msg_body";
        public static final String COLUMN_MSG_TIME = "msg_time";
        public static final String COLUMN_IS_READ = "msg_isread";

        public static Uri buildMessageUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    /* Inner class that defines the table contents of the notifications table */
    public static final class NotificationEntry implements MBaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOTIFICATIONS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTIFICATIONS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOTIFICATIONS;

        public static final String TABLE_NAME = "notifications";

        // Adding columns DbHelper create table command  must be changed also
        public static final String COLUMN_SENDER_ID = "notif_sender_id";
        public static final String COLUMN_SENDER_NAME = "notif_sender_name";
        public static final String COLUMN_NOTIF_TEXT = "notif_text";
        public static final String COLUMN_NOTIF_TIME = "notif_time";
        public static final String COLUMN_NOTIF_LINK_ID = "notif_link_id";

        public static Uri buildNotifUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ProfileEntry implements MBaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PROFILES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROFILES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PROFILES;

        public static final String TABLE_NAME = "profiles";

        // Adding columns DbHelper create table command  must be changed also
        public static final String COLUMN_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_PROF_PIC = "picture";
        public static final String COLUMN_COVER_PIC = "cover_pic";
        public static final String COLUMN_FOLLOWING_NUM = "following_num";
        public static final String COLUMN_FOLLOWERS_NUM = "followers_num";
        public static final String COLUMN_VERIFIED = "verified";
        public static final String COLUMN_FIRST_LOGIN = "first_login";
        public static final String COLUMN_FOLLOWING = "following";
        public static final String COLUMN_FOLLOWED = "followed";
        public static final String COLUMN_LAST_MSG_ID = "profile_last_msg_id";

        public static Uri buildProfileUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class BulletinEntry implements MBaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BULLETINS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BULLETINS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BULLETINS;

        public static final String TABLE_NAME = "bulletins";

        // Adding columns DbHelper create table command  must be changed also
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_TEXT = "post_text";
        public static final String COLUMN_SUBJECT = "post_subject";
        public static final String COLUMN_DATE = "post_date";
        public static final String COLUMN_ATTACHMENTS = "post_attachments";
        public static final String COLUMN_NUM_OF_REPLIES = "bulletin_num_of_replies";
        public static final String COLUMN_NUM_OF_LIKES = "bulletin_num_of_likes";

        public static String DEFAULT_SORT = " DESC";

        public static Uri buildBulletinUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildBulletinsWithApproves() {
            return CONTENT_URI.buildUpon().appendPath("both").build();
        }
    }

    public static final class ReReplyEntry implements MBaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REPLY_REPLIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REPLY_REPLIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REPLY_REPLIES;

        public static final String TABLE_NAME = "re_replies";

        // Adding columns DbHelper create table command  must be changed also
        public static final String COLUMN_REPLY_ID = "post_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_TEXT = "re_reply_text";
        public static final String COLUMN_DATE = "re_reply_date";
        public static final String COLUMN_NUM_OF_LIKES = "re_reply_num_of_likes";

        public static Uri buildReReplyUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ReplyEntry implements MBaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BULLETIN_REPLIES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BULLETIN_REPLIES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BULLETIN_REPLIES;

        public static final String TABLE_NAME = "replies";

        // Adding columns DbHelper create table command  must be changed also
        public static final String COLUMN_POST_ID = "post_id";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_FIRST_NAME = "first_name";
        public static final String COLUMN_LAST_NAME = "last_name";
        public static final String COLUMN_TEXT = "reply_text";
        public static final String COLUMN_DATE = "reply_date";
        public static final String COLUMN_NUM_OF_LIKES = "reply_num_of_likes";
        public static final String COLUMN_NUM_OF_REPLIES = "reply_num_of_replies";

        public static Uri buildReplyUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildBulletinRepliesUri(long bulletin_id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(bulletin_id)).build();
        }
    }

    public static final class ApproveEntry implements MBaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_APPROVES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_APPROVES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_APPROVES;

        public static final String TABLE_NAME = "approves";

        // Adding columns DbHelper create table command  must be changed also
        public static final String COLUMN_POST_ID = "post_id";
        public static final String COLUMN_REPLY_ID = "reply_id";
        public static final String COLUMN_USER_ID = "user_id";

        public static Uri buildApproveUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class NotUploadedEntry implements MBaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NOT_UPLOADED).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOT_UPLOADED;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_NOT_UPLOADED;

        public static final String TABLE_NAME = "not_uploaded_items";

        /**
         * Entry type value can be class name from model package
         */
        public static final String COLUMN_ENTRY_TYPE = "entry_type";

        public static Uri buildNotUploadedUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}
