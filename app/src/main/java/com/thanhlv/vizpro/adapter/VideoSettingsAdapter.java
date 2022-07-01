package com.thanhlv.vizpro.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.thanhlv.vizpro.Core;
import com.thanhlv.vizpro.R;
import com.thanhlv.vizpro.model.VideoProperties;

import java.util.ArrayList;

public class VideoSettingsAdapter extends RecyclerView.Adapter<VideoSettingsAdapter.ViewHolder>{
    private final Context mContext;
    private ArrayList<VideoProperties> mVideoProperties;
    private int mType;

    public VideoSettingsAdapter(Context context, ArrayList<VideoProperties> videoResolutions, int type) {
        this.mContext = context;
        this.mVideoProperties = videoResolutions;
        this.mType = type;
    }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.item_settings_with_checked, parent, false);
            return new ViewHolder(view);
        }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        VideoProperties item = mVideoProperties.get(position);
        holder.radio_check.setChecked(item.getCheck());
        holder.tv_value.setText(item.getValue());
    }


    @Override
    public int getItemCount() {
        return mVideoProperties.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_value;
        public RadioButton radio_check;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_value = itemView.findViewById(R.id.tv_value);
            radio_check = itemView.findViewById(R.id.radio_check);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeCheckList(getAdapterPosition());
                }
            });
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void changeCheckList(int itemId) {
        for (int i = 0; i < mVideoProperties.size(); i++) {
            mVideoProperties.get(i).setCheck(i==itemId);
            notifyDataSetChanged();
        }
        if (mType == 1) Core.resolution = mVideoProperties.get(itemId).getValue();
        if (mType == 2) Core.bitrate = mVideoProperties.get(itemId).getValue();
        if (mType == 3) Core.frameRate = mVideoProperties.get(itemId).getValue();
    }
}
