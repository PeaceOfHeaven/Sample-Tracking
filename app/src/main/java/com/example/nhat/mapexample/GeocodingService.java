package com.example.nhat.mapexample;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.nhat.mapexample.data.SamplesDataSource;
import com.example.nhat.mapexample.data.remote.SamplesRemoteStorage;
import com.example.nhat.mapexample.samples.SamplesActivity;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Nhat on 10/27/2016.
 */

public class GeocodingService extends IntentService {

    private final String TAG = GeocodingService.class.getSimpleName();

    public static final String ACTION_GEOCODING = "geocoding";

    private static final String SAMPLE_ID_KEY = "sample_id";
    private static final String LATLONG_KEY = "latlng";

    private final String mBaseUrl = "https://maps.googleapis.com/maps/api/geocode/json";

    public static Intent getStartIntent(Context context, Sample location) {
        Intent intent = new Intent(context, GeocodingService.class);
        intent.setAction(ACTION_GEOCODING);
        intent.putExtra(SAMPLE_ID_KEY, location.getSampleId());
        intent.putExtra(LATLONG_KEY, location.getLatitude() + "," + location.getLongitude());
        return intent;
    }

    private static final Intent mUpdatedAddressIntent;

    static {
        mUpdatedAddressIntent = new Intent(SamplesActivity.UPDATED_ADDRESS_ACTION);
    }

    public GeocodingService() {
        super("GeocodingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(intent.getAction().equals(ACTION_GEOCODING)) {
            if(intent.hasExtra(SAMPLE_ID_KEY) && intent.hasExtra(LATLONG_KEY)) {
                final String sampleId = intent.getStringExtra(SAMPLE_ID_KEY);
                String latlong = intent.getStringExtra(LATLONG_KEY);

                StringBuilder builder = new StringBuilder();
                builder.append(mBaseUrl)
                        .append("?latlng=").append(latlong)
                        .append("&key=").append(getString(R.string.google_geocoding_key));

                InputStream inputStream = null;
                String content = "";
                try {
                    URL url = new URL(builder.toString());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(3000);

                    int responseCode = connection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK) {
                        inputStream = connection.getInputStream();
                        BufferedReader reader  = new BufferedReader(new InputStreamReader(inputStream));

                        builder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                        content = builder.toString();
                        Log.d(TAG, content);

                        final String formattedAddress = parseJsonContent(content);
                        if(!TextUtils.isEmpty(formattedAddress)) {
                            final SamplesRemoteStorage samplesRemoteStorage = SamplesRemoteStorage.getInstance(this);

                            Log.d(TAG, formattedAddress);
                            samplesRemoteStorage.updateSampleAddress(sampleId, formattedAddress, new SamplesDataSource.CRUDSampleCallback() {
                                @Override
                                public void onSuccess(Sample sample) {
                                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(mUpdatedAddressIntent);
                                }

                                @Override
                                public void onError() {
                                    Log.d(TAG, "Cannot save formatted address");
                                }
                            });
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    if(inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private String parseJsonContent(String content) throws JSONException {
        String formattedAddress = "";
        JSONObject root = new JSONObject(content);
        if(root.has("results")) {
            JSONArray results = root.getJSONArray("results");
            JSONObject o = (JSONObject) results.get(0);
            if (o != null && o.has("formatted_address")) {
                formattedAddress = o.getString("formatted_address");
            }
        }
        return formattedAddress;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
