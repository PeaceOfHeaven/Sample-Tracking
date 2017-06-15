package com.example.nhat.mapexample.data.remote.fusiontable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseIntArray;

import com.example.nhat.mapexample.data.remote.SampleRESTApi;
import com.example.nhat.mapexample.samples.SamplesSearchType;
import com.example.nhat.mapexample.samples.domain.model.Sample;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.model.Sqlresponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Nhat on 11/12/2016.
 */

public class SamplesFusionTableApi implements SampleRESTApi<Sample> {

    private final String TAG = SamplesFusionTableApi.class.getSimpleName();

    private static final String SAMPLES_TABLE_ID = "1DfvSto3LY77I4BqOAzf6V-A4JxP3dnO5kiw-a-5V";

    private static SamplesFusionTableApi INSTANCE;

    /**
     * Be sure to specify the name of your application. If the application name is {@code null} or
     * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
     */
    private static final String APPLICATION_NAME = "FusionTableApi";

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport httpTransport;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final String INSERT_SAMPLE_SQL_COMMAND = "INSERT INTO " + SAMPLES_TABLE_ID + " (sample_id,name,result,address,location,date) "
            + "VALUES (%s,'%s','%s','%s','%s','%s')";

    private final String SELECT_BY_ID_SQL_COMMAND = "SELECT * FROM " + SAMPLES_TABLE_ID + " WHERE sample_id=%s";

    private static final String SELECT_SAMPLES_SQL_COMMAND = "SELECT ROWID,sample_id,name,result,address,location,date FROM " + SAMPLES_TABLE_ID;

    private final String UPDATE_SAMPLE_ADDRESS_SQL_COMMAND = "UPDATE " + SAMPLES_TABLE_ID + " SET address='%s' WHERE ROWID ='%d'";

    private final String UPDATE_SAMPLE_SQL_COMMAND = "UPDATE " + SAMPLES_TABLE_ID + " SET address='%s',result='%s' WHERE ROWID ='%d'";

    private final String DELETE_SAMPLE_SQL_COMMAND = "DELETE FROM " + SAMPLES_TABLE_ID + " WHERE ROWID='%d'";

    public static final String buildSearchSqlCommand(String searchValue, SamplesSearchType samplesSearchType) {
        String sqlCommand = SELECT_SAMPLES_SQL_COMMAND;

        if(samplesSearchType == SamplesSearchType.ALL) {
            if(isIdSearchType(searchValue)) {
                samplesSearchType = SamplesSearchType.ID;
            } else if(isNameSearchType(searchValue)){
                samplesSearchType = SamplesSearchType.NAME;
            } else {
                samplesSearchType = SamplesSearchType.DATE;
            }
        }
        switch (samplesSearchType) {
            case NAME:
                sqlCommand += " WHERE name matches '%" + searchValue + "%'";
                break;
            case ID:
                sqlCommand += " WHERE sample_id='" + searchValue + "'";
                break;
            case DATE:
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date date = null;
                try {
                    date = sdf.parse(searchValue);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return "";
                }
                date.setDate(date.getDate() + 1);
                long milliseconds = date != null ? date.getTime() : -1;
                sqlCommand += " WHERE date <='" + milliseconds + "'";
                break;
        }
        return sqlCommand;
    }

    private static boolean isIdSearchType(String searchValue) {
        try {
            Integer.parseInt(searchValue.trim());
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private static boolean isNameSearchType(String searchValue) {
        return !searchValue.trim().contains("/") ? true : false;
    }

    private Fusiontables fusiontables;

    private final SparseIntArray mRowSampleIdsMapper;

    public static SamplesFusionTableApi getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SamplesFusionTableApi(context);
        }
        return INSTANCE;
    }

