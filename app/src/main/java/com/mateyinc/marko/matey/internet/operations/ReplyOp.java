package com.mateyinc.marko.matey.internet.operations;


import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.ServerStatus;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.UrlData;
import com.mateyinc.marko.matey.model.Reply;
import com.mateyinc.marko.matey.utils.ImageCompress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReplyOp extends Operations {
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
        switch (mOpType) {
            case REPLY_ON_POST: {
                uploadFile();
                return;
            }
            case REPLY_ON_REPLY: {
                url = "#";
                method = Request.Method.POST;
                break;
            }

            default: {
                Log.e(TAG, "No operation type has been specified!");
                url = "#";
                method = Request.Method.POST;
            }
        }

        notifyUI(R.string.upload_started);
        createNewUploadReq(url, method);
        startUpload();

    }

    public void uploadFile() {
//        String[] parts = selectedFilePath.split("/");
//        final String fileName = parts[parts.length - 1];

        if (mContextRef.get() != null)
            notifyUI(R.string.upload_started);


        List<String> mFilePaths = reply.getAttachments();
        List<String> mMarkers = new ArrayList<>();
        LinkedList<File> files = new LinkedList<>();

        // Create list of valid files
        Iterator i = mFilePaths.iterator();
        while (i.hasNext()) {
            // Checking to see if file paths are valid, if not just dismiss them
            String s = (String) i.next();
            File selectedFile = new File(s);
            if (!selectedFile.isFile()) {
//                Toast.makeText(mContextRef.get(), "Source file doesn't exits: " + selectedFile, Toast.LENGTH_LONG).show();
                mMarkers.add(s);
            } else
                files.add(new File(s));
        }

        // Create first part for multipart that contains text
        JSONObject jsonObject = new JSONObject();
        try {
            String message = reply.getReplyText();
            if (!message.isEmpty()) // Is there is no message, send just the subject
                jsonObject.put(CONTENT_FIELD_NAME, message);
            else
                return;

            if (mMarkers.size() != 0) { // Add locations
                JSONArray array = new JSONArray();
                for (String s :
                        mMarkers) {
                    array.put(s);
                }
                jsonObject.put(LOCATIONS_FIELD_NAME, array);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Create builder and add first file
        final OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("json_data", null,
                        RequestBody.create(MediaType.parse("application/json"), jsonObject.toString()));

        // Add attachments
        Iterator it = files.iterator();
        while (it.hasNext()) {
            File file = (File) it.next();
            // Substring last segment and get file name
            String fileName = file.toString().substring(
                    file.toString().lastIndexOf("/") + 1);

            requestBodyBuilder.addFormDataPart(file.toString(), fileName,
                    RequestBody.create(MediaType.parse("image/jpeg"),
                            ImageCompress.compressImageToFile(file, mContextRef.get())));
        }

        // Create network request
        final okhttp3.Request request = new okhttp3.Request.Builder()
                .header(UrlData.PARAM_AUTH_TYPE, "Bearer " + MotherActivity.access_token)
                .url(UrlData.buildNewBulletinReplyUrl(reply.getPostId()))
                .post(requestBodyBuilder.build())
                .build();

        try {
            // Notify user
            Context c = mContextRef.get();

            Response response = client.newCall(request).execute();
            if (c != null) {
                if (!response.isSuccessful()) {
                    String s = response.body().string();
                    Log.e(TAG, "Upload failed: " + s);
//                    reply.onUploadFailed(s, c);


                    if (mUploadListener != null)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mUploadListener.onUploadFailed();
                            }
                        });

                    for (Reply r :
                            DataAccess.getInstance(c).getBulletinById(this.reply.getPostId()).getReplies()) {
                        if (r.equals(this.reply))
                            r.setServerStatus(ServerStatus.STATUS_RETRY_UPLOAD);
                    }
//                    notifyUI(R.string.upload_failed);
                } else {
                    String s = response.body().string();
                    Log.d(TAG, "Upload success: " + s);
                    for (Reply r :
                            DataAccess.getInstance(c).getBulletinById(this.reply.getPostId()).getReplies()) {
                        if (r.equals(this.reply))
                            r.setServerStatus(ServerStatus.STATUS_SUCCESS);
                    }
                    if (mUploadListener != null)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mUploadListener.onUploadSuccess();
                            }
                        });
                    notifyUI(R.string.upload_success);
                }
            }
            // TODO - finish response parsing

            System.out.println(response.body().string());
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
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

        String errorDesc;

        try {
            errorDesc = new String(error.networkResponse.data);
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            return;
        }

        if (c != null) {
            reply.onUploadFailed(errorDesc, c);
        }
    }

    @Override
    protected String getTag() {
        return null;
    }
}