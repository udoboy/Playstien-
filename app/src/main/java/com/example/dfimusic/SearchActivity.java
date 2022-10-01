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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;

import com.example.dfimusic.Adapters.SongsListAdapter;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.Models.MyMediaPlayer;
import com.example.dfimusic.Services.MusicService;
import com.example.dfimusic.databinding.ActivitySearchBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    static ActivitySearchBinding b;
    ArrayList<AudioModel> searchlist;
    static SongsListAdapter songsListAdapter;
    MediaPlayer mediaPlayer;
    String currentPath;
    String currentTitle;
    String currentArtist;
    List<AudioModel> allSongsList;
    public static int ALL_SONG_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        b = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        getSupportActionBar().hide();
        searchlist = new ArrayList<>();
        songsListAdapter = new SongsListAdapter(searchlist, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        b.searchResultsRec.setAdapter(songsListAdapter);
        b.searchResultsRec.setLayoutManager(linearLayoutManager);
        allSongsList = new ArrayList<>();
        getSupportLoaderManager().restartLoader(ALL_SONG_LOADER_ID, null, this);
        //songsList = new ArrayList<>();


        currentPath = getSharedPreferences("playingStatus", MODE_PRIVATE).getString("playingPath", "none");
        currentArtist = getSharedPreferences("currentArtist", MODE_PRIVATE).getString("currentArtist", "none");
        currentTitle = getSharedPreferences("currentTitle", MODE_PRIVATE).getString("currentTitle", "none");


        /** below is used to watch the mediametadata, when it changes, the code in this codeblock is executed **/
        androidx.lifecycle.Observer<HashMap<String, String>> currentHashObserver = new androidx.lifecycle.Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> stringObjectHashMap) {
                b.seCurrentSongTitle.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_TITLE));
                b.seCurrentSongArtist.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_ARTIST));
                currentPath = stringObjectHashMap.get("songPath");
                setBitmap(currentPath);
                songsListAdapter.notifyDataSetChanged();
            }
        };
        Hub.getCurrentHashData().observe(this, currentHashObserver);

        if (currentPath.equals("none")){
            b.seCurrentSongTitle.setText("Your Playing song will appear here");
            b.seCurrentSongArtist.setText("PlayStein");
        }
        else{
            b.seCurrentSongArtist.setText(currentArtist);
            b.seCurrentSongTitle.setText(currentTitle);
            setBitmap(currentPath);
        }

        mediaPlayer = MyMediaPlayer.getInstance();

        SearchActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null){
                    if (mediaPlayer.isPlaying()){
                        b.sePlayPause.setImageResource(R.drawable.ic_music_playing);
                    }
                    else{
                        b.sePlayPause.setImageResource(R.drawable.ic_pause);
                    }
                }

                new Handler().postDelayed(this,0);
            }
        });


        b.sePlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentPath.equals("none")){

                }
                else{
                    pausePlay();
                }
            }
        });



        b.imgCloseSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        b.edtkeywords.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString() != "") {
                    findSong(charSequence.toString());
                } else {

                }


            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")) {
                    searchlist.clear();
                    songsListAdapter.notifyDataSetChanged();
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
    }

    public void setBitmap(String path){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte[] albumCover = mediaMetadataRetriever.getEmbeddedPicture();

        if (albumCover == null){
            b.seMetaImage.setImageResource(R.drawable.my_display_disk);
        }
        else{
            Bitmap songCoverArt = BitmapFactory.decodeByteArray(albumCover, 0, albumCover.length);
            b.seMetaImage.setImageBitmap(songCoverArt);
        }

    }
    public void findSong(String keyword) {
        searchlist.clear();
        for (AudioModel audioModel : allSongsList) {
            if (audioModel.getTitle().toLowerCase().contains(keyword.toLowerCase()) || audioModel.getArtist().toLowerCase().contains(keyword.toLowerCase())) {
                searchlist.add(audioModel);
            } else {
                searchlist.remove(audioModel);
            }
        }
        Collections.reverse(searchlist);
        songsListAdapter.notifyDataSetChanged();

    }



    @Deprecated
    public void inflateDummyList(){
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
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection ,
                null,
                null);

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

            if (new File(songData.getPath()).exists()){
                allSongsList.add(songData);
            }
        }
        cursor.close();

        if (allSongsList.size() == 0){
            //write your code here
        }
    }


    public void pausePlay(){
        if (MyMediaPlayer.currentIndex == -1){
            Hub hub = new Hub(this);
            for (AudioModel audioModel: getCurrentList() ){
                if (audioModel.getPath().equals(currentPath)){
                    Hub.setCurrentSong(audioModel);
                }
            }
        }

        Intent playMusicService = new Intent(SearchActivity.this, MusicService.class);
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

    public List<AudioModel> getCurrentList() {
        if (Hub.getCurrentMode().equals("shuffle")) {
            out.println("getting current from shuffle");
            return Hub.getShuffledSong();

        } else {
            out.println("getting current from song model");
            return Hub.getSongsModel();
        }
    }

    @Override
    protected void onPostResume () {
       // songsListAdapter.notifyDataSetChanged();
        super.onPostResume();
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        b.searchResultsRec.setVisibility(View.GONE);
        b.fetchingProgress.setVisibility(View.VISIBLE);
        b.edtkeywords.setEnabled(false);
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

        return new CursorLoader(this,MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
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

            if (new File(songData.getPath()).exists()){
                allSongsList.add(songData);
            }
        }
        b.edtkeywords.setEnabled(true);
        b.searchResultsRec.setVisibility(View.VISIBLE);
        b.fetchingProgress.setVisibility(View.GONE);

        if (allSongsList.size() == 0){
            //write your code here
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}
