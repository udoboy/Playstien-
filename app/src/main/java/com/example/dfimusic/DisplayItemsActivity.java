package com.example.dfimusic;

import static java.lang.System.out;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.dfimusic.Adapters.SongsListAdapter;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.Models.MembersMode;
import com.example.dfimusic.Models.MyMediaPlayer;
import com.example.dfimusic.Services.MusicService;
import com.example.dfimusic.databinding.ActivityDisplayItemsBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DisplayItemsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    ActivityDisplayItemsBinding b;
    String nameList;
    String focusName;
    ArrayList<AudioModel> displayList;
    SongsListAdapter songsListAdapter;
    String currentPath;
    String currentArtist;
    String currentTitle;
    String samplePath;
    MediaPlayer mediaPlayer;
    List<MembersMode> membersModelList;
    Hub hub;
    Hub thub;
    List<AudioModel> allSongsList;
    DisplayImgDialog displayImgDialog;
    ImageView fullSongCover;
    MyRunnable myRunnable;
    public static final int MEMBERS_MODEL_LOADER = 7;
    public static final int DUMMY_LIST_LOADER = 8;

   static Handler uiHandler;
    Runnable uiRunnable;

    @Override
    protected void onDestroy() {
        uiHandler.removeCallbacks(myRunnable);
        uiHandler.removeCallbacksAndMessages(null);
        uiRunnable= null;
        hub = null;
        b.getRoot().removeCallbacks(myRunnable);
        b.imgCloseDisplay.setOnClickListener(null);
        b.miniRel.setOnClickListener(null);
        b.asPlayPause.setOnClickListener(null);
        b.displayListRec.setAdapter(null);
        currentPath = null;
        currentTitle = null;
        currentArtist = null;
        displayImgDialog = null;
        b.getRoot().removeCallbacks(uiRunnable);
        super.onDestroy();
    }

    public static class MyRunnable implements Runnable{
        MediaPlayer mediaPlayer;
        ImageView asPausePlay;

        public MyRunnable(MediaPlayer mediaPlayer, ImageView asPausePlay) {
            this.mediaPlayer = mediaPlayer;
            this.asPausePlay = asPausePlay;
        }

        @Override
        public void run() {
            if (mediaPlayer != null){
                if (mediaPlayer.isPlaying()){
                    asPausePlay.setImageResource(R.drawable.ic_music_playing);
                }
                else{
                   asPausePlay.setImageResource(R.drawable.ic_pause);
                }
            }
            uiHandler.postDelayed(this, 100);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityDisplayItemsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        getSupportActionBar().hide();
        b.txtFocusName.setSelected(true);
        hub = new ViewModelProvider(this).get(Hub.class);
        mediaPlayer = MyMediaPlayer.getInstance();
        b.imgCloseDisplay.bringToFront();
        b.txtFocusName.bringToFront();
        b.imgPlayAll.bringToFront();
        displayImgDialog = new DisplayImgDialog(this);
        fullSongCover = displayImgDialog.findViewById(R.id.fullSongCoverImage);
        allSongsList = new ArrayList<>();
        membersModelList = new ArrayList<>();
        thub = new Hub(getApplicationContext());
        overridePendingTransition(0,0);

        nameList = getIntent().getStringExtra("nameList");
        focusName = getIntent().getStringExtra("focusName");
        samplePath = getIntent().getStringExtra("albumId");
        displayList = new ArrayList<>();
        songsListAdapter = new SongsListAdapter(displayList, getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        b.displayListRec.setHasFixedSize(true);
        b.displayListRec.setLayoutManager(linearLayoutManager);
        b.displayListRec.setAdapter(songsListAdapter);

        if (nameList.equals("genre")){
            setMembersModel();
        }
        this.getSupportLoaderManager().restartLoader(DUMMY_LIST_LOADER,null, this);

//        uiHandler = new Handler();
//        uiRunnable = new Runnable() {
//            @Override
//            public void run() {
//                if (mediaPlayer != null){
//                    if (mediaPlayer.isPlaying()){
//                        b.asPlayPause.setImageResource(R.drawable.ic_music_playing);
//                    }
//                    else{
//                        b.asPlayPause.setImageResource(R.drawable.ic_pause);
//                    }
//                }
//
//                //uiHandler.post(this);
//            }
//        };
     //  b.getRoot().post(uiRunnable);


        uiHandler =new Handler();
        myRunnable = new MyRunnable(mediaPlayer ,b.asPlayPause);
        b.getRoot().post(myRunnable);


        currentPath = getSharedPreferences("playingStatus", MODE_PRIVATE).getString("playingPath", "none");
        currentArtist = getSharedPreferences("currentArtist", MODE_PRIVATE).getString("currentArtist", "none");
        currentTitle = getSharedPreferences("currentTitle", MODE_PRIVATE).getString("currentTitle", "none");

        /** below is used to watch the mediametadata, when it changes, the code in this codeblock is executed **/
        Observer<HashMap<String, String>> currentHashObserver = new Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> stringObjectHashMap) {
                b.asCurrentSongTitle.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_TITLE));
                b.asCurrentSongArtist.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_ARTIST));
                currentPath = stringObjectHashMap.get("songPath");
                songsListAdapter.notifyDataSetChanged();
                setBitmap(currentPath);
            }
        };
        Hub.getCurrentHashData().observe(this, currentHashObserver);

        if (currentPath.equals("none")){
            b.asCurrentSongTitle.setText("Your Playing song will appear here");
            b.asCurrentSongArtist.setText("PlayStein");
        }
        else{
            b.asCurrentSongArtist.setText(currentArtist);
            b.asCurrentSongTitle.setText(currentTitle);
            setBitmap(currentPath);
        }

       b.txtFocusName.setText(focusName);
       displayList.clear();



        b.imgCloseDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 DisplayItemsActivity.super.onBackPressed();
            }
        });

        b.miniRel.setOnClickListener(view -> songsListAdapter.playPath(currentPath));
        b.asPlayPause.setOnClickListener(view -> pausePlay());

    }


    public void setBitmap(String path){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
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

    public void setMembersModel(){
        //String audioId = null;
     String [] projection = {
             MediaStore.Audio.Genres.Members.AUDIO_ID,
             MediaStore.Audio.Genres.Members.GENRE_ID
     };

        Cursor cursor1 = getContentResolver().query(Uri.parse("content://media/external/audio/genres/all/members"), projection, null, null, null);
        while(cursor1.moveToNext()){
           MembersMode membersModel = new MembersMode(cursor1.getString(0), cursor1.getString(1));
           membersModelList.add(membersModel);
        }
        cursor1.close();
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
    public Uri getArtWork(String albumId){
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(albumId));
        return albumArtUri;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        b.fetchingProgress.setVisibility(View.VISIBLE);
                if(id == 7){
            String [] projection = {
                    MediaStore.Audio.Genres.Members.AUDIO_ID,
                    MediaStore.Audio.Genres.Members.GENRE_ID
            };
            return new CursorLoader(getApplicationContext(), Uri.parse("content://media/external/audio/genres/all/members"),projection,
                    null,null, null);
        }
        else if (id == 8){
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
            return new CursorLoader(getApplicationContext(),MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection, selection,null, null);
        }
        else{
            return null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
            if (loader.getId() == 7){
                membersModelList.clear();
        while(cursor.moveToNext()){
            MembersMode membersModel = new MembersMode(cursor.getString(0), cursor.getString(1));
            membersModelList.add(membersModel);
        }

    }
    else if (loader.getId() == 8){
                displayList.clear();
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
        if (allSongsList.size() == 0){
            //write your code here
        }
    }

        /** checking the context used to open this activity to know which list is to be loaded **/
        if (nameList.equals("artist")){
            if (samplePath != null){
                b.sampleImage.setImageURI(getArtWork(samplePath));
                b.sampleImage.setVisibility(View.VISIBLE);
                b.albumImage.setVisibility(View.GONE);

                if (b.sampleImage.getDrawable() == null){
                    b.sampleImage.setImageResource(R.drawable.iconme);
                }

            }
            // b.nameOfList.setText("(Artist)");
            displayList.clear();
            for (AudioModel audioModel:allSongsList){
                if (audioModel.getArtist().equals(focusName)){
                    displayList.add(audioModel);

                }
            }
            b.sampleImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fullSongCover.setImageURI(getArtWork(samplePath));
                    if (fullSongCover.getDrawable() == null){

                    }
                    else{
                        displayImgDialog.show();
                    }
                }
            });
            songsListAdapter.notifyDataSetChanged();
        }
        else if (nameList.equals("albums")){
            if (samplePath != null){
                b.albumImage.setImageURI(getArtWork(samplePath));
                b.sampleImage.setVisibility(View.GONE);
                b.albumImage.setVisibility(View.VISIBLE);

                if (b.albumImage.getDrawable() == null){
                    b.albumImage.setImageResource(R.drawable.iconme);
                    b.albumImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }

            }
            //  b.nameOfList.setText("(Album)");

            for (AudioModel audioModel: allSongsList){
                if (audioModel.getAlbum().equals(focusName)){
                    displayList.add(audioModel);
                }
            }
            b.albumImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fullSongCover.setImageURI(getArtWork(samplePath));
                    if (fullSongCover.getDrawable() == null){

                    }
                    else{
                        displayImgDialog.show();
                    }
                }
            });
            songsListAdapter.notifyDataSetChanged();
        }
        else if (nameList.equals("genre")){
            String genreId = getIntent().getStringExtra("genreId");

            //setMembersModel();
            for (AudioModel audioModel: allSongsList){
                for (MembersMode membersModel : membersModelList){
                    if(membersModel.getAudioId()!= null && audioModel.getSongId()!= null && membersModel.getGenreId()!= null){
                        if (membersModel.getAudioId().equals(audioModel.getSongId()) && membersModel.getGenreId().equals(genreId)){
                            displayList.add(audioModel);

                        }
                    }

                }
            }

            b.albumImage.setVisibility(View.VISIBLE);
            b.sampleImage.setVisibility(View.GONE);
            if (displayList != null){
                if (displayList.size()>0){
                    b.albumImage.setImageURI(getArtWork(displayList.get(0).getAlbumId()));
                }

            }

            if (b.albumImage.getDrawable() == null){
                b.albumImage.setImageResource(R.drawable.iconme);
            }
            b.albumImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    fullSongCover.setImageURI(getArtWork(displayList.get(0).getAlbumId()));
                    if (fullSongCover.getDrawable() == null){

                    }
                    else{
                        displayImgDialog.show();
                    }
                }
            });
            songsListAdapter.notifyDataSetChanged();


        }
        b.fetchingProgress.setVisibility(View.GONE);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

}