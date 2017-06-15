package com.example.nhat.mapexample.samples.domain.usecase;

import android.support.annotation.NonNull;

import com.example.nhat.mapexample.UseCase;
import com.example.nhat.mapexample.data.SamplesDataSource;
import com.example.nhat.mapexample.data.SamplesRepository;
import com.example.nhat.mapexample.data.remote.SamplesRemoteStorage;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Nhat on 11/17/2016.
 */

public class EditSample extends UseCase<EditSample.RequestValues, EditSample.ResponseValue> {

    private final SamplesRepository mSamplesRepository;

    private final SamplesRemoteStorage mSamplesRemoteStorage;

    public EditSample(@NonNull  SamplesRepository samplesRepository, @NonNull SamplesRemoteStorage remoteStorage) {
        mSamplesRepository = checkNotNull(samplesRepository, "samplesRepository cannot be null!");
        mSamplesRemoteStorage = checkNotNull(remoteStorage, "remoteStorage cannot be null!");
    }

    @Override
    protected void executeUseCase(EditSample.RequestValues requestValues) {
        mSamplesRemoteStorage.editSample(requestValues.getSample(), new SamplesDataSource.CRUDSampleCallback() {
            @Override
            public void onSuccess(Sample sample) {
                getUseCaseCallback().onSuccess(new ResponseValue(sample));
            }

            @Override
            public void onError() {

            }
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final Sample mSample;

        public RequestValues(Sample sample) {
            checkNotNull(sample, "sample id cannot be null");
            mSample = sample;
        }

        public Sample getSample() {
            return mSample;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final Sample mSample;

        public ResponseValue(Sample sample) {
            mSample = sample;
        }

        public Sample getSample() {
            return mSample;
        }
    }
}
