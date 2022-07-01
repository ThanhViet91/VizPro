package com.thanhlv.vizpro.ui.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.thanhlv.vizpro.R;
import com.thanhlv.vizpro.ui.fragments.FragmentFAQ;
import com.thanhlv.vizpro.ui.services.ControllerService;
import com.thanhlv.vizpro.ui.services.streaming.StreamingService;
import com.thanhlv.vizpro.ui.utils.MyUtils;
import com.google.android.material.snackbar.Snackbar;
import com.takusemba.rtmppublisher.helper.StreamProfile;

import java.util.Objects;

import static com.thanhlv.vizpro.ui.utils.MyUtils.hideStatusBar;
import static com.thanhlv.vizpro.ui.utils.MyUtils.isMyServiceRunning;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static boolean active = false;
    private static final boolean DEBUG = MyUtils.DEBUG;
    private static final int PERMISSION_REQUEST_CODE = 3004;
    private static final int PERMISSION_DRAW_OVER_WINDOW = 3005;
    private static final int PERMISSION_RECORD_DISPLAY = 3006;
    private static String[] mPermission = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public int mMode = MyUtils.MODE_RECORDING;

    private Intent mScreenCaptureIntent = null;


    private int mScreenCaptureResultCode = MyUtils.RESULT_CODE_FAILED;

    private StreamProfile mStreamProfile;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(MyUtils.KEY_CONTROLlER_MODE, mMode);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState!=null){
            mMode = savedInstanceState.getInt(MyUtils.KEY_CONTROLlER_MODE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hideStatusBar(this);
        setContentView(R.layout.activity_main);
        initViews();

        Intent intent = getIntent();
        if(intent!=null)
            handleIncomingRequest(intent);
    }

    private void handleIncomingRequest(Intent intent) {
        if(intent != null) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case MyUtils.ACTION_START_CAPTURE_NOW:
                    mImgRec.performClick();
                    break;
            }
        }
    }

    private void requestScreenCaptureIntent() {
        if(mScreenCaptureIntent == null){
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), PERMISSION_RECORD_DISPLAY);
        }
    }

    private ImageView mImgRec, mImgFAQ;

    private void initViews() {

        // initialise pulsator layout
        PulsatorLayout pulsator = (PulsatorLayout) findViewById(R.id.pulsator);
        pulsator.start();

        //
        mImgRec =  findViewById(R.id.img_record);
        mImgFAQ =  findViewById(R.id.img_btn_faq);

        mImgRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMyServiceRunning(getApplicationContext(), StreamingService.class))
                {
                    MyUtils.showSnackBarNotification(mImgRec, "You are in Streaming Mode. Please close stream controller", Snackbar.LENGTH_INDEFINITE);
                    return;
                }
                if(isMyServiceRunning(getApplicationContext(), ControllerService.class)){
                    MyUtils.showSnackBarNotification(mImgRec,"Recording service is running!", Snackbar.LENGTH_LONG);
                    return;
                }
                mMode = MyUtils.MODE_RECORDING;

                shouldStartControllerService();

            }
        });

        mImgFAQ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.frame_layout_fragment, new FragmentFAQ(), "")
                        .addToBackStack("")
                        .commit();
            }
        });

        LinearLayout react_cam = findViewById(R.id.ln_react_cam);
        react_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ReactCamActivity.class);
                startActivity(intent);

            }
        });

    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // PERMISSION DRAW OVER
            if(!Settings.canDrawOverlays(this)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERMISSION_DRAW_OVER_WINDOW);
            }
            ActivityCompat.requestPermissions(this, mPermission, PERMISSION_REQUEST_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    int granted = PackageManager.PERMISSION_GRANTED;
                    for(int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != granted) {
                            MyUtils.showSnackBarNotification(mImgRec,"Please grant all permissions to record screen.", Snackbar.LENGTH_LONG);
                            return;
                        }
                    }
                    shouldStartControllerService();
                }
                break;
            }
        }
    }

    public void shouldStartControllerService() {
        if(!hasCaptureIntent())
            requestScreenCaptureIntent();

        if(hasPermission()) {
            startControllerService();
        }
        else{
            requestPermissions();
//            if(!hasCaptureIntent())
//                requestScreenCaptureIntent();

        }
    }

    private boolean hasCaptureIntent() {
        return mScreenCaptureIntent != null;// || mScreenCaptureResultCode == MyUtils.RESULT_CODE_FAILED;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PERMISSION_DRAW_OVER_WINDOW) {

            //Check if the permission is granted or not.
            if (resultCode != RESULT_OK) { //Permission is not available
                MyUtils.showSnackBarNotification(mImgRec, "Draw over other app permission not available.",Snackbar.LENGTH_SHORT);
            }
        }
        else if( requestCode == PERMISSION_RECORD_DISPLAY) {
            if(resultCode != RESULT_OK){
                MyUtils.showSnackBarNotification(mImgRec, "Recording display permission not available.",Snackbar.LENGTH_SHORT);
                mScreenCaptureIntent = null;
            }
            else{
                mScreenCaptureIntent = data;
                mScreenCaptureIntent.putExtra(MyUtils.SCREEN_CAPTURE_INTENT_RESULT_CODE, resultCode);
                mScreenCaptureResultCode = resultCode;

                shouldStartControllerService();
            }
        }
        else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void startControllerService() {
        Intent controller = new Intent(MainActivity.this, ControllerService.class);

        controller.setAction(MyUtils.ACTION_INIT_CONTROLLER);

        controller.putExtra(MyUtils.KEY_CAMERA_AVAILABLE, checkCameraHardware(this));

        controller.putExtra(MyUtils.KEY_CONTROLlER_MODE, mMode);

        controller.putExtra(Intent.EXTRA_INTENT, mScreenCaptureIntent);

        if(mMode == MyUtils.MODE_STREAMING) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(MyUtils.STREAM_PROFILE, mStreamProfile);
            controller.putExtras(bundle);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(controller);
        }
        else{
            startService(controller);
        }

        if(mMode==MyUtils.MODE_RECORDING)
            finish();
    }

     /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasPermission(){
        int granted = PackageManager.PERMISSION_GRANTED;

        return ContextCompat.checkSelfPermission(this, mPermission[0]) == granted
                && ContextCompat.checkSelfPermission(this, mPermission[1]) == granted
                    && ContextCompat.checkSelfPermission(this, mPermission[2]) == granted
                        && Settings.canDrawOverlays(this)
                            && mScreenCaptureIntent != null
                                && mScreenCaptureResultCode != MyUtils.RESULT_CODE_FAILED;
    }

    public void setStreamProfile(StreamProfile streamProfile) {
        this.mStreamProfile = streamProfile;

    }

    public void notifyUpdateStreamProfile() {
        if(mMode == MyUtils.MODE_STREAMING){
            Intent controller = new Intent(MainActivity.this, ControllerService.class);

            controller.setAction(MyUtils.ACTION_UPDATE_STREAM_PROFILE);
            Bundle bundle = new Bundle();
            bundle.putSerializable(MyUtils.STREAM_PROFILE, mStreamProfile);
            controller.putExtras(bundle);
            startService(controller);
        }
    }


}

