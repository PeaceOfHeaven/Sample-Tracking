package com.example.nhat.mapexample.samples;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.nhat.mapexample.R;
import com.example.nhat.mapexample.UseCaseHandler;
import com.example.nhat.mapexample.helpers.Injection;
import com.example.nhat.mapexample.others.SearchActivity;
import com.example.nhat.mapexample.samples.domain.model.Sample;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class MainActivity extends AppCompatActivity implements MainContract.View, SamplesAdapter.Callback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mSamplesRecyclerView;
    private SamplesAdapter mAdapter;
    private TextView mEmptyTxtView;
    private AVLoadingIndicatorView mAVLoadingIndicatorView;
    private Toolbar mToolbar;
    private MainContract.Presenter mPresenter;
    private SampleDetailBottomSheetFragment mSampleDetailBottomSheetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(mToolbar);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SamplesActivity.class);
                intent.setAction(SamplesActivity.ACTION_CREATE_SAMPLE);
                startActivity(intent);
            }
        });

        mEmptyTxtView = (TextView) findViewById(R.id.emptyTxtView);
        mAVLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.avi);

        mSamplesRecyclerView = (RecyclerView) findViewById(R.id.samplesRecyclerView);
        mSamplesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SamplesAdapter(this, this);
        mSamplesRecyclerView.setHasFixedSize(true);
        mSamplesRecyclerView.setAdapter(mAdapter);

        mPresenter = new MainPresenter(this,
                Injection.provideGetSamples(getApplicationContext()),
                Injection.provideDeleteSample(getApplicationContext()),
                Injection.provideEditSample(getApplicationContext()),
                UseCaseHandler.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                startActivity(SearchActivity.getStartIntent(this, ""));
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void showLoadingIndicator(boolean active) {
        if(active) {
            mSamplesRecyclerView.setVisibility(View.INVISIBLE);
            mEmptyTxtView.setVisibility(View.INVISIBLE);
            mAVLoadingIndicatorView.smoothToShow();
        } else  {
            mAVLoadingIndicatorView.hide();
        }
    }

    @Override
    public void showSamples(List<Sample> samples) {
        mAdapter.setData(samples);
        mSamplesRecyclerView.setVisibility(View.VISIBLE);
        mEmptyTxtView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showNoSamples() {
        mSamplesRecyclerView.setVisibility(View.INVISIBLE);
        mEmptyTxtView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPresenter(@NonNull MainContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onSampleClicked(final int position, final Sample sample) {
        mSampleDetailBottomSheetFragment = SampleDetailBottomSheetFragment.newInstance(sample);
        mSampleDetailBottomSheetFragment.setCallback(new SampleDetailBottomSheetFragment.Callback() {
            @Override
            public void onDeleteSample(String sampleId) {
                MainActivity.this.onDeleteSample(sampleId);
            }

            @Override
            public void onEditSample(Sample sample) {
                mPresenter.editSample(sample);
            }

            @Override
            public void onShowSampleDetails(Sample sample) {
                mPresenter.openSampleDetails(sample);
            }
        });
        mSampleDetailBottomSheetFragment.show(getSupportFragmentManager(), "sample_detail_dialog");
    }

    @Override
    public void onDeleteSample(String sampleId) {
        mPresenter.deleteSample(sampleId);
    }

    @Override
    public void showSampleDetailsUi(String sampleId) {
        Intent intent = new Intent(MainActivity.this, SamplesActivity.class);
        intent.setAction(SamplesActivity.ACTION_VIEW_SAMPLE);
        intent.putExtra(SamplesActivity.SAMPLE_ID_KEY, sampleId);
        startActivity(intent);
    }
}
