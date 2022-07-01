package com.thanhlv.vizpro.ui.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class MyVideoView extends VideoView {

    private int mVideoWidth;
    private int mVideoHeight;

    public MyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyVideoView(Context context) {
        super(context);
    }

    public void setVideoSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Log.i("@@@", "onMeasure");
//        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
//        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
//        int screenHeight = 600;
//        int screenWidth = (screenHeight*3)/2;
//        setMeasuredDimension(screenWidth, screenHeight);
    }
}
