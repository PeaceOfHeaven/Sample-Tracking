package com.example.nhat.mapexample.samples;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.nhat.mapexample.UseCase;
import com.example.nhat.mapexample.UseCaseHandler;
import com.example.nhat.mapexample.samples.domain.model.Sample;
import com.example.nhat.mapexample.samples.domain.usecase.AddSample;
import com.example.nhat.mapexample.samples.domain.usecase.DeleteSample;
import com.example.nhat.mapexample.samples.domain.usecase.EditSample;
import com.example.nhat.mapexample.samples.domain.usecase.GetSample;
import com.example.nhat.mapexample.samples.domain.usecase.GetSamples;
import com.example.nhat.mapexample.samples.domain.usecase.SearchSamples;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Nhat on 11/12/2016.
 */

public class SamplesPresenter implements SamplesContract.Presenter {

    private final String TAG = SamplesPresenter.class.getSimpleName();

    private final SamplesContract.View mSamplesView;
    private final GetSamples mGetSamples;
    private final GetSample mGetSample;
    private final AddSample mSaveSample;
    private final DeleteSample mDeleteSample;
    private final EditSample mEditSample;
    private final SearchSamples mSearchSamples;

    private final UseCaseHandler mUseCaseHandler;

    public SamplesPresenter(@NonNull SamplesContract.View samplesView,
                            @NonNull GetSamples getSamples,
                            @NonNull GetSample getSample,
                            @NonNull AddSample addSample,
                            @NonNull DeleteSample deleteSample,
                            @NonNull EditSample editSample,
                            @NonNull SearchSamples searchSamples,
                            @NonNull UseCaseHandler useCaseHandler) {
        mSamplesView = checkNotNull(samplesView, "samplesView cannot be null");
        mGetSamples = checkNotNull(getSamples, "getSamples cannot be null");
        mGetSample = checkNotNull(getSample, "getSample cannot be null");
        mSaveSample = checkNotNull(addSample, "mSaveSample cannot be null");
        mDeleteSample = checkNotNull(deleteSample, "deleteSamples cannot be null");
        mEditSample = checkNotNull(editSample, "editSample cannot be null");
        mSearchSamples = checkNotNull(searchSamples, "searchSamples cannot be null");
        mUseCaseHandler = checkNotNull(useCaseHandler, "usecaseHandler cannot be null");

        mSamplesView.setPresenter(this);
    }

    @Override
    public void start() {
        loadSamples(false);
    }

    @Override
    public void loadSamples(boolean forceUpdate) {
        mUseCaseHandler.execute(mGetSamples, new GetSamples.RequestValues(forceUpdate),
                new UseCase.UseCaseCallback<GetSamples.ResponseValue>() {
            @Override
            public void onSuccess(@NonNull GetSamples.ResponseValue response) {
                List<Sample> samples = response.getSamples();

                mSamplesView.showSamples(samples);
            }

            @Override
            public void onError() {
                mSamplesView.showNoSamples();
            }
        });
    }

    @Override
    public void addSample(@NonNull final Sample sample) {
        checkNotNull(sample, "sample cannot be null!");

        mSamplesView.setAddSampleActive(false);

        mUseCaseHandler.execute(mSaveSample, new AddSample.RequestValues(sample),
                new UseCase.UseCaseCallback<AddSample.ResponseValue>() {
            @Override
            public void onSuccess(@NonNull AddSample.ResponseValue response) {
                mSamplesView.setAddSampleActive(true);
                mSamplesView.highlightSavedSampleMarker(response.getSample().getSampleId());
                loadSamples(false);
            }

            @Override
            public void onError() {
                mSamplesView.setAddSampleActive(true);
                Log.d(TAG, "Cannot save " + sample.toString());
            }
        });
    }

    @Override
    public void deleteSample(@NonNull final String requestedSampleId) {
        checkNotNull(requestedSampleId);

        mSamplesView.setDeleteSampleMarkerVisible(requestedSampleId, false);

        mUseCaseHandler.execute(mDeleteSample, new DeleteSample.RequestValues(requestedSampleId),
                new UseCase.UseCaseCallback<DeleteSample.ResponseValue>() {
                    @Override
                    public void onSuccess(DeleteSample.ResponseValue response) {
                        mSamplesView.notifySampleDeleted(true, requestedSampleId);
                        loadSamples(false);
                    }

                    @Override
                    public void onError() {
                        // TODO Implement action when delete sample being failed
                        mSamplesView.setDeleteSampleMarkerVisible(requestedSampleId, true);
                        mSamplesView.notifySampleDeleted(false, requestedSampleId);
                    }
                });
    }

    @Override
    public void editSample(@NonNull final Sample requestedSample) {
        mUseCaseHandler.execute(mEditSample, new EditSample.RequestValues(requestedSample),
                new UseCase.UseCaseCallback<EditSample.ResponseValue>() {
                    @Override
                    public void onSuccess(EditSample.ResponseValue response) {
                        mSamplesView.notifySampleUpdated(true, response.getSample());
                    }

                    @Override
                    public void onError() {
                        mSamplesView.notifySampleUpdated(false, requestedSample);
                    }
                });
    }

    @Override
    public void searchSample(@NonNull final String query, @NonNull SamplesSearchType samplesSearchType) {
        checkNotNull(query);
        checkNotNull(samplesSearchType);

        mSamplesView.setSearchSampleActive(false);

        mUseCaseHandler.execute(mSearchSamples,
                new SearchSamples.RequestValues(query, samplesSearchType),
                new UseCase.UseCaseCallback<SearchSamples.ResponseValue>() {
                    @Override
                    public void onSuccess(@NonNull SearchSamples.ResponseValue response) {
                        mSamplesView.setSearchSampleActive(true);
                        mSamplesView.showSearchSampleResult(response.getSamples().get(0));
                    }

                    @Override
                    public void onError() {
                        mSamplesView.setSearchSampleActive(true);
                        mSamplesView.showSearchQueryNotFound(query);
                    }
                });
    }

    @Override
    public void processSearchResult(@NonNull String sampleId) {

        mUseCaseHandler.execute(mGetSample, new GetSample.RequestValues(sampleId), new UseCase.UseCaseCallback<GetSample.ResponseValue>() {
            @Override
            public void onSuccess(GetSample.ResponseValue response) {
                mSamplesView.showSearchSampleResult(response.getSample());
            }

            @Override
            public void onError() {

            }
        });
    }
}
