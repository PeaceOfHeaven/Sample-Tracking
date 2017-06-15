package com.example.nhat.mapexample.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.nhat.mapexample.data.local.SamplesLocalStorage;
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

public class SamplesRepository implements SamplesDataSource {

    private static SamplesRepository INSTANCE = null;
    private final SamplesDataSource mSamplesLocalDataSource;
    private final SamplesDataSource mSamplesRemoteDataSource;

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */
    Map<String, Sample> mCachedSamples;
    private boolean mCacheIsDirty;

    // Prevent direct instantiation.
    private SamplesRepository(@NonNull SamplesDataSource samplesRemoteDataSource,
                              @NonNull SamplesDataSource samplesLocalDataSource) {
        mSamplesRemoteDataSource = checkNotNull(samplesRemoteDataSource);
        mSamplesLocalDataSource = checkNotNull(samplesLocalDataSource);
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @param samplesRemoteDataSource the backend data source
     * @param samplesLocalDataSource  the device storage data source
     * @return the {@link SamplesRepository} instance
     */
    public static SamplesRepository getInstance(SamplesDataSource samplesRemoteDataSource,
                                                SamplesDataSource samplesLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new SamplesRepository(samplesRemoteDataSource, samplesLocalDataSource);
        }
        return INSTANCE;
    }

    @Override
    public void getSamples(@NonNull final LoadSamplesCallback callback) {
        checkNotNull(callback);

        if (mCachedSamples != null && !mCacheIsDirty) {
            callback.onSamplesLoaded(new ArrayList<>(mCachedSamples.values()));
            return;
        }

        // Need to show local data first if we have because of improving UX
        getSamplesFromLocalDataSource(callback);

        if (mCacheIsDirty) {
            getSamplesFromRemoteDataSource(callback);
        }
    }

    @Override
    public void getSample(@NonNull final String sampleId, @NonNull final GetSampleCallback callback) {
        checkNotNull(sampleId);
        checkNotNull(callback);

        Sample sample = mCachedSamples.get(sampleId);

        if (sample != null) {
            callback.onSampleLoaded(sample);
            return;
        }

        mSamplesLocalDataSource.getSample(sampleId, new GetSampleCallback() {
            @Override
            public void onSampleLoaded(Sample sample) {
                callback.onSampleLoaded(sample);
            }

            @Override
            public void onDataNotAvailable() {
                mSamplesRemoteDataSource.getSample(sampleId, new GetSampleCallback() {
                    @Override
                    public void onSampleLoaded(Sample sample) {
                        callback.onSampleLoaded(sample);
                    }

                    @Override
                    public void onDataNotAvailable() {
                        callback.onDataNotAvailable();
                    }
                });
            }
        });
    }

    @Override
    public void addSample(Sample sample, final CRUDSampleCallback callback) {
        checkNotNull(sample);
        checkNotNull(callback);

        mSamplesLocalDataSource.addSample(sample, new CRUDSampleCallback() {
            @Override
            public void onSuccess(Sample sample) {
                updateCache(sample);
                callback.onSuccess(sample);

                mSamplesRemoteDataSource.addSample(sample, new CRUDSampleCallback() {
                    @Override
                    public void onSuccess(Sample sample) {
                        ((SamplesLocalStorage) mSamplesLocalDataSource).notifySampleSynced(sample);
                        updateCache(sample);
                        callback.onSuccess(sample);
                    }

                    @Override
                    public void onError() {
                        callback.onError();
                    }
                });
            }

            @Override
            public void onError() {
                callback.onError();
            }
        });
    }

    @Override
    public void editSample(@NonNull Sample sample, @NonNull CRUDSampleCallback callback) {
        checkNotNull(sample);
        checkNotNull(callback);

        mSamplesLocalDataSource.editSample(sample, callback);

    }

    @Override
    public void deleteSample(@NonNull String sampleId, @NonNull CRUDSampleCallback callback) {
        deleteSample(getSampleWithId(sampleId), callback);
    }

