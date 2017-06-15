package com.example.nhat.mapexample.samples;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nhat.mapexample.R;
import com.example.nhat.mapexample.samples.domain.model.Sample;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Nhat on 11/18/2016.
 */
public class SamplesAdapter extends RecyclerView.Adapter<SamplesAdapter.ViewHolder> {

    private List<Sample> mSamples;
    private SimpleDateFormat format = new SimpleDateFormat("MMM dd,yyyy hh:mm:ss a");

    private Callback mCallback;
    private Context mContext;

    public SamplesAdapter(Context context, Callback callback) {
        checkNotNull(context);

        mSamples = new ArrayList<>();
        mCallback = callback;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_item_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Sample sample = mSamples.get(position);
        holder.nameTv.setText("Tên : " + sample.getName());

        String date = format.format(new Date(sample.getTime()));
        holder.timeTv.setText(date);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback != null) {
                    mCallback.onSampleClicked(position, sample);
                }
            }
        });

        holder.deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onDeleteSample(sample.getSampleId());
            }
        });

    }

    @Override
    public int getItemCount() {
        return mSamples.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTv;
        public TextView timeTv;
        public View deleteView;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTv = (TextView) itemView.findViewById(R.id.nameTxtView);
            timeTv = (TextView) itemView.findViewById(R.id.timeTxtView);
            deleteView = itemView.findViewById(R.id.deleteView);
        }
    }

    public void setData(List<Sample> newData) {
        mSamples = newData;
        notifyDataSetChanged();
    }

    public interface Callback {

        void onSampleClicked(final int position, final Sample sample);

        void onDeleteSample(final String sampleId);
    }
}
