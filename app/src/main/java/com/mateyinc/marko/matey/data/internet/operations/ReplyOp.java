package com.mateyinc.marko.matey.data.internet.operations;


import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.model.Reply;

public class ReplyOp extends Operations{
    private static final String TAG = ReplyOp.class.getSimpleName();

    Reply reply;

    public ReplyOp(Context context, Reply r) {
        super(context);
        reply = r;
    }

    @Override
    public void startDownloadAction() {

    }

    @Override
    protected void onDownloadSuccess(String response) {
        Context c = mContextRef.get();

        if (c != null) {
            reply.onDownloadSuccess(response, c);
        }
    }

    @Override
    protected void onDownloadFailed(VolleyError error) {
        Context c = mContextRef.get();

        if (c != null) {
            reply.onDownloadFailed(new String(error.networkResponse.data), c);
        }
    }

    @Override
    public void startUploadAction() {
        String url;
        int method;
        switch (mOpType){
            case REPLY_ON_POST: {
                url = "";
                method = Request.Method.POST;
                break;
            }
            case REPLY_ON_REPLY: {
                url = "";
                method = Request.Method.POST;
                break;
            }

            default:{
                Log.e(TAG, "No operation type has been specified!");
                url = "#";
                method = Request.Method.POST;
            }
        }

        createNewUploadReq(url, method);
        startUpload();

    }

    @Override
    protected void onUploadSuccess(String response) {
        Context c = mContextRef.get();

        if (c != null) {
            reply.onUploadSuccess(response, c);
        }
    }

    @Override
    protected void onUploadFailed(VolleyError error) {
        Context c = mContextRef.get();

        if (c != null) {
            reply.onUploadFailed(new String(error.networkResponse.data), c);
        }
    }

    @Override
    protected String getTag() {
        return null;
    }
}
