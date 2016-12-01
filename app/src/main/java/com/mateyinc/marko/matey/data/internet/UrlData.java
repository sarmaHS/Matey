package com.mateyinc.marko.matey.data.internet;

public interface UrlData {
    /** Server API version */
    String API_VERSION = "v1";

    /** Server authorisation header */
    String PARAM_AUTH_TYPE ="X-Bearer-Authorization";

    /** Base server url */
    String BASE_URL = "https://matey-api-m4rk07.c9users.io/web/index.php";

    /**
     * URL for downloading and uploading to resource server
     * NOTE: X-Bearer-Authorization required
     */
    String ACCESS_BASE_URL = BASE_URL.concat("/api/").concat(API_VERSION);

    /** Url for registering new device */
    String REGISTER_DEVICE = BASE_URL.concat("/register/device");

    /** The device id retrieved from the server */
    String PARAM_DEVICE_ID = "device_id";

    /** GCM Token param */
    String PARAM_NEW_GCM_ID = "gcm";
    /** GCM Token param */
    String PARAM_OLD_GCM_ID = "old_gcm";

    /////////////////////////////////////////////////////////////////////////////////////
    // Data download params and urls ////////////////////////////////////////////////////

    /** Url for downloading news feed */
     String GET_NEWSFEED_ROUTE = ACCESS_BASE_URL.concat("/newsfeed");

    /** Position parameter of the post in the database for route {@link UrlData#GET_NEWSFEED_ROUTE} */
     String PARAM_START_POS = "start";

    /** Count parameter for post count to download at route {@link UrlData#GET_NEWSFEED_ROUTE} */
     String PARAM_COUNT = "count";
    /////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////////////////////////
    // Data upload params and urls //////////////////////////////////////////////////////

    /** Url for uploading new post to the server. */
    String POST_NEW_BULLETINS_ROUTE = ACCESS_BASE_URL.concat("/post/add");
    /** Interest_id parameter of the selected interest in new post */
    String PARAM_INTEREST_ID = "interest_id";
    /** Text param of new post */
    String PARAM_TEXT_DATA = "text";

    /** Url for uploading new comment to the server. */
    String POST_NEW_REPLY_ROUTE = ACCESS_BASE_URL.concat("/comment/add");
    /** post id parameter of the commented post */
    String PARAM_REPLY_POST_ID = "post_id";
    /** Text param of new comment */
    String PARAM_REPLY_TEXT_DATA = "text";

    /** Url for uploading new like to the server. */
    String POST_NEW_LIKE_ROUTE = ACCESS_BASE_URL.concat("/like/add");
    /** Liked reply post id */
    String PARAM_LIKED_POST_ID = "post_id";
    /** Liked reply id */
    String PARAM_LIKED_REPLY_ID = "text";

    /** Url for uploading new followed friends list to the server. */
    String POST_NEW_FOLLOWED_FRIENDS = ACCESS_BASE_URL.concat("/follower/follow");
    /** User_id param of the followed user */
    String PARAM_FOLLOWED_USER_ID = "user_id";
    /////////////////////////////////////////////////////////////////////////////////////

    // Register new user
    String REGISTER_USER = BASE_URL.concat("/register/user");
    String PARAM_USER_FIRST_NAME = "first_name";
    String PARAM_USER_LAST_NAME = "last_name";
    String PARAM_PASSWORD = "password";
    String PARAM_EMAIL = "email";

    // Login OAuth2
    String OAUTH_LOGIN = BASE_URL.concat("/api/oauth2/token");
    String PARAM_GRANT_TYPE = "grant_type";
    String PARAM_GRANT_TYPE_PASSWORD = "password";
    String PARAM_CLIENT_ID = "client_id";
    String PARAM_CLIENT_ID_VALUE = "1";
    String PARAM_CLIENT_SECRET = "client_secret";
    String PARAM_CLIENT_SECRET_VALUE = "";
    String PARAM_USERNAME = "username";

    // Login user
    String LOGIN_USER = ACCESS_BASE_URL.concat("/login");
    String PARAM_FBTOKEN = "access_token";

    // Facebook login
    String FACEBOOK_LOGIN = OAUTH_LOGIN;
    String PARAM_GRANT_TYPE_SOCIAL = "social_exchange";
    // Path for merging standard account with facebook account
    String FACEBOOK_MERGE = LOGIN_USER.concat("/merge/facebook");
    // Path for merging facebook account with newly created standard account
    String STD_EMAIL_MERGE = LOGIN_USER.concat("/merge/standard");

    // Logout
    String LOGOUT_USER = ACCESS_BASE_URL.concat("/logout");



}