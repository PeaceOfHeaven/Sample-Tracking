package com.example.nhat.mapexample.data;

import android.support.annotation.NonNull;

import com.example.nhat.mapexample.samples.SamplesSearchType;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import java.util.List;

/**
 * Created by Nhat on 10/24/2016.
 */

public interface SamplesDataSource {

    interface LoadSamplesCallback {

        void onSamplesLoaded(List<Sample> samples);

        void onDataNotAvailable();
    }

    interface GetSampleCallback {

        void onSampleLoaded(Sample sample);

        void onDataNotAvailable();

    }

    interface CRUDSampleCallback {

        void onSuccess(Sample sample);

        void onError();
    }

    void getSamples(@NonNull  LoadSamplesCallback callback);

    void getSample(@NonNull String sampleId, @NonNull GetSampleCallback callback);

    void addSample(@NonNull  Sample sample, @NonNull CRUDSampleCallback callback);

    void editSample(@NonNull  Sample sample, @NonNull CRUDSampleCallback callback);

    void deleteSample(@NonNull String sampleId, @NonNull CRUDSampleCallback callback);

    void deleteAllSamples();

    void updateSampleAddress(@NonNull String sampleId, @NonNull String formatedAddress, @NonNull CRUDSampleCallback callback);

    void updateSampleAddress(@NonNull Sample sample, @NonNull String formatedAddress, @NonNull CRUDSampleCallback callback);

    void search(String searchValue, SamplesSearchType samplesSearchType, LoadSamplesCallback callback);

    void refreshSamples();
}
