package com.example.nhat.mapexample.samples;

import com.example.nhat.mapexample.BasePresenter;
import com.example.nhat.mapexample.BaseView;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import java.util.List;

/**
 * Created by Nhat on 11/15/2016.
 */

public class MainContract {

    interface View extends BaseView<MainContract.Presenter> {

        void showLoadingIndicator(boolean active);

        void showSamples(List<Sample> samples);

        void showNoSamples();

        void showSampleDetailsUi(String sampleId);
    }

    interface Presenter extends BasePresenter {

        void loadSamples(boolean forceUpdate);

        void deleteSample(String sampleId);

        void editSample(Sample updatedSample);

        void openSampleDetails(Sample sample);
    }
}
