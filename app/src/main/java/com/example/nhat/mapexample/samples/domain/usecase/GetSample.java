package com.example.nhat.mapexample.samples.domain.usecase;

import android.support.annotation.NonNull;

import com.example.nhat.mapexample.UseCase;
import com.example.nhat.mapexample.data.SamplesDataSource;
import com.example.nhat.mapexample.data.SamplesRepository;
import com.example.nhat.mapexample.data.remote.SamplesRemoteStorage;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Nhat on 11/13/2016.
 */

public class GetSample extends UseCase<GetSample.RequestValues, GetSample.ResponseValue> {

    private final SamplesRepository mSamplesRepository;

    private final SamplesRemoteStorage mSamplesRemoteStorage;

    public GetSample(@NonNull  SamplesRepository samplesRepository, @NonNull SamplesRemoteStorage remoteStorage) {
        mSamplesRepository = checkNotNull(samplesRepository, "samplesRepository cannot be null!");
        mSamplesRemoteStorage = checkNotNull(remoteStorage, "remoteStorage cannot be null!");
    }

    @Override
    protected void executeUseCase(RequestValues requestValues) {
        mSamplesRepository.getSample(requestValues.getSampleId(), new SamplesDataSource.GetSampleCallback() {
            @Override
            public void onSampleLoaded(Sample sample) {
                getUseCaseCallback().onSuccess(new ResponseValue(sample));
            }

            @Override
            public void onDataNotAvailable() {
                getUseCaseCallback().onError();
            }
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final String mSampleId;

        public RequestValues(String sampleId) {
            mSampleId = sampleId;
        }

        public String getSampleId() {
            return mSampleId;
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
