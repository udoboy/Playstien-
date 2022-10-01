package com.example.dfimusic.Models;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.dfimusic.R;

public class OptionsDialog extends Dialog {
    public OptionsDialog(@NonNull Context context) {
        super(context);


        WindowManager.LayoutParams params = getWindow().getAttributes();
        getWindow().setAttributes(params);
        //params.gravity = Gravity.BOTTOM;
        setTitle(null);
        setCancelable(true);
        setOnCancelListener(null);
        View view = LayoutInflater.from(context).inflate(R.layout.songs_options_layout, null);
        setContentView(view);
    }
}
