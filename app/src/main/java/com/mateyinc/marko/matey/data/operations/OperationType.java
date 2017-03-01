package com.mateyinc.marko.matey.data.operations;


/**
 * Operation type that can be used in {@link Operations}
 */
public enum OperationType{
    DOWNLOAD_NEWS_FEED,
    BULLETIN_OP,

    // User profile operations
    DOWNLOAD_USER_PROFILE,
    FOLLOW_USER_PROFILE,
    UNFOLLOW_USER_PROFILE,
    DOWNLOAD_FOLLOWERS,

    NO_OPERATION
}
