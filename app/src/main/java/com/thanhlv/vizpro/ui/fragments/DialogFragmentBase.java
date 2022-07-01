package com.thanhlv.vizpro.ui.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.thanhlv.vizpro.R;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Created by Le Viet Thanh on 7/6/22.
 */

public class DialogFragmentBase extends DialogFragment {
    public int getLayout() {
        return R.layout.dialog_settings_empty;
    }


    public interface CallbackFragment{
        void onClick();
    }
    @Override
    @Nullable
    public View onCreateView(@NotNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Objects.requireNonNull(Objects.requireNonNull(getDialog()).getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        Objects.requireNonNull(getDialog().getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        return requireActivity().getLayoutInflater().inflate(getLayout(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            if (getDialog() != null && getDialog().getWindow() != null) {
                getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            }
        } catch (Exception e) {
            handleException(e);
        }
        setCancelable(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    public void updateUI() {
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, android.view.KeyEvent event) {
                if ((keyCode ==  android.view.KeyEvent.KEYCODE_BACK))
                {
                    //Hide your keyboard here!!!
                    updateUI();
                    dismissAllowingStateLoss();
                    return true; // pretend we've processed it
                }
                else
                    return false; // pass on to be processed as normal
            }
        });
    }

    public static void handleException(Exception e) {
        try {
            e.printStackTrace();
            Log.e("Error", "handleException", e);
        } catch (Exception ex) {
            Log.e("Error Exception", Objects.requireNonNull(ex.getMessage()));
        }
    }
}
