package com.example.dfimusic;

import static java.lang.System.out;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.HardwareRenderer;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HardwarePropertiesManager;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.dfimusic.Adapters.OptionsFragmentAdapter;
import com.example.dfimusic.Adapters.SongsListAdapter;
import com.example.dfimusic.Fragments.AllSongsFragment;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.Models.MyMediaPlayer;
import com.example.dfimusic.Services.MusicService;
import com.example.dfimusic.databinding.ActivitySongOptionsBinding;
import com.example.dfimusic.databinding.SongsOptionsLayoutBinding;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

public class SongOptionsActivity extends AppCompatActivity {
    ActivitySongOptionsBinding b;
    String currentPath;
    String currentArtist;
    String currentTitle;
    Hub hub;
    MyUiRunnable myUiRunnable;
    MediaPlayer mediaPlayer;
    SongsListAdapter songsListAdapter;
    MediaMetadataRetriever mediaMetadataRetriever;
    int sortValue = 0;
    Hub thub;
   // List<AudioModel> keptList;
    static Handler uiHandler;
    //Runnable uiRunnable;
    ActivityManager activityManager;


    public static class MyUiRunnable implements  Runnable{
        ImageView asPlayPause;
        MediaPlayer mediaPlayer;



        public MyUiRunnable(ImageView asPlayPause, MediaPlayer mediaPlayer) {
            this.asPlayPause = asPlayPause;
            this.mediaPlayer = mediaPlayer;


        }

        @Override
        public void run() {
            if (mediaPlayer != null){
                if (mediaPlayer.isPlaying()){
                    asPlayPause.setImageResource(R.drawable.ic_music_playing);
                }
                else{
                    asPlayPause.setImageResource(R.drawable.ic_pause);
                }
            }
            else{

            }
            uiHandler.post(this);

        }
    }

