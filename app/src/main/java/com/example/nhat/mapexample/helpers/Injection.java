package com.example.nhat.mapexample.helpers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.example.nhat.mapexample.data.SamplesRepository;
import com.example.nhat.mapexample.data.local.SamplesLocalStorage;
import com.example.nhat.mapexample.data.remote.SamplesRemoteStorage;
import com.example.nhat.mapexample.samples.domain.usecase.DeleteSample;
import com.example.nhat.mapexample.samples.domain.usecase.EditSample;
import com.example.nhat.mapexample.samples.domain.usecase.GetSample;
import com.example.nhat.mapexample.samples.domain.usecase.GetSamples;
import com.example.nhat.mapexample.samples.domain.usecase.AddSample;
import com.example.nhat.mapexample.samples.domain.usecase.SearchSamples;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Nhat on 11/11/2016.
 */

public class Injection {

    public static SamplesRepository provideSamplesRepository(@NonNull Context context) {
        checkNotNull(context);
        return SamplesRepository.getInstance(SamplesRemoteStorage.getInstance(context),
                SamplesLocalStorage.getInstance(context));
    }

    public static GetSamples provideGetSamples(@NonNull Context context) {
        return new GetSamples(Injection.provideSamplesRepository(context), SamplesRemoteStorage.getInstance(context));
    }

    public static GetSample provideGetSample(@NonNull Context context) {
        return new GetSample(Injection.provideSamplesRepository(context), SamplesRemoteStorage.getInstance(context));
    }

    public static AddSample provideAddSample(@NonNull Context context) {
        return new AddSample(Injection.provideSamplesRepository(context), SamplesRemoteStorage.getInstance(context));
    }

    public static DeleteSample provideDeleteSample(@NonNull Context context) {
        return new DeleteSample(Injection.provideSamplesRepository(context), SamplesRemoteStorage.getInstance(context));
    }

    public static EditSample provideEditSample(@NonNull Context context) {
        return new EditSample(Injection.provideSamplesRepository(context), SamplesRemoteStorage.getInstance(context));
    }

    public static SearchSamples provideSearchSamples(@NonNull Context context) {
        return new SearchSamples(Injection.provideSamplesRepository(context), SamplesRemoteStorage.getInstance(context));
    }
}
