package com.hackathon.harmalarm.service;

import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;

import com.bottlerocketstudios.groundcontrol.agent.AbstractAgent;
import com.hackathon.harmalarm.TemperatureEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Ryan Timmons on 2/26/16.
 */
public class DataAgent extends AbstractAgent<List<TemperatureEntry>, Integer> {

    private final Context mContext;
    private boolean mCancelled;
    private OkHttpClient mHttpClient = new OkHttpClient();
    private static final String TEMPERATURE_URL = "https://run-east.att.io/7fc276f7921c6/13003d3dc6f3/3eb75aeb1fdbd7a/in/flow/tempReadings";

    public DataAgent(Context context) {
        mContext = context;
    }

    @Override
    public String getUniqueIdentifier() {
        return DataAgent.class.getCanonicalName();
    }

    @Override
    public void onProgressUpdateRequested() {
        // Do nothing
    }

    @Override
    public void run() {
        String response = run(TEMPERATURE_URL);
        try {
            List<TemperatureEntry> temperatureEntryList = new ArrayList<>();
            if (!("").equals(response)) {
                JSONArray responseJsonArray = sanitizeJsonString(response);
                for (int i = 0; i < responseJsonArray.length(); i++) {
                    temperatureEntryList.add(temperatureEntryFromJson(responseJsonArray.getJSONObject(i)));
                }
                getAgentListener().onCompletion(getUniqueIdentifier(), temperatureEntryList);
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.e(getUniqueIdentifier(), "Error with parsing JSON.");
        }
    }

    private String run(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Log.i(getUniqueIdentifier(), "Creating response for " + url);

        try {
            Response response = mHttpClient.newCall(request).execute();
            Log.i(getUniqueIdentifier(), "Got Response: " + response);
            return response.body().string();
        } catch (IOException ex) {
            ex.printStackTrace();
            Log.e(getUniqueIdentifier(), "Error with call.");
            return "";
        }
    }

    private TemperatureEntry temperatureEntryFromJson(JSONObject json) throws JSONException {
        String temperature = json.getString("value");
        String date = json.getString("timestamp");
        return new TemperatureEntry((int) Double.parseDouble(temperature), date);
    }

    @Override
    public void cancel() {
        mCancelled = true;
    }

    private JSONArray sanitizeJsonString(String rawContent) {
        try {
            String working = rawContent.substring(rawContent.indexOf('{'));
            String rawJsonString = new JSONObject(working).getString("raw");
            return new JSONObject(rawJsonString).getJSONArray("values");
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
