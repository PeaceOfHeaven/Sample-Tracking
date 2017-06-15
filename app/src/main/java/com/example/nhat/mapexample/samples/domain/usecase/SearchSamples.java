package com.example.nhat.mapexample.samples.domain.usecase;

import android.support.annotation.NonNull;

import com.example.nhat.mapexample.UseCase;
import com.example.nhat.mapexample.data.SamplesDataSource;
import com.example.nhat.mapexample.data.SamplesRepository;
import com.example.nhat.mapexample.data.remote.SamplesRemoteStorage;
import com.example.nhat.mapexample.samples.SamplesSearchType;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Nhat on 11/22/2016.
 */

public class SearchSamples extends UseCase<SearchSamples.RequestValues, SearchSamples.ResponseValue> {

    private final SamplesRepository mSamplesRepository;

    private final SamplesRemoteStorage mSamplesRemoteStorage;

    public SearchSamples(@NonNull SamplesRepository samplesRepository, @NonNull SamplesRemoteStorage remoteStorage) {
        mSamplesRepository = checkNotNull(samplesRepository, "samplesRepository cannot be null!");
        mSamplesRemoteStorage = checkNotNull(remoteStorage, "remoteStorage cannot be null!");
    }

    @Override
    protected void executeUseCase(final SearchSamples.RequestValues requestValues) {
        checkNotNull(requestValues);

        /*if (requestValues.isForceUpdate()) {
            mSamplesRepository.refreshSamples();
        }*/

        mSamplesRemoteStorage.search(requestValues.getSearchValue(), requestValues.getSearchType(),
                new SamplesDataSource.LoadSamplesCallback() {

            @Override
            public void onSamplesLoaded(List<Sample> samples) {
                getUseCaseCallback().onSuccess(new SearchSamples.ResponseValue(samples));
            }

            @Override
            public void onDataNotAvailable() {
                getUseCaseCallback().onError();
            }
        });
    }

    public static final class RequestValues implements UseCase.RequestValues {

        private final String mSearchValue;
        private final SamplesSearchType mSearchType;

        public RequestValues(String searchValue, SamplesSearchType searchType) {
            mSearchValue = searchValue;
            mSearchType = searchType;
        }

        public SamplesSearchType getSearchType() {
            return mSearchType;
        }

        public String getSearchValue() {
            return mSearchValue;
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
