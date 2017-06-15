package com.example.nhat.mapexample.samples;

import android.app.Dialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.nhat.mapexample.R;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import java.sql.Date;
import java.text.SimpleDateFormat;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Nhat on 11/20/2016.
 */


public class SampleDetailBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String SAMPLE_ARG_KEY = "sample_arg";

    private SampleDetailBottomSheetFragment.Callback mCallback;

    private Sample mSample;

    private boolean mActiveLocationButton = true;

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    public static SampleDetailBottomSheetFragment newInstance(Sample sample) {
        checkNotNull(sample, "Sample need to display cannot be null!");

        Bundle args = new Bundle();
        args.putParcelable(SAMPLE_ARG_KEY, sample);

        SampleDetailBottomSheetFragment fragment = new SampleDetailBottomSheetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        final View contentView = View.inflate(getContext(), R.layout.layout_sample_detail, null);
        dialog.setContentView(contentView);

        mSample = (Sample) getArguments().get(SAMPLE_ARG_KEY);

        final TextView mSampleIdTV = (TextView) contentView.findViewById(R.id.sampleIdTV);
        final TextView mNameTV = (TextView) contentView.findViewById(R.id.sampleNameTV);
        final EditText mAddressTV = (EditText) contentView.findViewById(R.id.sampleAddressTV);
        final EditText mResultTV = (EditText) contentView.findViewById(R.id.sampleResultTV);
        final TextView mTimeTV = (TextView) contentView.findViewById(R.id.sampleTimeTV);
        final View locationBtn =  contentView.findViewById(R.id.locationBtn);

        if(!mActiveLocationButton) {
            locationBtn.setVisibility(View.INVISIBLE);
        }

        final ImageView editActionView = (ImageView) contentView.findViewById(R.id.edit_sample_action);
        final ImageView deleteActionView = (ImageView) contentView.findViewById(R.id.delete_sample_action);
        final ImageView cancelActionView = (ImageView) contentView.findViewById(R.id.cancel_sample_action);
        final ImageView doneActionView = (ImageView) contentView.findViewById(R.id.done_sample_action);

        editActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableEditMode(mResultTV, true);
                enableEditMode(mAddressTV, true);

                deleteActionView.setVisibility(View.INVISIBLE);
                editActionView.setVisibility(View.INVISIBLE);
                cancelActionView.setVisibility(View.VISIBLE);
                doneActionView.setVisibility(View.VISIBLE);

                mResultTV.requestFocus();
                mResultTV.setSelection(mResultTV.getText().length());

                if(mActiveLocationButton) {
                    locationBtn.setVisibility(View.INVISIBLE);
                }
                mNameTV.setEnabled(false);
                mSampleIdTV.setEnabled(false);
                mTimeTV.setEnabled(false);
            }
        });

        deleteActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback != null) {
                    mCallback.onDeleteSample(mSample.getSampleId());
                }
                dismiss();
            }
        });

        cancelActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableEditMode(mResultTV, false);
                enableEditMode(mAddressTV, false);

                deleteActionView.setVisibility(View.VISIBLE);
                editActionView.setVisibility(View.VISIBLE);
                cancelActionView.setVisibility(View.INVISIBLE);
                doneActionView.setVisibility(View.INVISIBLE);

                if(mActiveLocationButton) {
                    locationBtn.setVisibility(View.INVISIBLE);
                }
                mNameTV.setEnabled(true);
                mSampleIdTV.setEnabled(true);
                mTimeTV.setEnabled(true);
            }
        });

        doneActionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSample.setResult(mResultTV.getText().toString());
                mSample.setAddress(mAddressTV.getText().toString());

                enableEditMode(mResultTV, false);
                enableEditMode(mAddressTV, false);

                deleteActionView.setVisibility(View.VISIBLE);
                editActionView.setVisibility(View.VISIBLE);
                cancelActionView.setVisibility(View.INVISIBLE);
                doneActionView.setVisibility(View.INVISIBLE);

                if(mActiveLocationButton) {
                    locationBtn.setVisibility(View.INVISIBLE);
                }
                mNameTV.setEnabled(true);
                mSampleIdTV.setEnabled(true);
                mTimeTV.setEnabled(true);

                getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                if(mCallback != null) {
                    mCallback.onEditSample(mSample);
                }
                dismiss();
            }
        });

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback != null) {
                    mCallback.onShowSampleDetails(mSample);
                    dismiss();
                }
            }
        });

        mSampleIdTV.setText(mSample.getSampleId());
        mNameTV.setText(mSample.getName());
        mResultTV.setText(mSample.getResult());
        mAddressTV.setText(mSample.getAddress());

        SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy hh:mm:ss a");
        mTimeTV.setText(format.format(new Date(mSample.getTime())));

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            final BottomSheetBehavior mBottomSheetBehavior = (BottomSheetBehavior) behavior;
            mBottomSheetBehavior.setBottomSheetCallback(mBottomSheetBehaviorCallback);

            contentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    contentView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    int height = contentView.getMeasuredHeight();
                    mBottomSheetBehavior.setPeekHeight(height);
                }
            });
        }
    }

    private void enableEditMode(EditText editText, boolean enable) {
        editText.setCursorVisible(enable);
        editText.setFocusable(enable);
        editText.setFocusableInTouchMode(enable);

        if(enable) {
            editText.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN );
        } else {
            editText.getBackground().setColorFilter(getResources().getColor(android.R.color.transparent), PorterDuff.Mode.SRC_IN );
        }
    }

    public boolean isActiveLocationButton() {
        return mActiveLocationButton;
    }

    public void setActiveLocationButton(boolean activeLocationButton) {
        mActiveLocationButton = activeLocationButton;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {

        void onDeleteSample(String sampleId);

        void onEditSample(Sample sample);

        void onShowSampleDetails(Sample sample);
    }
}
