package com.example.dfimusic;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public class MyDialogCreator extends Dialog {
    public MyDialogCreator(@NonNull Context context, int layout, boolean cancelable, OnCancelListener onCancelListener) {
        super(context);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        getWindow().setAttributes(params);
        setCancelable(cancelable);
        setOnCancelListener(onCancelListener);
        View view = LayoutInflater.from(context).inflate(layout, null);
        setContentView(view);
    }
}
