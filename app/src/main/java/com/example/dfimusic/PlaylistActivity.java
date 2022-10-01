package com.example.dfimusic;

import static java.lang.System.out;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import android.support.v4.media.MediaMetadataCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.dfimusic.Adapters.SongsListAdapter;
import com.example.dfimusic.LocalDAtabases.PlayListDatabase;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.Models.MyMediaPlayer;
import com.example.dfimusic.Services.MusicService;
import com.example.dfimusic.databinding.ActivityPlaylistBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class PlaylistActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
     ActivityPlaylistBinding b;
    List<AudioModel> playlistAudioModel;
    List<String>playListPaths;
    ArrayList<AudioModel> playListSongs;
    SongsListAdapter songsListAdapter;
    String playStatus;
    Hub hub;
    static Handler p_UiHandler;
    pRunnable myPRunnable;
    MediaPlayer mediaPlayer;
    String currentPath;
    String currentTitle;
    String currentArtist;
    public static int P_LIST_ALL_SONG_ID = 11;

    public static class pRunnable implements Runnable{
        MediaPlayer mediaPlayer;
        ImageView pausePlay;

        public pRunnable(MediaPlayer mediaPlayer, ImageView pausePlay) {
            this.mediaPlayer = mediaPlayer;
            this.pausePlay = pausePlay;
        }

        @Override
        public void run() {
            if (mediaPlayer != null){
                if (mediaPlayer.isPlaying()){
                   pausePlay.setImageResource(R.drawable.ic_music_playing);
                }
                else{
                   pausePlay.setImageResource(R.drawable.ic_pause);
                }
            }

            p_UiHandler.post(this);
        }
    }

    @Override
    protected void onDestroy() {
        p_UiHandler.removeCallbacksAndMessages(null);
        p_UiHandler.removeCallbacks(myPRunnable);
        b.getRoot().removeCallbacks(myPRunnable);
        b.playListRec.setAdapter(null);
        hub = null;
        b.plPlayPause.setOnClickListener(null);
        b.lDetails.setOnClickListener(null);
        b.miniRel.setOnClickListener(null);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityPlaylistBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        getSupportActionBar().hide();
        hub = new Hub(getApplicationContext());
        mediaPlayer = MyMediaPlayer.getInstance();

        //arraylist init
        playlistAudioModel = new ArrayList<>();
        playListPaths = new ArrayList<>();
        playListSongs = new ArrayList<>();

        currentPath = getSharedPreferences("playingStatus", MODE_PRIVATE).getString("playingPath", "none");
        currentArtist = getSharedPreferences("currentArtist", MODE_PRIVATE).getString("currentArtist", "none");
        currentTitle = getSharedPreferences("currentTitle", MODE_PRIVATE).getString("currentTitle", "none");




        String playListName = getIntent().getStringExtra("playListName");
        songsListAdapter = new SongsListAdapter(playListSongs, getApplicationContext(), playListName );
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        b.playListRec.setLayoutManager(linearLayoutManager);
        b.playListRec.setHasFixedSize(true);
        b.playListRec.setAdapter(songsListAdapter);
        b.playListName.setText(playListName);
        getPlayListItems(playListName);




        //Onclick Listeners
        b.lDetails.setOnClickListener(view -> songsListAdapter.playPath(playStatus));


        b.plPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentPath.equals("none")){

                }
                else{
                    pausePlay();
                }
            }
        });


        p_UiHandler = new Handler();
        myPRunnable = new pRunnable(mediaPlayer, b.plPlayPause);
        b.getRoot().post(myPRunnable);

        androidx.lifecycle.Observer<HashMap<String, String>> currentHashObserver = new androidx.lifecycle.Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> stringObjectHashMap) {
                b.plCurrentSongTitle.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_TITLE));
                b.plCurrentSongArtist.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_ARTIST));
                currentPath = stringObjectHashMap.get("songPath");
                setBitmap(currentPath);
                songsListAdapter.notifyDataSetChanged();
            }
        };
        Hub.getCurrentHashData().observe(this, currentHashObserver);




    }


    public List<AudioModel> getCurrentList(){
        String currentMode = Hub.getCurrentMode();
        if (currentMode.equals("shuffle")){
            out.println("getting current from shuffle");
            return Hub.getShuffledSong();

        }
        else{
            out.println("getting current from song model");
            return Hub.getSongsModel();
        }

    }


    public void getPlayListItems(String playlistname){
        PlayListDatabase playListDatabase= new PlayListDatabase(getApplicationContext());
        Cursor cursor =playListDatabase.getPlayListItems(playlistname);
        if (cursor.getCount() ==0){
        }

        while (cursor.moveToNext()){
            String playListItem = cursor.getString(2);
            playListPaths.add(playListItem);
        }
        playListDatabase.close();
        cursor.close();
        getSupportLoaderManager().restartLoader(P_LIST_ALL_SONG_ID, null, this);

    }

    public void getAllSongs(){
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
        Cursor songCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
        while(songCursor.moveToNext()){
            AudioModel songData = new AudioModel(songCursor.getString(1),
                    songCursor.getString(0),
                    songCursor.getString(2),
                    songCursor.getString(3),
                    songCursor.getString(4),
                    songCursor.getString(5),
                    songCursor.getString(6),
                    songCursor.getString(7),
                    songCursor.getString(8)
            );
           playlistAudioModel.add(songData);
        }
        songCursor.close();

        convertToAudioModel();
    }


    public void convertToAudioModel(){
        if (playListPaths.isEmpty()){
           // b.playListRec.setVisibility(View.VISIBLE);
            b.txtNoSongs.setVisibility(View.VISIBLE);
        }
        else{
            b.txtNoSongs.setVisibility(View.GONE);
            if (!playlistAudioModel.isEmpty()){
                for (String path : playListPaths){
                    for (AudioModel song: playlistAudioModel){
                        if (song.getPath().equals(path)){
                            playListSongs.add(song);
                        }

                    }
                }

            }
            songsListAdapter.notifyDataSetChanged();
            b.plNumberOfSongs.setText("("+ playListSongs.size() + ")" + " Songs");


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

        Intent playMusicService = new Intent(PlaylistActivity.this, MusicService.class);
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


    public void setBitmap(String path){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte[] albumCover = mediaMetadataRetriever.getEmbeddedPicture();

        if (albumCover == null){
            b.plMetaImage.setImageResource(R.drawable.my_display_disk);
        }
        else{
            Bitmap songCoverArt = BitmapFactory.decodeByteArray(albumCover, 0, albumCover.length);
            b.plMetaImage.setImageBitmap(songCoverArt);
        }

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == 11){
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
            return new CursorLoader(getApplicationContext(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null);
        }
        else{
            return null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == 11){
            while(cursor.moveToNext()){
                AudioModel songData = new AudioModel(cursor.getString(1),
                        cursor.getString(0),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8)
                );
                playlistAudioModel.add(songData);
            }
            cursor.close();
            convertToAudioModel();
            if (playListSongs.size() ==0){
                b.txtNoSongs.setVisibility(View.VISIBLE);
            }
            else{
                b.txtNoSongs.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}