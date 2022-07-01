package com.thanhlv.vizpro.ui.fragments;


import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.thanhlv.vizpro.Core;
import com.thanhlv.vizpro.R;
import com.thanhlv.vizpro.adapter.VideoSettingsAdapter;
import com.thanhlv.vizpro.model.VideoProperties;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DialogVideoResolution extends DialogFragmentBase{

    RecyclerView recyclerView;
    ArrayList<VideoProperties> mVideoResolutions;
    SharedPreferences pref;
    public CallbackFragment callback;
    public DialogVideoResolution(CallbackFragment callback) {
        this.callback = callback;
    }

    @Override
    public int getLayout() {
        return R.layout.dialog_settings_video_properties;
    }

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pref = requireActivity().getSharedPreferences("DataSettings", MODE_PRIVATE);
        ImageView btn_back = view.findViewById(R.id.img_btn_back_header);
        recyclerView = view.findViewById(R.id.rc_item);

        TextView title = view.findViewById(R.id.title_box);
        title.setText(getResources().getString(R.string.video_resolution));

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUI();
                dismissAllowingStateLoss();
            }
        });

        initChecked();

        VideoSettingsAdapter adapter = new VideoSettingsAdapter(getContext(), mVideoResolutions, 1);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public void updateUI() {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("VideoResolution", Core.resolution);
        editor.apply();
        callback.onClick();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initChecked() {

        mVideoResolutions = new ArrayList<>();
        mVideoResolutions.add(new VideoProperties("1080p (HD)", false));
        mVideoResolutions.add(new VideoProperties("720p (HD)", false));
        mVideoResolutions.add(new VideoProperties("480p (SD)", false));
        mVideoResolutions.add(new VideoProperties("360p (SD)", false));

        String resolutionSelected = pref.getString("VideoResolution", "480p (SD)");
        for (VideoProperties selected: mVideoResolutions) {
            selected.setCheck(selected.getValue().contains(resolutionSelected));
        }
    }
}
