package com.thanhlv.vizpro.ui.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.thanhlv.vizpro.R;
import com.thanhlv.vizpro.controllers.settings.CameraSetting;
import com.thanhlv.vizpro.ui.utils.AspectFrameLayout;
import com.thanhlv.vizpro.ui.utils.CustomOnScaleDetector;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReactCamActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback, CustomOnScaleDetector.OnScaleListener {


    private MediaRecorder recorder;
    private SurfaceHolder holder;
    private CamcorderProfile camcorderProfile;
    private Camera camera;

    boolean recording = false;
    boolean usecamera = true;
    boolean previewRunning = false;

    private int mScreenWidth, mScreenHeight;
    private int mCameraWidth = 160, mCameraHeight = 120;

    private void calculateCameraSize(CameraSetting cameraProfile) {
        int factor;
        switch (cameraProfile.getSize()){
            case CameraSetting.SIZE_BIG:
                factor = 3;
                break;
            case CameraSetting.SIZE_MEDIUM:
                factor = 4;
                break;
            default: //small
                factor = 5;
                break;
        }
        if(mScreenWidth > mScreenHeight) {//landscape
            mCameraWidth = mScreenWidth / factor;
            mCameraHeight = mCameraWidth*3/4;
        }
        else{
            mCameraWidth = mScreenHeight/factor;
            mCameraHeight = mCameraWidth*3/4;
        }
    }


    private void setCameraOrientation() {
        Camera.CameraInfo camInfo =
                new Camera.CameraInfo();
        Camera.getCameraInfo(getBackFacingCameraId(), camInfo);

        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (camInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (camInfo.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private int getBackFacingCameraId() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {

                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    int widthVideo, heightVideo;
    VideoView videoView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);

        setContentView(R.layout.activity_react_cam);

        videoView = (VideoView)findViewById(R.id.video_main);
        videoView.setVideoPath("/sdcard/videomain.mp4");

//        videoView.setMediaController(new MediaController(this));
        videoView.requestFocus();


        AspectFrameLayout frameLayout = findViewById(R.id.frame_video);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                widthVideo = mp.getVideoWidth();
                heightVideo = mp.getVideoHeight();
                frameLayout.setAspectRatio((double) widthVideo/heightVideo);

                leftX = 0;
                rightX = leftX + width;

                topH = height/2f - (heightVideo*width/widthVideo)/2f;
                bottomH = height/2f + (heightVideo*width/widthVideo)/2f;
                initCamView();

            }
        });


        btn = findViewById(R.id.fab);

        btn.setClickable(true);
        btn.setOnClickListener(this);


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;


    }

    float topH, bottomH, leftX, rightX, width, height;

    LinearLayout cameraPreview;
    View mCameraLayout;
    Button btn;
    float ww, hh;
    private void initCamView() {

        RelativeLayout root = findViewById(R.id.root_container);
        mCameraLayout = LayoutInflater.from(this).inflate(R.layout.layout_camera_view, null);
        cameraPreview = mCameraLayout.findViewById(R.id.camera_preview);
        SurfaceView cameraView = new SurfaceView(this);
        cameraPreview.addView(cameraView);
        holder = cameraView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        root.addView(mCameraLayout);

        mCameraLayout.post(new Runnable() {
            @Override
            public void run() {

                ww = mCameraLayout.getWidth();
                hh = mCameraLayout.getHeight();

                mCameraLayout.setX(leftX);
                mCameraLayout.setY(bottomH - hh-1);
                videoView.start();

            }
        });

        CustomOnScaleDetector customOnScaleDetector = new CustomOnScaleDetector(this);

        ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this, customOnScaleDetector);

        mCameraLayout.setOnTouchListener(new View.OnTouchListener() {
            private int x, y;
            private int pointerId1, pointerId2;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getPointerCount() > 1) {
                    pointerId1 = event.getPointerId(0);
                    pointerId2 = event.getPointerId(1);

                    if (event.getX(pointerId1) < 0 || event.getX(pointerId2) < 0
                    || event.getX(pointerId1) > v.getWidth() || event.getX(pointerId2) > v.getWidth()
                    || event.getY(pointerId1) < 0 || event.getY(pointerId2) < 0
                            || event.getY(pointerId1) > v.getHeight() || event.getY(pointerId2) > v.getHeight()) {

                    } else scaleGestureDetector.onTouchEvent(event);
                    hasZoom = true;
                }

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        x = (int) (v.getX() - event.getRawX());
                        y = (int) (v.getY() - event.getRawY());
                        customOnScaleDetector.resetLast();
                        hasZoom = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (event.getPointerCount() < 2 && !hasZoom ){
                            v.setX(event.getRawX() + x);
                            v.setY(event.getRawY() + y);
                            if (v.getY() < topH) {
                                v.setY(topH-1);
                            } else if (v.getY() + hh >= bottomH){
                                v.setY(bottomH-hh-1);
                            } else {
                                v.setY(event.getRawY() + y);
                            }
                            if (v.getX() < leftX) {
                                v.setX(leftX);
                            } else if (v.getX() >= rightX - ww){
                                v.setX(rightX-ww + 1);
                            } else {
                                v.setX(event.getRawX() + x);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    default:
                        return true;
                }

            }
        });


    }

    boolean hasZoom = false;

    File file_path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath(), "Thanh");

    private void prepareRecorder() {
        recorder = new MediaRecorder();
        recorder.setPreviewDisplay(holder.getSurface());

        if (usecamera) {
            camera.unlock();
            recorder.setCamera(camera);
        }

        recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        recorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);

        recorder.setProfile(camcorderProfile);

        if (!file_path.exists()) {
            file_path.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File recordingFile = new File(file_path.getAbsolutePath() + File.separator + timeStamp+".mp4");

        recorder.setOutputFile(recordingFile.getAbsolutePath());

        // This is all very sloppy
        try {
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            finish();
        } catch (IOException e) {
            e.printStackTrace();
            finish();
        }
    }

    public void onClick(View v) {
        if (recording) {
            recorder.stop();
            if (usecamera) {
                try {
                    camera.reconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // recorder.release();
            recording = false;
            // Let's prepareRecorder so we can record again
//            prepareRecorder();
        } else {
            recording = true;
            recorder.start();

        }
    }

    public void surfaceCreated(SurfaceHolder holder) {

        if (usecamera) {
            camera = Camera.open(1);

            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                previewRunning = true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (!recording && usecamera) {
            if (previewRunning){
                camera.stopPreview();
            }

            setCameraOrientation();
            try {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
                previewRunning = true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            prepareRecorder();
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        if (recording) {
            recorder.stop();
            recording = false;
        }
        recorder.release();
        if (usecamera) {
            previewRunning = false;
            camera.release();
        }
        finish();
    }

    @Override
    public void zoomOut() {

        ww = ww*1.1f;
        hh = hh*1.1f;
        if (ww >= width ) {
            ww = width;
            hh = ww*4/3f;
        }

        if (hh >= heightVideo*width/widthVideo) {
            hh = heightVideo*width/widthVideo;
            ww = hh*3/4f;
        }
        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) ww, (int) hh));
        mCameraLayout.setX(mCameraLayout.getX() - (ww*0.1f)/2f);
        mCameraLayout.setY(mCameraLayout.getY() - (hh*0.1f)/2f);
        if (mCameraLayout.getY() < topH) {
            mCameraLayout.setY(topH-1);
        } else if (mCameraLayout.getY() + hh >= bottomH){
            mCameraLayout.setY(bottomH-hh-1);
        } else {
            mCameraLayout.setY(mCameraLayout.getY() - (hh*0.1f)/2f);
        }
        if (mCameraLayout.getX() < leftX) {
            mCameraLayout.setX(leftX);
        } else if (mCameraLayout.getX() >= rightX - ww){
            mCameraLayout.setX(rightX - ww + 1);
        } else {
            mCameraLayout.setX(mCameraLayout.getX() - (ww*0.1f)/2f);
        }
        hasZoom = true;

    }

    @Override
    public void zoomIn() {
        if (ww <= 200) return;
        ww = ww/1.1f;
        hh = hh/1.1f;
        cameraPreview.setLayoutParams(new FrameLayout.LayoutParams((int) ww, (int) hh));
        mCameraLayout.setX(mCameraLayout.getX() + (ww-ww/1.1f)/2f);
        mCameraLayout.setY(mCameraLayout.getY() + (hh-hh/1.1f)/2f);
        hasZoom = true;
    }
}