package com.thanhlv.vizpro.ui.utils;

import android.view.ScaleGestureDetector;

public class CustomOnScaleDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    OnScaleListener mListener;
    public interface OnScaleListener {
        void zoomOut();
        void zoomIn();
    }
    public CustomOnScaleDetector(OnScaleListener listener) {
        mListener = listener;
    }

    float last = 0;
    public void resetLast() {
        last = 0;
    }
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (detector.getScaleFactor() > 1) {
            //zoom out
            if (last == 0) last = detector.getCurrentSpan();
            if (detector.getCurrentSpan() - last > 30) {
                mListener.zoomOut();
                last = detector.getCurrentSpan();
            }
        } else {
            //zoom in
            if (last == 0) last = detector.getCurrentSpan();
            if (last-detector.getCurrentSpan() > 30) {
                mListener.zoomIn();
                last = detector.getCurrentSpan();
            }

        }
        return super.onScale(detector);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return super.onScaleBegin(detector);
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        super.onScaleEnd(detector);
    }
}
