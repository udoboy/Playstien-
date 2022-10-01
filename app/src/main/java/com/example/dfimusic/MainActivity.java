package com.example.dfimusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.example.dfimusic.LocalDAtabases.PlayMode;
import com.example.dfimusic.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding b;
    ManagePermissions permissions;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 100:
                if (grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    System.out.println("permission granted");
                    Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
                    startActivity(intent);
                }
                else{
                    AlertDialog.Builder alertDiaolog = new AlertDialog.Builder(this);
                    alertDiaolog.setMessage("PlayStein requires the Storage permission in order to display all the songs on this device please enable this permission in your settings before proceeding");
                    alertDiaolog.setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + getPackageName()));
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    });

                    alertDiaolog.setCancelable(false);
                    alertDiaolog.show();
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        getSupportActionBar().hide();

    }
  private  class MyAnimationListener implements Animation.AnimationListener{

      @Override
      public void onAnimationStart(Animation animation) {
          System.out.println("animation started");

      }

      @Override
      public void onAnimationEnd(Animation animation) {
          Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
          startActivity(intent);
      }

      @Override
      public void onAnimationRepeat(Animation animation) {

      }
  }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onResume() {
        permissions = new ManagePermissions(this);
        if (!permissions.checkReadExternalPermission()){
            permissions.requestReadPermission();
        }
        else{
            TranslateAnimation translateAnimation = new TranslateAnimation(0,0,0,0);
            translateAnimation.setDuration(1000);
            b.r1.setAnimation(translateAnimation);
            translateAnimation.setAnimationListener(new MyAnimationListener());
        }
        System.out.println("activity resumed");
        super.onResume();
    }
}