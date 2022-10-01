package com.example.dfimusic;

import static java.lang.System.out;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaMetadataCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dfimusic.Adapters.PlayListNamesAdapter;
import com.example.dfimusic.Adapters.SongsListAdapter;
import com.example.dfimusic.LocalDAtabases.PlayListDatabase;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.Models.MyMediaPlayer;
import com.example.dfimusic.Services.MusicService;
import com.example.dfimusic.databinding.ActivityPlayListOptionsBinding;
import com.example.dfimusic.databinding.ActivityPlaylistBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlayListOptionsActivity extends AppCompatActivity {
    ActivityPlayListOptionsBinding b;
    PlayListDatabase playListDatabase;
    List<String> playListNamesList;
    PlayListNamesAdapter playListNamesAdapter;
    String currentPath;
    String currentTitle;
    String currentArtist;
    MediaPlayer mediaPlayer;
    SongsListAdapter songsListAdapter;
    CreatePlalistDialogue createPlalistDialogue;
    ImageView cancelProcessBtn;
    static Handler opUiHandler;
    PlaylistOpUiRunnable playlistOpUiRunnable;
    Hub thub;
    String cPath;
    String songName;
    boolean exists;


    public static class PlaylistOpUiRunnable implements Runnable{
        MediaPlayer mediaPlayer;
        ImageView poPausePlay;

        public PlaylistOpUiRunnable(MediaPlayer mediaPlayer, ImageView poPausePlay) {
            this.mediaPlayer = mediaPlayer;
            this.poPausePlay = poPausePlay;
        }

        @Override
        public void run() {
            if (mediaPlayer != null){
                if (mediaPlayer.isPlaying()){
                   poPausePlay.setImageResource(R.drawable.ic_music_playing);
                }
                else{
                    poPausePlay.setImageResource(R.drawable.ic_pause);
                }
            }
            else{

            }
            opUiHandler.post(this);
        }
    }
    @Override
    protected void onDestroy() {
        opUiHandler.removeCallbacksAndMessages(null);
        opUiHandler.removeCallbacks(playlistOpUiRunnable);
        b.getRoot().removeCallbacks(playlistOpUiRunnable);
        playListDatabase.close();
        thub = null;
        b.playListNamesRec.setAdapter(null);
        createPlalistDialogue = null;
        b.middle.setOnClickListener(null);
        b.miniRel.setOnClickListener(null);
        b.poPlayPause.setOnClickListener(null);
        cancelProcessBtn.setOnClickListener(null);
        playListNamesAdapter= null;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityPlayListOptionsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        mediaPlayer = MyMediaPlayer.getInstance();
        getSupportActionBar().hide();
        playListDatabase = new PlayListDatabase(getApplicationContext());
         createPlalistDialogue = new CreatePlalistDialogue(this);
        EditText edtEnterPlayListName = createPlalistDialogue.findViewById(R.id.edtEnterPlaylistName);
        Button createPlaylistBtn = createPlalistDialogue.findViewById(R.id.createPlaylistBtn);
         cancelProcessBtn = createPlalistDialogue.findViewById(R.id.cancelProcessBtn);
         cPath = getIntent().getStringExtra("currentPath");
        songName = getIntent().getStringExtra("songName");
        out.println("playlistsongname" +songName);
        if(cPath != null){
            b.txtChooosePlaylist.setSelected(true);
            b.txtChooosePlaylist.setText("Choose playlist to add "+ songName);
            b.topRel.setVisibility(View.VISIBLE);
        }
        else{
           b.topRel.setVisibility(View.GONE);
        }
        playListNamesList = new ArrayList<>();
        songsListAdapter = new SongsListAdapter(getApplicationContext());
        initAdapter();

        disPlayPlaylistNames();
        thub = new Hub(getApplicationContext());


        b.middle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPlalistDialogue.show();

            }
        });

        b.miniRel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPath = getSharedPreferences("playingStatus", MODE_PRIVATE).getString("playingPath", "none");
                if (!currentPath.equals("none")){
                    songsListAdapter.playPath(currentPath);
                }
            }
        });

        b.imgCloseTopRel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cPath = null;
                songName = null;
                initAdapter();
                playListNamesAdapter.notifyDataSetChanged();
                b.topRel.setVisibility(View.GONE);
            }
        });

        opUiHandler = new Handler();
        playlistOpUiRunnable = new PlaylistOpUiRunnable(mediaPlayer, b.poPlayPause);
        b.getRoot().post(playlistOpUiRunnable);

        currentPath = getSharedPreferences("playingStatus", MODE_PRIVATE).getString("playingPath", "none");
        currentArtist = getSharedPreferences("currentArtist", MODE_PRIVATE).getString("currentArtist", "none");
        currentTitle = getSharedPreferences("currentTitle", MODE_PRIVATE).getString("currentTitle", "none");


        androidx.lifecycle.Observer<HashMap<String, String>> currentHashObserver = new Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> stringObjectHashMap) {
                b.poCurrentSongTitle.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_TITLE));
                b.asCurrentSongArtist.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_ARTIST));
                currentPath = stringObjectHashMap.get("songPath");
                setBitmap(currentPath);
            }
        };
        Hub.getCurrentHashData().observe(this, currentHashObserver);

        if (currentPath.equals("none")){
            b.poCurrentSongTitle.setText("Your Playing song will appear here");
            b.asCurrentSongArtist.setText("PlayStein");
        }
        else{
            b.asCurrentSongArtist.setText(currentArtist);
            b.poCurrentSongTitle.setText(currentTitle);
            setBitmap(currentPath);
        }

        b.poPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentPath.equals("none")){

                }
                else{
                    pausePlay();
                }
            }
        });


        createPlaylistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exists = false;
                String playListName = edtEnterPlayListName.getText().toString();
                if (playListName.isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
                }
                else{
                    for (String name : playListNamesList){
                        if (name.equals(playListName)){
                            Toast.makeText(getApplicationContext(), "A playlist with the name "+ playListName + " already exists", Toast.LENGTH_SHORT).show();
                            exists = true;
                            break;
                        }
                    }

                    if (exists){

                    }
                    else{
                        boolean created = playListDatabase.addPlaylistName(playListName);
                        if (created){
                            Toast.makeText(getApplicationContext(), "Playlist " +playListName + " Created successfully", Toast.LENGTH_SHORT).show();
                            createPlalistDialogue.dismiss();
                            disPlayPlaylistNames();
                            edtEnterPlayListName.setText("");
                        }
                    }

                }
            }
        });


        cancelProcessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createPlalistDialogue.dismiss();
            }
        });


    }

    public void initAdapter(){
        playListNamesAdapter = new PlayListNamesAdapter(playListNamesList,this, checkPath(cPath));
        b.playListNamesRec.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        b.playListNamesRec.setLayoutManager(linearLayoutManager);
        b.playListNamesRec.setAdapter(playListNamesAdapter);
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
    public void setBitmap(String path){
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(path);
        byte[] albumCover = mediaMetadataRetriever.getEmbeddedPicture();

        if (albumCover == null){
            b.poMetaImage.setImageResource(R.drawable.my_display_disk);
        }
        else{
            Bitmap songCoverArt = BitmapFactory.decodeByteArray(albumCover, 0, albumCover.length);
            b.poMetaImage.setImageBitmap(songCoverArt);
        }

    }


    public String checkPath(String path){
        if (path != null){
            return path;
        }

        else{
            return "null";
        }

    }

    public void disPlayPlaylistNames(){
        playListNamesList.clear();
        Cursor allPlaylistCursor = playListDatabase.getPlayListNames();
        while (allPlaylistCursor.moveToNext()){
            String playListName = allPlaylistCursor.getString(1);
            playListNamesList.add(playListName);
        }
        Collections.reverse(playListNamesList);
        playListNamesAdapter.notifyDataSetChanged();
        allPlaylistCursor.close();

    }

    public class  CreatePlalistDialogue extends Dialog{

        public CreatePlalistDialogue(@NonNull Context context) {
            super(context);

            WindowManager.LayoutParams params = getWindow().getAttributes();
            getWindow().setAttributes(params);
            //params.gravity = Gravity.BOTTOM;
            setTitle(null);
            setCancelable(true);
            setOnCancelListener(null);
            View view = LayoutInflater.from(context).inflate(R.layout.create_new_playlist_dialogue, null);
            setContentView(view);
        }
    }
}