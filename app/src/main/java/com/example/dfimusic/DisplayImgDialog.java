package com.example.dfimusic;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public class
DisplayImgDialog extends Dialog {
    public DisplayImgDialog(@NonNull Context context) {
        super(context);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        getWindow().setAttributes(params);
        //params.gravity = Gravity.BOTTOM;
        setTitle(null);
        setCancelable(true);
        setOnCancelListener(null);
        View view = LayoutInflater.from(context).inflate(R.layout.display_img_dialog, null);
        setContentView(view);
    }
}
