package com.example.nhat.mapexample.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.example.nhat.mapexample.data.SamplesDataSource;
import com.example.nhat.mapexample.data.local.SamplesPersistenceContract.SampleEntry;
import com.example.nhat.mapexample.samples.SamplesSearchType;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import java.util.ArrayList;
import java.util.List;

import static com.example.nhat.mapexample.data.local.SamplesPersistenceContract.SampleEntry.TABLE_NAME;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Nhat on 11/11/2016.
 */

public class SamplesLocalStorage implements SamplesDataSource {

    private static SamplesLocalStorage INSTANCE;

    private SamplesDbHelper mDbHelper;


    public static SamplesLocalStorage getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SamplesLocalStorage(context);
        }
        return INSTANCE;
    }

    private SamplesLocalStorage(@NonNull Context context) {
        checkNotNull(context);
        mDbHelper = new SamplesDbHelper(context);
    }

    @Override
    public void getSamples(LoadSamplesCallback callback) {
        checkNotNull(callback);

        List<Sample> samples = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] columns = new String[]{SampleEntry._ID,
                SampleEntry.COLUMN_NAME_SAMPLE_ID_COLUMN,
                SampleEntry.COLUMN_NAME_NAME_COLUMN,
                SampleEntry.COLUMN_NAME_ADDRESS_COLUMN,
                SampleEntry.COLUMN_NAME_LAT_COLUMN,
                SampleEntry.COLUMN_NAME_LONG_COLUMN,
                SampleEntry.COLUMN_NAME_DATE_COLUMN,
                SampleEntry.COLUMN_NAME_SYNC_COLUMN};
        Cursor result = db.query(TABLE_NAME, columns, null, null, null, null, null);

        if (result != null && result.moveToFirst()) {
            while (!result.isAfterLast()) {
                Sample sample = Sample.from(result);
                samples.add(sample);
                result.moveToNext();
            }
        }
        if (result != null) {
            result.close();
        }

        if (!samples.isEmpty()) {
            callback.onSamplesLoaded(samples);
        } else {
            callback.onDataNotAvailable();
        }
    }

    @Override
    public void getSample(@NonNull String sampleId, @NonNull GetSampleCallback callback) {
        checkNotNull(callback);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String selection = SampleEntry.COLUMN_NAME_SAMPLE_ID_COLUMN + " =?";
        String[] selectionArgs = {String.valueOf(sampleId)};
        Cursor result = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);

        Sample sample = null;
        if (result != null && result.moveToFirst()) {
            sample = Sample.from(result);
        }
        if (result != null) {
            result.close();
        }

        if (sample != null) {
            callback.onSampleLoaded(sample);
        } else {
            callback.onDataNotAvailable();
        }
    }

    @Override
    public void addSample(@NonNull Sample sample, final CRUDSampleCallback callback) {
        checkNotNull(sample);
        checkNotNull(callback);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        db.insert(TABLE_NAME
                , null
                , SamplesPersistenceContract
                        .SampleEntry
                        .buildSampleContentValues(sample));

        getSample(sample.getSampleId(), new GetSampleCallback() {
            @Override
            public void onSampleLoaded(Sample sample) {
                callback.onSuccess(sample);
            }

            @Override
            public void onDataNotAvailable() {
                callback.onError();
            }
        });
    }

    @Override
    public void editSample(@NonNull Sample sample, @NonNull CRUDSampleCallback callback) {

    }

    @Override
    public void deleteSample(@NonNull String sampleId, @NonNull CRUDSampleCallback callback) {
        checkNotNull(sampleId);
        checkNotNull(callback);

        int numOfRowsAffected = mDbHelper.getWritableDatabase()
                .delete(TABLE_NAME
                        , SampleEntry.COLUMN_NAME_SAMPLE_ID_COLUMN + "=?"
                        , new String[]{sampleId});

        if(numOfRowsAffected != -1) {
            callback.onSuccess(null);
        } else {
            callback.onError();
        }
    }


    @Override
    public void deleteAllSamples() {
        mDbHelper.getWritableDatabase().delete(TABLE_NAME, null, null);
    }

    @Override
    public void updateSampleAddress(@NonNull String sampleId, @NonNull String formatedAddress, @NonNull final CRUDSampleCallback callback) {

    }

    @Override
    public void updateSampleAddress(@NonNull Sample sample, @NonNull String formattedAddress, @NonNull final CRUDSampleCallback callback) {
        checkNotNull(sample);
        checkNotNull(formattedAddress);
        checkNotNull(callback);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String sampleId = sample.getSampleId();

        ContentValues values = new ContentValues();
        values.put(SampleEntry.COLUMN_NAME_ADDRESS_COLUMN, formattedAddress);

        int numOfRowsAffected = db.update(TABLE_NAME,
                values,
                SampleEntry.COLUMN_NAME_SAMPLE_ID_COLUMN + "=?",
                new String[]{sampleId});
        if (numOfRowsAffected != 0) {
            sample.setAddress(formattedAddress);
            callback.onSuccess(sample);
        } else {
            callback.onError();
        }
    }

    public void notifySampleSynced(@NonNull Sample sample) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String sampleId = sample.getSampleId();

        ContentValues values = new ContentValues();
        values.put(SampleEntry.COLUMN_NAME_SYNC_COLUMN, sample.isSyncedUp() ? 1 : 0);

        db.update(TABLE_NAME,
                values,
                SampleEntry.COLUMN_NAME_SAMPLE_ID_COLUMN + "=?",
                new String[]{sampleId});
    }

    @Override
    public void search(String searchValue, SamplesSearchType samplesSearchType, LoadSamplesCallback callback) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        List<Sample> locations = new ArrayList<>();
        String[] columns = new String[]{SampleEntry._ID,
                SampleEntry.COLUMN_NAME_SAMPLE_ID_COLUMN,
                SampleEntry.COLUMN_NAME_NAME_COLUMN,
                SampleEntry.COLUMN_NAME_ADDRESS_COLUMN,
                SampleEntry.COLUMN_NAME_LAT_COLUMN,
                SampleEntry.COLUMN_NAME_LONG_COLUMN,
                SampleEntry.COLUMN_NAME_DATE_COLUMN};
        /*Cursor result = getReadableDatabase().query(TABLE_NAME,
                columns,
                "upper(" + searchBy + ") like ?",
                new String[]{query.toUpperCase()},
                null, null, null);*/

        Cursor result = db.rawQuery("select * from locations where upper(name) like '%" + searchValue.toUpperCase() + "%';", null);
        if (result.moveToFirst()) {
            while (!result.isAfterLast()) {
                locations.add(Sample.from(result));
                result.moveToNext();
            }
        }
    }

    @Override
    public void refreshSamples() {

    }
}