    private void deleteSample(@NonNull Sample sample, @NonNull CRUDSampleCallback callback) {
        checkNotNull(sample);

        mSamplesLocalDataSource.deleteSample(sample.getSampleId(), callback);
        mSamplesRemoteDataSource.deleteSample(sample.getSampleId(), callback);

        mCachedSamples.remove(sample.getSampleId());
    }

    @Override
    public void deleteAllSamples() {
        mSamplesLocalDataSource.deleteAllSamples();
        mSamplesRemoteDataSource.deleteAllSamples();

        if (mCachedSamples == null) {
            mCachedSamples = new LinkedHashMap<>();
        }
        mCachedSamples.clear();
    }

    @Override
    public void updateSampleAddress(@NonNull String sampleId,
                                    @NonNull String formatedAddress,
                                    @NonNull CRUDSampleCallback callback) {
        updateSampleAddress(getSampleWithId(sampleId), formatedAddress, callback);
    }

    @Override
    public void updateSampleAddress(@NonNull Sample sample,
                                    @NonNull final String formattedAddress,
                                    @NonNull final CRUDSampleCallback callback) {
        checkNotNull(sample);
        checkNotNull(formattedAddress);
        checkNotNull(callback);

        mSamplesLocalDataSource.updateSampleAddress(getSampleWithId(sample.getSampleId()), formattedAddress, new CRUDSampleCallback() {
            @Override
            public void onSuccess(Sample sample) {
                updateCache(sample);
                callback.onSuccess(sample);

                mSamplesRemoteDataSource.updateSampleAddress(sample, formattedAddress, new CRUDSampleCallback() {
                    @Override
                    public void onSuccess(Sample sample) {
                        ((SamplesLocalStorage) mSamplesLocalDataSource).notifySampleSynced(sample);
                        updateCache(sample);
                        callback.onSuccess(sample);
                    }

                    @Override
                    public void onError() {
                        callback.onError();
                    }
                });
            }

            @Override
            public void onError() {
                callback.onError();
            }
        });
    }

    @Nullable
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

    }

    @Override
    public void refreshSamples() {
        mCacheIsDirty = true;
    }

    private void getSamplesFromRemoteDataSource(@NonNull final LoadSamplesCallback callback) {
        mSamplesRemoteDataSource.getSamples(new LoadSamplesCallback() {

            @Override
            public void onSamplesLoaded(List<Sample> samples) {
                refreshCache(samples);
                refreshLocalDataSource(samples);
                callback.onSamplesLoaded(new ArrayList<>(mCachedSamples.values()));
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void getSamplesFromLocalDataSource(@NonNull final LoadSamplesCallback callback) {
        mSamplesLocalDataSource.getSamples(new LoadSamplesCallback() {

            @Override
            public void onSamplesLoaded(List<Sample> samples) {
                refreshCache(samples);
                callback.onSamplesLoaded(new ArrayList<>(mCachedSamples.values()));
            }

            @Override
            public void onDataNotAvailable() {
                getSamplesFromRemoteDataSource(callback);
            }
        });
    }

    private void refreshCache(List<Sample> samples) {
        if (mCachedSamples == null) {
            mCachedSamples = new LinkedHashMap<>();
        }
        mCachedSamples.clear();

        for (Sample sample : samples) {
            mCachedSamples.put(sample.getSampleId(), sample);
        }
        mCacheIsDirty = false;
    }

    private void refreshLocalDataSource(List<Sample> samples) {
        mSamplesLocalDataSource.deleteAllSamples();
        for (final Sample sample : samples) {
            mSamplesLocalDataSource.addSample(sample, new CRUDSampleCallback() {
                @Override
                public void onSuccess(Sample sample) {

                }

                @Override
                public void onError() {
                    mCachedSamples.remove(sample.getSampleId());
                }
            });
        }
    }

    private void updateCache(Sample sample) {
        // Do in memory cache update to keep the app UI up to date
        if (mCachedSamples == null) {
            mCachedSamples = new LinkedHashMap<>();
        }
        mCachedSamples.put(sample.getSampleId(), sample);
    }
}
