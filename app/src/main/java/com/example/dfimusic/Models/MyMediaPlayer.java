package com.example.dfimusic.Models;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.lifecycle.LiveData;

public class MyMediaPlayer {
     static MediaPlayer instance;
     static MediaSessionCompat mediaSessionCompat;

     public static MediaSessionCompat getMyMediaSessionCompat(Context context){
         if (mediaSessionCompat == null){
             mediaSessionCompat = new MediaSessionCompat(context, "Media session");

         }
         return mediaSessionCompat;
     }
     public static MediaPlayer getInstance(){
         if (instance == null){
             instance = new MediaPlayer();
         }
         return instance;
     }

     public static int currentIndex = -1;
}
