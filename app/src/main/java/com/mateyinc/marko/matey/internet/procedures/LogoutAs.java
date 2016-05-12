package com.mateyinc.marko.matey.internet.procedures;

import android.os.AsyncTask;
import android.os.Bundle;

import com.mateyinc.marko.matey.activity.main.MainActivity;
import com.mateyinc.marko.matey.data_and_managers.UrlData;
import com.mateyinc.marko.matey.internet.http.HTTP;

import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by M4rk0 on 4/25/2016.
 */
public class LogoutAs extends AsyncTask<String,Void,String> {

    MainActivity activity;

    public LogoutAs (MainActivity activity) {
        this.activity = activity;
    }

    @Override
    protected String doInBackground(String... params) {

        if(!isCancelled()) {

            String email = params[0];
            String uid = params[1];
            String device_id = params[2];

            try {

                String data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8") + "&" +
                        URLEncoder.encode("uid", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8") + "&" +
                        URLEncoder.encode("device_id", "UTF-8") + "=" + URLEncoder.encode(device_id, "UTF-8");
                HTTP http = new HTTP(UrlData.LOG_OUT_URL, "POST");

                if(http.sendPost(data)) return http.getData();

            } catch (Exception e) {

                return null;

            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {

        try{

            // if there is some result check if successful
            if (result != null) {

                JSONObject jsonObject = new JSONObject(result);

                // if successful, set everything to SecurePreferences
                if (jsonObject.getBoolean("success")) {

                    activity.clearUserCredentials();
                    activity.startTommy();

                } else if(!jsonObject.getBoolean("success")){

                    Bundle bundle = new Bundle();
                    bundle.putString("message", jsonObject.getString("message"));
                    activity.showDialog(4, bundle);

                } else throw new Exception();

            } else throw new Exception();

        } catch (Exception e) {
            // if there was an error, show corresponding alert dialog
            activity.showDialog(0);
        }

    }

}