    private SamplesFusionTableApi(Context context) {
        checkNotNull(context);
        mRowSampleIdsMapper = new SparseIntArray();

        try {
            httpTransport = new NetHttpTransport();
            Credential credential = FusionTableApiUtil.authorize(context, httpTransport, JSON_FACTORY);

            // set up global FusionTables instance
            fusiontables = new Fusiontables.Builder(
                    httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean insert(@NonNull Sample entry) {
        checkNotNull(entry);

        boolean success = false;
        try {
            Fusiontables.Query.Sql sql = fusiontables.query()
                    .sql(String.format(INSERT_SAMPLE_SQL_COMMAND
                                , entry.getSampleId()
                                , entry.getName()
                                , entry.getResult()
                                , entry.getAddress()
                                , entry.getLatitude() + "," + entry.getLongitude()
                                , String.valueOf(entry.getTime())
                            )
                        );

            Sqlresponse response = sql.execute();

            JSONObject responseJson = new JSONObject(response);
            JSONArray rows = responseJson.getJSONArray("rows");
            int rowId = rows.getJSONArray(0).getInt(0);

            mRowSampleIdsMapper.put(Integer.parseInt(entry.getSampleId()), rowId);

            success = true;
        } catch (IllegalArgumentException e) {
            // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
            // been thrown.
            // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
            // http://code.google.com/p/google-api-java-client/issues/detail?id=545
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public boolean editSample(@NonNull Sample sample) {
        checkNotNull(sample);

        try {
            Fusiontables.Query.Sql sql = fusiontables.query()
                    .sql(String.format(UPDATE_SAMPLE_SQL_COMMAND
                            , sample.getAddress()
                            , sample.getResult()
                            , mRowSampleIdsMapper.get(Integer.parseInt(sample.getSampleId()))
                            )
                    );

            Sqlresponse response = sql.execute();
            Log.d(TAG, response.toString());

            JSONObject responseJson = new JSONObject(response.toString());
            JSONArray rows = responseJson.getJSONArray("rows");
            int affectedRows = rows.getJSONArray(0).getInt(0);

            return affectedRows != 0 ? true : false;
        } catch (IllegalArgumentException e) {
            // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
            // been thrown.
            // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
            // http://code.google.com/p/google-api-java-client/issues/detail?id=545
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateSampleAddress(@NonNull String sampleId, @NonNull String formattedAddress) {
        checkNotNull(sampleId);
        checkNotNull(formattedAddress);

        try {
            Fusiontables.Query.Sql sql = fusiontables.query()
                    .sql(String.format(UPDATE_SAMPLE_ADDRESS_SQL_COMMAND
                            , formattedAddress
                            , mRowSampleIdsMapper.get(Integer.parseInt(sampleId))
                            )
                    );

            Sqlresponse response = sql.execute();
            Log.d(TAG, response.toString());

            JSONObject responseJson = new JSONObject(response.toString());
            JSONArray rows = responseJson.getJSONArray("rows");
            int affectedRows = rows.getJSONArray(0).getInt(0);

            return affectedRows != 0 ? true : false;
        } catch (IllegalArgumentException e) {
            // For google-api-services-fusiontables-v1-rev1-1.7.2-beta this exception will always
            // been thrown.
            // Please see issue 545: JSON response could not be deserialized to Sqlresponse.class
            // http://code.google.com/p/google-api-java-client/issues/detail?id=545
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(String sampleId) {
        Log.d(TAG, "WHAT THE FUCK");

        int rowId = mRowSampleIdsMapper.get(Integer.parseInt(sampleId));
        try {
            Fusiontables.Query.Sql sql = fusiontables.query()
                    .sql(String.format(DELETE_SAMPLE_SQL_COMMAND, rowId));

            Sqlresponse response = sql.execute();
            Log.d(TAG, response.toString());

            JSONObject responseJson = new JSONObject(response);
            JSONArray rows = responseJson.getJSONArray("rows");
            int numOfRowsAffected  = rows.getJSONArray(0).getInt(0);

            return numOfRowsAffected != 0 ? true : false;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<Sample> retrieveAll() {
        try{
            Fusiontables.Query.Sql sql = fusiontables.query()
                    .sql(SELECT_SAMPLES_SQL_COMMAND);

            Sqlresponse response = sql.execute();
            Log.d(TAG, response.toString());

            JSONObject responseJson = new JSONObject(response);
            JSONArray columns = responseJson.getJSONArray("columns");
            int numOfColumns = columns.length();

            List<Sample> samples = null;
            if(responseJson.has("rows")) {
                JSONArray rows = responseJson.getJSONArray("rows");
                samples = new ArrayList<>(rows.length());

                for (int i = 0; i < rows.length(); i++) {
                    JSONArray singleRow = rows.getJSONArray(i);
                    Sample sample = new Sample();
                    // select rowid,sample_id,name,address,location,date
                    int rowId = singleRow.getInt(0);

                    sample.setSampleId(singleRow.getString(1));
                    sample.setName(singleRow.getString(2));
                    sample.setResult(singleRow.getString(3));
                    sample.setAddress(singleRow.getString(4));

                    String[] latlng = singleRow.getString(5).split(",");
                    sample.setLatitude(Double.parseDouble(latlng[0]));
                    sample.setLongitude(Double.parseDouble(latlng[1]));

                    sample.setTime(singleRow.getLong(6));

                    mRowSampleIdsMapper.put(Integer.parseInt(sample.getSampleId()), rowId);
                    samples.add(sample);
                }
            }
            return samples;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Sample get(String sampleId) {
        try {
            Fusiontables.Query.Sql sql = fusiontables.query()
                    .sql(String.format(SELECT_BY_ID_SQL_COMMAND, sampleId));

            Sqlresponse response = sql.execute();

            Log.d(TAG, response.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public List<Sample> searchSample(String sqlCommand) {
        try{
            Fusiontables.Query.Sql sql = fusiontables.query()
                    .sql(sqlCommand);

            Sqlresponse response = sql.execute();
            Log.d(TAG, "Search : " + response.toString());

            JSONObject responseJson = new JSONObject(response);
            JSONArray columns = responseJson.getJSONArray("columns");
            int numOfColumns = columns.length();

            List<Sample> samples = null;
            if(responseJson.has("rows")) {
                JSONArray rows = responseJson.getJSONArray("rows");
                samples = new ArrayList<>(rows.length());

                for (int i = 0; i < rows.length(); i++) {
                    JSONArray singleRow = rows.getJSONArray(i);
                    Sample sample = new Sample();
                    // select rowid,sample_id,name,address,location,date
                    int rowId = singleRow.getInt(0);

                    sample.setSampleId(singleRow.getString(1));
                    sample.setName(singleRow.getString(2));
                    sample.setResult(singleRow.getString(3));
                    sample.setAddress(singleRow.getString(4));

                    String[] latlng = singleRow.getString(5).split(",");
                    sample.setLatitude(Double.parseDouble(latlng[0]));
                    sample.setLongitude(Double.parseDouble(latlng[1]));

                    sample.setTime(singleRow.getLong(6));
                    samples.add(sample);
                }
            }
            return samples;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
