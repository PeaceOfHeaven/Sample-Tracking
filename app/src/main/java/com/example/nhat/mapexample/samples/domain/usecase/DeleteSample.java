package com.example.nhat.mapexample.samples.domain.usecase;

import android.support.annotation.NonNull;
import android.util.Log;

import com.example.nhat.mapexample.UseCase;
import com.example.nhat.mapexample.data.SamplesDataSource;
import com.example.nhat.mapexample.data.SamplesRepository;
import com.example.nhat.mapexample.data.remote.SamplesRemoteStorage;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Nhat on 11/17/2016.
 */

public class DeleteSample extends UseCase<DeleteSample.RequestValues, DeleteSample.ResponseValue> {

    private final SamplesRepository mSamplesRepository;

    private final SamplesRemoteStorage mSamplesRemoteStorage;

    public DeleteSample(@NonNull  SamplesRepository samplesRepository, @NonNull SamplesRemoteStorage remoteStorage) {
        mSamplesRepository = checkNotNull(samplesRepository, "samplesRepository cannot be null!");
        mSamplesRemoteStorage = checkNotNull(remoteStorage, "remoteStorage cannot be null!");
    }

    @Override
    protected void executeUseCase(DeleteSample.RequestValues requestValues) {
        Log.d("FUCK", requestValues.getSampleId());

        mSamplesRemoteStorage.deleteSample(requestValues.getSampleId(), new SamplesDataSource.CRUDSampleCallback() {
            @Override
            public void onSuccess(Sample sample) {
                getUseCaseCallback().onSuccess(new DeleteSample.ResponseValue());
            }

            @Override
            public void onError() {
                getUseCaseCallback().onError();
            }
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final String mSampleId;

        public RequestValues(String sampleId) {
            checkNotNull(sampleId, "sample id cannot be null");
            mSampleId = sampleId;
        }

        public String getSampleId() {
            return mSampleId;
        }
    }

    public static final class ResponseValue implements UseCase.ResponseValue {

    }
}