    @Override
    protected void onDestroy() {
        uiHandler.removeCallbacksAndMessages(null);
        uiHandler.removeCallbacks(myUiRunnable);

       // myUiRunnable = null;
       // onStop();
        songsListAdapter = null;
        hub = null;
        activityManager = null;
        b.asPlayPause.setOnClickListener(null);
        b.miniRel.setOnClickListener(null);
        b.imgSearch.setOnClickListener(null);
        b.imgSortOrder.setOnClickListener(null);
        b.viewPager.setAdapter(null);
        b = null;
        thub = null;
        mediaMetadataRetriever = null;
        finish();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b= ActivitySongOptionsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        getSupportActionBar().hide();
        mediaPlayer = MyMediaPlayer.getInstance();
        hub = new ViewModelProvider(this).get(Hub.class);
        songsListAdapter = new SongsListAdapter(getApplicationContext());
        thub = new Hub(getApplicationContext());

       // keptList = new ArrayList<>();

        b.viewPager.setAdapter(new OptionsFragmentAdapter(getSupportFragmentManager()));
        b.tabLayout.setupWithViewPager(b.viewPager);
        b.viewPager.setOffscreenPageLimit(3);
        b.tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        b.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        activityManager = (ActivityManager) getApplication().getSystemService(ACTIVITY_SERVICE);
        out.println("islow ram device "+activityManager.isLowRamDevice());
        out.println("memory class "+activityManager.getMemoryClass());

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        out.println("memory info" + memoryInfo.totalMem);
       // memory info  1451589632
       // memory info  2931335168

        uiHandler = new Handler();
        myUiRunnable = new MyUiRunnable(b.asPlayPause, mediaPlayer);
        myUiRunnable.run();

        /** below is used to watch the mediametadata, when it changes, the code in this codeblock is executed **/
        Observer<HashMap<String, String>> currentHashObserver = new Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> stringObjectHashMap) {
                out.println("hashObserver"+ stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_TITLE));
                if (MyMediaPlayer.currentIndex != -1){
                    b.asCurrentSongTitle.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_TITLE));
                    b.asCurrentSongArtist.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_ARTIST));
                    currentPath = stringObjectHashMap.get("songPath");
                    setBitmap(currentPath);
                }
                else{
                    out.println(MyMediaPlayer.currentIndex);
                }

            }
        };
        Hub.getCurrentHashData().observe(this, currentHashObserver);

        out.println("current index on start is"+ MyMediaPlayer.currentIndex);
        currentPath = getSharedPreferences("playingStatus", MODE_PRIVATE).getString("playingPath", "none");
        currentArtist = getSharedPreferences("currentArtist", MODE_PRIVATE).getString("currentArtist", "none");
        currentTitle = getSharedPreferences("currentTitle", MODE_PRIVATE).getString("currentTitle", "none");



        if (currentPath.equals("none") && !new File(currentPath).exists()){
           b.asCurrentSongTitle.setText("Your Playing song will appear here");
           b.asCurrentSongArtist.setText("PlayStein");
        }
        else{
          b.asCurrentSongArtist.setText(currentArtist);
          b.asCurrentSongTitle.setText(currentTitle);
            setBitmap(currentPath);
        }

       b.asPlayPause.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if (currentPath.equals("none") && !new File(currentPath).exists()){

               }
               else{
                   pausePlay();
               }
           }
       });
        b.miniRel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPath = getSharedPreferences("playingStatus", MODE_PRIVATE).getString("playingPath", "none");
                if (currentPath != "none"){
                    songsListAdapter.playPath(currentPath);
                }
            }
        });
        b.imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SearchActivity.class));

            }
        });
        b.imgSortOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(),  view);
                popupMenu.getMenu().add("Alphabetical ascending");
                popupMenu.getMenu().add("Alphabetical descending");
                if (getItemNameForSortChange().equals("artists_sort") || getItemNameForSortChange().equals("albums_sort") || getItemNameForSortChange().equals("genre_sort")){

                }
                else{
                    popupMenu.getMenu().add("Date added ascending");
                    popupMenu.getMenu().add("Date added descending");
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getTitle().equals("Alphabetical ascending")){
                            makeSharedPrefs(getItemNameForSortChange(), getItemNameForSortChange(), menuItem.getTitle().toString());
                        }
                        else if (menuItem.getTitle().equals("Alphabetical descending")){
                            makeSharedPrefs(getItemNameForSortChange(), getItemNameForSortChange(), menuItem.getTitle().toString());

                        }
                        else if (menuItem.getTitle().equals("Date added ascending")){
                            makeSharedPrefs(getItemNameForSortChange(), getItemNameForSortChange(), menuItem.getTitle().toString());

                        }
                        else if(menuItem.getTitle().equals("Date added descending")){
                            makeSharedPrefs(getItemNameForSortChange(), getItemNameForSortChange(), menuItem.getTitle().toString());

                        }
                        sortValue ++;
                        hub.getSortChangeInteger().setValue(sortValue);
                        return true;
                    }
                });
                popupMenu.show();


            }
        });
    }

    public void makeSharedPrefs(String prefsName, String itemName, String itemValue){
        getSharedPreferences(prefsName, MODE_PRIVATE).edit().putString(itemName, itemValue).apply();
    }

    public String getItemNameForSortChange(){
        String itemName = "null";
        switch (b.viewPager.getCurrentItem()){
            case 0:
                itemName = "all_songs_sort"; break;
            case 1:
                itemName = "artists_sort"; break;
            case 2:
                itemName = "albums_sort"; break;
            case 3:
                itemName = "genre_sort"; break;
            default:
                itemName = "null"; break;
        }
        return itemName;
    }




    public void pausePlay(){
        if (MyMediaPlayer.currentIndex == -1){
            for (AudioModel audioModel: getCurrentList() ){
                if (audioModel.getPath().equals(currentPath)){
                    Hub.setCurrentSong(audioModel);
                }
            }
        }

        Intent playMusicService = new Intent(getApplicationContext(), MusicService.class);
        if (mediaPlayer.isPlaying()){
            playMusicService.setAction("android.intent.action.MEDIA_BUTTON");
            playMusicService.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));

        }
        else{
            playMusicService.setAction("android.intent.action.MEDIA_BUTTON");
            playMusicService.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(playMusicService);
        }
        else{
            startService(playMusicService);
        }


    }

    public List<AudioModel> getCurrentList(){
        if (Hub.getCurrentMode().equals("shuffle")){
            return Hub.getShuffledSong();

        }
        else{
            return Hub.getSongsModel();
        }

    }


    public void setBitmap(String path){
        if(new File(path).exists()){
             mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(path);
            byte[] albumCover = mediaMetadataRetriever.getEmbeddedPicture();
            if (albumCover == null){
                b.asMetaImage.setImageResource(R.drawable.my_display_disk);
            }
            else{
                Bitmap songCoverArt = BitmapFactory.decodeByteArray(albumCover, 0, albumCover.length);
                b.asMetaImage.setImageBitmap(songCoverArt);

            }

        }


    }
}