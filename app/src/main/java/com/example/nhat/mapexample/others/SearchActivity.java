package com.example.nhat.mapexample.others;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.nhat.mapexample.R;
import com.example.nhat.mapexample.UseCase;
import com.example.nhat.mapexample.UseCaseHandler;
import com.example.nhat.mapexample.helpers.Injection;
import com.example.nhat.mapexample.samples.SamplesAdapter;
import com.example.nhat.mapexample.samples.SamplesSearchType;
import com.example.nhat.mapexample.samples.domain.model.Sample;
import com.example.nhat.mapexample.samples.domain.usecase.SearchSamples;

import java.util.Collections;

public class SearchActivity extends AppCompatActivity {


    public static final String SAMPLE_ID_RESULT_KEY = "sample_id";
    private static final String ARG_QUERY = "query";

    private SearchView mSearchView;
    private Spinner mSearchFilterSpinner;
    private RecyclerView mSamplesRecyclerView;
    private SamplesAdapter mAdapter;

    private String mQuery = "";
    private SamplesSearchType mCurrentSearchType;
    private final String[] searchTypeNames = new String[]{"Tất cả", "Tên", "Id", "Thời gian"};

    public static Intent getStartIntent(Context context, String query) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(ARG_QUERY, query);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mCurrentSearchType = SamplesSearchType.NAME;

