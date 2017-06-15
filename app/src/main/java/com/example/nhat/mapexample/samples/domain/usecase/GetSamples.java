package com.example.nhat.mapexample.samples.domain.usecase;

import android.support.annotation.NonNull;

import com.example.nhat.mapexample.UseCase;
import com.example.nhat.mapexample.data.SamplesDataSource;
import com.example.nhat.mapexample.data.SamplesRepository;
import com.example.nhat.mapexample.data.remote.SamplesRemoteStorage;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Nhat on 11/12/2016.
 */

public class GetSamples extends UseCase<GetSamples.RequestValues, GetSamples.ResponseValue> {

    private final SamplesRepository mSamplesRepository;

    private final SamplesRemoteStorage mSamplesRemoteStorage;

    public GetSamples(@NonNull  SamplesRepository samplesRepository, @NonNull SamplesRemoteStorage remoteStorage) {
        mSamplesRepository = checkNotNull(samplesRepository, "samplesRepository cannot be null!");
        mSamplesRemoteStorage = checkNotNull(remoteStorage, "remoteStorage cannot be null!");
    }

    @Override
    protected void executeUseCase(final RequestValues requestValues) {
        checkNotNull(requestValues);

        /*if (requestValues.isForceUpdate()) {
            mSamplesRepository.refreshSamples();
        }*/

        mSamplesRemoteStorage.getSamples(new SamplesDataSource.LoadSamplesCallback() {

            @Override
            public void onSamplesLoaded(List<Sample> samples) {
                getUseCaseCallback().onSuccess(new ResponseValue(samples));
            }

            @Override
            public void onDataNotAvailable() {
                getUseCaseCallback().onError();
            }
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final boolean mForceUpdate;

        public RequestValues(boolean forceUpdate) {
            mForceUpdate = forceUpdate;
        }

        public boolean isForceUpdate() {
            return mForceUpdate;
        }

    }

    public static final class ResponseValue implements UseCase.ResponseValue {

        private final List<Sample> mSamples;

        public ResponseValue(@NonNull List<Sample> samples) {
            mSamples = checkNotNull(samples, "tasks cannot be null!");
        }

        public List<Sample> getSamples() {
            return mSamples;
        }
    }
}
