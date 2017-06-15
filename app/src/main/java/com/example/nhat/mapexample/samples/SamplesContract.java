package com.example.nhat.mapexample.samples;

import android.support.annotation.NonNull;

import com.example.nhat.mapexample.BasePresenter;
import com.example.nhat.mapexample.BaseView;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import java.util.List;

/**
 * Created by Nhat on 11/12/2016.
 */

public interface SamplesContract {

    interface View extends BaseView<Presenter> {

        void showSamples(List<Sample> samples);

        void showNoSamples();

        void highlightSavedSampleMarker(String sampleId);

        void showSearchSampleResult(Sample sample);

        void showSearchQueryNotFound(String query);

        void setDeleteSampleMarkerVisible(String sampleId, boolean visible);

        void notifySampleDeleted(boolean success, String sampleId);

        void notifySampleUpdated(boolean success, Sample sample);

        void setSearchSampleActive(boolean active);

        void setAddSampleActive(boolean active);
    }

    interface Presenter extends BasePresenter {

        void processSearchResult(@NonNull String sampleId);

        void loadSamples(boolean forceUpdate);

        void addSample(@NonNull Sample requestedSample);

        void deleteSample(@NonNull String requestedSampleId);

        void editSample(@NonNull Sample requestedSample);

        void searchSample(@NonNull String query, SamplesSearchType samplesSearchType);

    }
}
