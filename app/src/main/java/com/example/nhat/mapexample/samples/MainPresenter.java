package com.example.nhat.mapexample.samples;

import android.support.annotation.NonNull;

import com.example.nhat.mapexample.UseCase;
import com.example.nhat.mapexample.UseCaseHandler;
import com.example.nhat.mapexample.samples.domain.model.Sample;
import com.example.nhat.mapexample.samples.domain.usecase.DeleteSample;
import com.example.nhat.mapexample.samples.domain.usecase.EditSample;
import com.example.nhat.mapexample.samples.domain.usecase.GetSamples;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Nhat on 11/15/2016.
 */

public class MainPresenter implements MainContract.Presenter {

    private final MainContract.View mMainView;
    private final GetSamples mGetSamples;
    private final DeleteSample mDeleteSample;
    private final EditSample mEditSample;

    private final UseCaseHandler mUseCaseHandler;

    private boolean mFirstLoad = true;

    public MainPresenter(@NonNull MainContract.View mainView,
                            @NonNull GetSamples getSamples,
                            @NonNull DeleteSample deleteSample,
                            @NonNull EditSample editSample,
                            @NonNull UseCaseHandler useCaseHandler) {
        mMainView = checkNotNull(mainView, "samplesView cannot be null");
        mGetSamples = checkNotNull(getSamples, "getSamples cannot be null");
        mDeleteSample = checkNotNull(deleteSample, "deleteSamples cannot be null");
        mEditSample = checkNotNull(editSample, "editSample cannot be null");
        mUseCaseHandler = checkNotNull(useCaseHandler, "usecaseHandler cannot be null");
        mMainView.setPresenter(this);
    }

    @Override
    public void start() {
        loadSamples(mFirstLoad);
        mFirstLoad = false;
    }

    @Override
    public void loadSamples(boolean forceUpdate) {
        mMainView.showLoadingIndicator(true);

        mUseCaseHandler.execute(mGetSamples, new GetSamples.RequestValues(forceUpdate),
                new UseCase.UseCaseCallback<GetSamples.ResponseValue>() {
            @Override
            public void onSuccess(GetSamples.ResponseValue response) {
                List<Sample> samples = response.getSamples();

                mMainView.showLoadingIndicator(false);
                mMainView.showSamples(samples);
            }

            @Override
            public void onError() {
                mMainView.showLoadingIndicator(false);
                mMainView.showNoSamples();
            }
        });
    }

    @Override
    public void deleteSample(String sampleId) {
        mMainView.showLoadingIndicator(true);

        mUseCaseHandler.execute(mDeleteSample, new DeleteSample.RequestValues(sampleId),
                new UseCase.UseCaseCallback<DeleteSample.ResponseValue>() {
            @Override
            public void onSuccess(DeleteSample.ResponseValue response) {
                // TODO Implement action when delete sample successfully
                loadSamples(false);
            }

            @Override
            public void onError() {
                // TODO Implement action when delete sample being failed
            }
        });
    }

    @Override
    public void editSample(Sample updatedSample) {
        mMainView.showLoadingIndicator(true);

        mUseCaseHandler.execute(mEditSample, new EditSample.RequestValues(updatedSample),
                new UseCase.UseCaseCallback<EditSample.ResponseValue>() {
            @Override
            public void onSuccess(EditSample.ResponseValue response) {
                loadSamples(false);
            }

            @Override
            public void onError() {

            }
        });
    }

    @Override
    public void openSampleDetails(Sample sample) {
        checkNotNull(sample, "Requested sample cannot be null!");
        mMainView.showSampleDetailsUi(sample.getSampleId());
    }
}
