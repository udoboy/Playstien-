package com.example.dfimusic;

import static java.lang.System.out;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.v4.media.MediaMetadataCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.dfimusic.Adapters.SongsListAdapter;
import com.example.dfimusic.LocalDAtabases.Favourites;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.Models.MyMediaPlayer;
import com.example.dfimusic.Services.MusicService;
import com.example.dfimusic.databinding.ActivityFavouritesListBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FavouritesListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    ActivityFavouritesListBinding b;
    List<AudioModel> favouriteSong;
    Favourites favourites;
    List<String> favouritePath;
    List<AudioModel> favouriteSongModel;
    SongsListAdapter songsListAdapter;
    String playStatus;
    MediaPlayer mediaPlayer;
    String currentPath;
    String currentTitle;
    String currentArtist;
    Hub thub;
    MyRunnable myRunnable;
    static Handler uiHandler;
    public static final int ALL_SONGS_LOADER_ID = 10;

    public static class MyRunnable implements Runnable {
        MediaPlayer mediaPlayer;
        ImageView faPlayPause;

        public MyRunnable(MediaPlayer mediaPlayer, ImageView faPlayPause) {
            this.mediaPlayer = mediaPlayer;
            this.faPlayPause = faPlayPause;
        }

        @Override
        public void run() {
            if (mediaPlayer != null){
                if (mediaPlayer.isPlaying()){
                    faPlayPause.setImageResource(R.drawable.ic_music_playing);
                }
                else{
                    faPlayPause.setImageResource(R.drawable.ic_pause);
                }
            }
            uiHandler.post(this);
        }
    }

    @Override
    protected void onDestroy() {
        b.favListRec.setAdapter(null);
        uiHandler.removeCallbacksAndMessages(null);
        uiHandler.removeCallbacks(myRunnable);
        b.getRoot().removeCallbacks(myRunnable);
        thub = null;
        b.faPlayPause.setOnClickListener(null);
        b.lDetails.setOnClickListener(null);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityFavouritesListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        getSupportActionBar().hide();

        favourites = new Favourites(getApplicationContext());
        favouritePath = new ArrayList<>();
        favouriteSong = new ArrayList<>();
        favouriteSongModel = new ArrayList<>();
        songsListAdapter = new SongsListAdapter((ArrayList<AudioModel>) favouriteSong, getApplicationContext());
        b.favListRec.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        b.favListRec.setLayoutManager(linearLayoutManager);
        b.favListRec.setAdapter(songsListAdapter);
        thub = new Hub(getApplicationContext());


        mediaPlayer = MyMediaPlayer.getInstance();
        getFavouritePaths();

        uiHandler = new Handler();
        myRunnable = new MyRunnable(mediaPlayer, b.faPlayPause);
        b.getRoot().post(myRunnable);


        currentPath = getSharedPreferences("playingStatus", MODE_PRIVATE).getString("playingPath", "none");
        currentArtist = getSharedPreferences("currentArtist", MODE_PRIVATE).getString("currentArtist", "none");
        currentTitle = getSharedPreferences("currentTitle", MODE_PRIVATE).getString("currentTitle", "none");

        if (currentPath.equals("none")){
            b.faCurrentSongTitle.setText("Your Playing song will appear here");
            b.faCurrentSongArtist.setText("PlayStein");
        }
        else{
            b.faCurrentSongArtist.setText(currentArtist);
            b.faCurrentSongTitle.setText(currentTitle);
            setBitmap(currentPath);
        }
        b.faPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPath.equals("none")){

                }
                else{
                    pausePlay();
                }
            }
        });

        androidx.lifecycle.Observer<HashMap<String, String>> currentHashObserver = new Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> stringObjectHashMap) {
                b.faCurrentSongTitle.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_TITLE));
                b.faCurrentSongArtist.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_ARTIST));
                currentPath = stringObjectHashMap.get("songPath");
                setBitmap(currentPath);
                songsListAdapter.notifyDataSetChanged();
            }
        };
        Hub.getCurrentHashData().observe(this, currentHashObserver);



        b.lDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPath = getSharedPreferences("playingStatus", MODE_PRIVATE).getString("playingPath", "none");
                if (!currentPath.equals("none")){
                    songsListAdapter.playPath(currentPath);
                }

            }
        });






    }

    public void setBitmap(String path){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte[] albumCover = mediaMetadataRetriever.getEmbeddedPicture();

        if (albumCover == null){
            b.faMetaImage.setImageResource(R.drawable.my_display_disk);
        }
        else{
            Bitmap songCoverArt = BitmapFactory.decodeByteArray(albumCover, 0, albumCover.length);
            b.faMetaImage.setImageBitmap(songCoverArt);
        }

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
            out.println("getting current from shuffle");
            return Hub.getShuffledSong();

        }
        else{
            out.println("getting current from song model");
            return Hub.getSongsModel();
        }

    }


    public void convertToAudioModel(){
        if (favouritePath.isEmpty()){
            b.favListRec.setVisibility(View.VISIBLE);
        }
        else{
            favouriteSong.clear();
            b.txtFavWillAppear.setVisibility(View.GONE);
           if (!favouriteSongModel.isEmpty()){
               for (String path : favouritePath){
                   for (AudioModel song: favouriteSongModel){
                       if (song.getPath().equals(path)){
                           favouriteSong.add(song);
                       }

                   }
               }

           }
           songsListAdapter.notifyDataSetChanged();
           b.favnumber.setText("Favourites "+ "("+ favouriteSong.size()+ ")" );

        }
    }



    public void getFavouritePaths(){
        favouritePath.clear();
        GetCursorData getCursorData = new GetCursorData(getApplicationContext(),"favourites",favourites);
        Cursor cursor = getCursorData.loadInBackground();
        while (cursor.moveToNext()){
            String path = cursor.getString(1);
            favouritePath.add(path);

        }
        cursor.close();
        favourites.close();
      // getAllSongs();
        getSupportLoaderManager().restartLoader(ALL_SONGS_LOADER_ID, null, this);

    }





    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if(id == 10){
            String[] projection ={
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DATE_ADDED

            };

            String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
            return new CursorLoader(getApplicationContext(),MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,selection, null, null);
        }
        else{
            return null;
        }

    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == 10){
            favouriteSongModel.clear();
            while(cursor.moveToNext()){
                AudioModel songData = new AudioModel(
                        cursor.getString(1),
                        cursor.getString(0),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8)

                );
                favouriteSongModel.add(songData);
            }


            convertToAudioModel();
            if (favouriteSong.size() ==0){
                b.txtFavWillAppear.setVisibility(View.VISIBLE);
            }
            else{
                b.txtFavWillAppear.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}