        mSearchFilterSpinner = (Spinner) findViewById(R.id.searchFilterSpinner);
        mSearchFilterSpinner.setAdapter(new ArrayAdapter<>(this,
                R.layout.support_simple_spinner_dropdown_item,
                searchTypeNames));
        mSearchFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
               @Override
               public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                   switch (position) {
                       case 0 :
                           mCurrentSearchType = SamplesSearchType.ALL;
                           break;
                       case 1 :
                           mCurrentSearchType = SamplesSearchType.NAME;
                           break;
                       case 2 :
                           mCurrentSearchType = SamplesSearchType.ID;
                           break;
                       case 3 :
                           mCurrentSearchType = SamplesSearchType.DATE;
                           break;
                   }
                   mSearchView.setQueryHint(searchTypeNames[position]);
               }

               @Override
               public void onNothingSelected(AdapterView<?> parent) {

               }
        });

        mSearchView = (SearchView) findViewById(R.id.search_view);
        setupSearchView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.appbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent intent = NavUtils.getParentActivityIntent(SearchActivity.this);
                NavUtils.navigateUpTo(SearchActivity.this, intent);*/
                finish();
            }
        });

        mSamplesRecyclerView = (RecyclerView) findViewById(R.id.search_results);
        mSamplesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new SamplesAdapter(this, new SamplesAdapter.Callback() {
            @Override
            public void onSampleClicked(int position, Sample sample) {
                Intent data = new Intent();
                data.putExtra(SAMPLE_ID_RESULT_KEY, sample.getSampleId());
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void onDeleteSample(String sampleId) {

            }
        });
        mSamplesRecyclerView.setHasFixedSize(true);
        mSamplesRecyclerView.setAdapter(mAdapter);

        String query = getIntent().getStringExtra(SearchManager.QUERY);
        query = query == null ? "" : query;
        mQuery = query;

        if (mSearchView != null) {
            mSearchView.setQuery(query, false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            doEnterAnim();
        }

        overridePendingTransition(0, 0);
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconified(false);
        // Set the query hint.
        mSearchView.setQueryHint(getString(R.string.search_hint_by_name));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(!TextUtils.isEmpty(s)) {
                    View view = getCurrentFocus();
                    if (view != null) {
                        view.clearFocus();
                        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    searchFor(s);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                dismiss(null);
                return false;
            }
        });
        if (!TextUtils.isEmpty(mQuery)) {
            mSearchView.setQuery(mQuery, false);
        }
    }

    @Override
    public void onBackPressed() {
        dismiss(null);
    }

    public void dismiss(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            doExitAnim();
        } else {
            ActivityCompat.finishAfterTransition(this);
        }
    }

    /**
     * On Lollipop+ perform a circular reveal animation (an expanding circular mask) when showing
     * the search panel.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void doEnterAnim() {
        // Fade in a background scrim as this is a floating window. We could have used a
        // translucent window background but this approach allows us to turn off window animation &
        // overlap the fade with the reveal animation – making it feel snappier.
        View scrim = findViewById(R.id.scrim);
        scrim.animate()
                .alpha(1f)
                .setDuration(500L)
                .setInterpolator(
                        AnimationUtils.loadInterpolator(this, android.R.interpolator.fast_out_slow_in))
                .start();

        // Next perform the circular reveal on the search panel
        final View searchPanel = findViewById(R.id.search_panel);
        if (searchPanel != null) {
            // We use a view tree observer to set this up once the view is measured & laid out
            searchPanel.getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            searchPanel.getViewTreeObserver().removeOnPreDrawListener(this);
                            // As the height will change once the initial suggestions are delivered by the
                            // loader, we can't use the search panels height to calculate the final radius
                            // so we fall back to it's parent to be safe
                            int revealRadius = ((ViewGroup) searchPanel.getParent()).getHeight();
                            // Center the animation on the top right of the panel i.e. near to the
                            // search button which launched this screen.
                            Animator show = ViewAnimationUtils.createCircularReveal(searchPanel,
                                    searchPanel.getRight(), searchPanel.getTop(), 0f, revealRadius);
                            show.setDuration(250L);
                            show.setInterpolator(AnimationUtils.loadInterpolator(SearchActivity.this,
                                    android.R.interpolator.fast_out_slow_in));
                            show.start();
                            return false;
                        }
                    });
        }
    }

    /**
     * On Lollipop+ perform a circular animation (a contracting circular mask) when hiding the
     * search panel.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void doExitAnim() {
        final View searchPanel = findViewById(R.id.search_panel);
        // Center the animation on the top right of the panel i.e. near to the search button which
        // launched this screen. The starting radius therefore is the diagonal distance from the top
        // right to the bottom left
        int revealRadius = (int) Math.sqrt(Math.pow(searchPanel.getWidth(), 2)
                + Math.pow(searchPanel.getHeight(), 2));
        // Animating the radius to 0 produces the contracting effect
        Animator shrink = ViewAnimationUtils.createCircularReveal(searchPanel,
                searchPanel.getRight(), searchPanel.getTop(), revealRadius, 0f);
        shrink.setDuration(200L);
        shrink.setInterpolator(AnimationUtils.loadInterpolator(SearchActivity.this,
                android.R.interpolator.fast_out_slow_in));
        shrink.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                searchPanel.setVisibility(View.INVISIBLE);
                ActivityCompat.finishAfterTransition(SearchActivity.this);
            }
        });
        shrink.start();

        // We also animate out the translucent background at the same time.
        findViewById(R.id.scrim).animate()
                .alpha(0f)
                .setDuration(200L)
                .setInterpolator(
                        AnimationUtils.loadInterpolator(SearchActivity.this,
                                android.R.interpolator.fast_out_slow_in))
                .start();
    }

    private void searchFor(String query) {
        UseCaseHandler.getInstance().execute(Injection.provideSearchSamples(this),
                new SearchSamples.RequestValues(query, mCurrentSearchType),
                new UseCase.UseCaseCallback<SearchSamples.ResponseValue>() {
            @Override
            public void onSuccess(SearchSamples.ResponseValue response) {
                mAdapter.setData(response.getSamples());
            }

            @Override
            public void onError() {
                mAdapter.setData(Collections.<Sample>emptyList());
                Snackbar.make(findViewById(R.id.rootLayout),  "Không tìm thấy mẫu thử", Snackbar.LENGTH_SHORT)
                        .show();

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
