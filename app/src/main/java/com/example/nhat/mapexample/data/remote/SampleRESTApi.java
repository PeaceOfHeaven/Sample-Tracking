package com.example.nhat.mapexample.data.remote;

import android.support.annotation.NonNull;

import com.example.nhat.mapexample.samples.domain.model.Sample;

import java.util.List;

/**
 * Created by Nhat on 11/12/2016.
 */

public interface SampleRESTApi<T> {

    boolean insert(@NonNull T sample);

    boolean updateSampleAddress(String sampleId, @NonNull String formattedAddress);

    boolean delete(String sampleId);

    boolean editSample(Sample sample);

    List<T> retrieveAll();

    T get(String sampleId);

    List<T> searchSample(String sql);
}
