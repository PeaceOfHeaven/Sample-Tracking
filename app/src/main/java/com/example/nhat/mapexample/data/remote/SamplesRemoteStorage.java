package com.example.nhat.mapexample.data.remote;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.example.nhat.mapexample.GeocodingService;
import com.example.nhat.mapexample.data.SamplesDataSource;
import com.example.nhat.mapexample.data.remote.fusiontable.SamplesFusionTableApi;
import com.example.nhat.mapexample.samples.SamplesSearchType;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Created by Nhat on 11/11/2016.
 */

public class SamplesRemoteStorage implements SamplesDataSource {

    private static SamplesRemoteStorage INSTANCE;
    private final SamplesFusionTableApi mSamplesFusionTableApi;

    private static Context mAppContext;

    Map<String, Sample> mCachedSamples;

    private boolean mCacheIsDirty = false;

    // TODO Implement caching sample data

    public static SamplesRemoteStorage getInstance(Context context) {
        if (INSTANCE == null) {
            mAppContext = context.getApplicationContext();
            INSTANCE = new SamplesRemoteStorage(context);
        }
        return INSTANCE;
    }

    private SamplesRemoteStorage(Context context) {
        mSamplesFusionTableApi = SamplesFusionTableApi.getInstance(context);
    }

    @Override
    public void getSamples(@NonNull LoadSamplesCallback callback) {
        checkNotNull(callback);

        if (mCachedSamples != null && !mCachedSamples.isEmpty()) {
            callback.onSamplesLoaded(new ArrayList<>(mCachedSamples.values()));
            return;
        }
        List<Sample> samples = mSamplesFusionTableApi.retrieveAll();

        if (samples != null && !samples.isEmpty()) {
            refreshCache(samples);
            callback.onSamplesLoaded(new ArrayList<>(mCachedSamples.values()));
        } else {
            callback.onDataNotAvailable();
        }
    }

    private void refreshCache(List<Sample> samples) {
        if (mCachedSamples == null) {
            mCachedSamples = new LinkedHashMap<>();
        }
        mCachedSamples.clear();

        for (Sample sample : samples) {
            mCachedSamples.put(sample.getSampleId(), sample);
        }
    }

    @Override
    public void getSample(@NonNull String sampleId, @NonNull GetSampleCallback callback) {
        checkNotNull(callback);

        if(mCachedSamples != null && mCachedSamples.containsKey(sampleId)) {
            callback.onSampleLoaded(mCachedSamples.get(sampleId));
            return;
        }
        Sample sample = mSamplesFusionTableApi.get(sampleId);
        if (sample != null) {
            callback.onSampleLoaded(sample);
        } else {
            callback.onDataNotAvailable();
        }
    }

    @Override
    public void addSample(@NonNull Sample sample, @NonNull CRUDSampleCallback callback) {
        checkNotNull(sample);
        checkNotNull(callback);

        sample.setSyncedUp(false);

        if(mCachedSamples == null) {
            mCachedSamples = new LinkedHashMap<>();
        }

        // Good UX here, user no need to wait because of syncing between client and server, just update UI immediately
        // mCachedSamples.put(sample.getSampleId(), sample);
        // callback.onSuccess(sample);

        boolean success = mSamplesFusionTableApi.insert(sample);
        if (success) {
            sample.setSyncedUp(true);
            mCachedSamples.put(sample.getSampleId(), sample);

            callback.onSuccess(sample);

            if (TextUtils.isEmpty(sample.getAddress())) {
                mAppContext.startService(GeocodingService.getStartIntent(mAppContext, sample));
            }
        } else {
            callback.onError();
        }

    }

    @Override
    public void editSample(@NonNull Sample sample, @NonNull CRUDSampleCallback callback) {
        checkNotNull(sample);
        checkNotNull(callback);

        if(mCachedSamples != null && mCachedSamples.containsKey(sample.getSampleId())) {
            Sample cachedSample = mCachedSamples.get(sample.getSampleId());
            cachedSample.setAddress(sample.getAddress());
            cachedSample.setSyncedUp(false);

            boolean success = mSamplesFusionTableApi.editSample(cachedSample);

            if (success) {
                cachedSample.setSyncedUp(true);
                callback.onSuccess(cachedSample);
            } else {
                callback.onError();
            }
        }
    }

    @Override
    public void deleteSample(@NonNull String sampleId, @NonNull CRUDSampleCallback callback) {
        checkNotNull(sampleId);
        checkNotNull(callback);

        deleteSample(getSampleWithId(sampleId), callback);
    }

    private void deleteSample(@NonNull Sample sample, @NonNull CRUDSampleCallback callback) {
        checkNotNull(sample);
        checkNotNull(callback);

        if(mCachedSamples != null && mCachedSamples.containsKey(sample.getSampleId())) {
            boolean success = mSamplesFusionTableApi.delete(sample.getSampleId());
            if (success) {
                mCachedSamples.remove(sample.getSampleId());
                callback.onSuccess(sample);
            } else {
                callback.onError();
            }
        }

    }

    @Override
    public void deleteAllSamples() {

    }

    @Override
    public void updateSampleAddress(@NonNull String sampleId, @NonNull String formatedAddress, @NonNull CRUDSampleCallback callback) {
        checkNotNull(sampleId);

        updateSampleAddress(getSampleWithId(sampleId), formatedAddress, callback);
    }

    @Override
    public void updateSampleAddress(@NonNull Sample sample, @NonNull String formatedAddress, @NonNull CRUDSampleCallback callback) {
        checkNotNull(sample);

        if(mCachedSamples != null && mCachedSamples.containsKey(sample.getSampleId())) {
            Sample cachedSample = mCachedSamples.get(sample.getSampleId());
            cachedSample.setAddress(formatedAddress);
            cachedSample.setSyncedUp(false);

            callback.onSuccess(cachedSample);

            boolean success = mSamplesFusionTableApi.updateSampleAddress(cachedSample.getSampleId(), formatedAddress);

            if (success) {
                cachedSample.setSyncedUp(true);
                callback.onSuccess(cachedSample);
            } else {
                callback.onError();
            }
        }
    }

    private Sample getSampleWithId(@NonNull String id) {
        checkNotNull(id);

        if (mCachedSamples == null || mCachedSamples.isEmpty()) {
            return null;
        } else {
            return mCachedSamples.get(id);
        }
    }

    @Override
    public void search(String searchValue, SamplesSearchType samplesSearchType, LoadSamplesCallback callback) {
        checkNotNull(callback);

        List<Sample> samples = mSamplesFusionTableApi.
                searchSample(SamplesFusionTableApi.buildSearchSqlCommand(searchValue, samplesSearchType));
        if (samples != null && !samples.isEmpty()) {
            callback.onSamplesLoaded(samples);
        } else {
            callback.onDataNotAvailable();
        }
    }

    @Override
    public void refreshSamples() {

    }
}
