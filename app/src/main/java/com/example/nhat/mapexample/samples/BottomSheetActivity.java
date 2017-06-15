package com.example.nhat.mapexample.samples;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;

import com.example.nhat.mapexample.R;

public class BottomSheetActivity extends AppCompatActivity implements View.OnClickListener {

    private BottomSheetBehavior mBottomSheetBehavior;

    BottomSheetDialogFragment bottomSheetDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_sheet);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        Button button1 = (Button) findViewById(R.id.button_1);
        Button button2 = (Button) findViewById(R.id.button_2);
        Button button3 = (Button) findViewById(R.id.button_3);

        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                String stateName = "";
                switch (newState){
                    case BottomSheetBehavior.STATE_HIDDEN:
                        stateName = "STATE HIDDEN";
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        stateName = "STATE EXPANDED";
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        stateName = "STATE DRAGGING";
                        break;

                    case BottomSheetBehavior.STATE_SETTLING:
                        stateName = "STATE SETTLING";
                        break;

                    case BottomSheetBehavior.STATE_COLLAPSED:
                        stateName = "STATE COLLAPSED";
                        break;
                }
                Log.d("BottomSheet", stateName);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                Log.d("BottomSheetSlide", slideOffset + "");
            }
        });

        bottomSheetDialogFragment = new TutsPlusBottomSheetDialogFragment();

    }

    @Override
    public void onClick(View v) {
        switch( v.getId() ) {
            case R.id.button_1: {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                break;
            }

            case R.id.button_3: {
                /*BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

                View view1 = getLayoutInflater().inflate(R.layout.layout_sample_detail, null);

                bottomSheetDialog.setContentView(view1);

                bottomSheetDialog.show();*/
                bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());
                break;
            }
        }
    }

    public static class TutsPlusBottomSheetDialogFragment extends BottomSheetDialogFragment {

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

        @Override
        public void setupDialog(Dialog dialog, int style) {
            super.setupDialog(dialog, style);

            final View contentView = View.inflate(getContext(), R.layout.layout_sample_detail, null);
            dialog.setContentView(contentView);


            /*final View card = contentView.findViewById(R.id.card);
            final View bg = contentView.findViewById(R.id.bg);
            final ViewTreeObserver vto = card.getViewTreeObserver();
            if(vto.isAlive()) {
                Log.d("FUCK", "fuckckckckc");

                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int height = card.getMeasuredHeight();
                        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) bg.getLayoutParams();
                        params.height = height - params.topMargin;
                        bg.setLayoutParams(params);

                        card.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }*/
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

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);


        }
    }
}
