package com.example.dfimusic;

import static java.lang.System.out;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.view.View;
import android.widget.ImageView;

import com.example.dfimusic.Adapters.SongsListAdapter;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.Models.MembersMode;
import com.example.dfimusic.Models.MyMediaPlayer;
import com.example.dfimusic.databinding.ActivityTestBinding;

import java.lang.ref.WeakReference;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;




public class TestActivity extends AppCompatActivity {
    ActivityTestBinding b;
    Hub hub;
    static Handler uiHandler;
    static Runnable uiRunnable;
    String currentPath;
    String currentArtist;
    String currentTitle;
    DisplayImgDialog displayImgDialog;
    ArrayList<AudioModel> displayList;
    List<MembersMode> membersModeList;
    MediaPlayer mediaPlayer;
    SongsListAdapter songsListAdapter;
    @Override
    protected void onDestroy() {
        uiHandler.removeCallbacks(uiRunnable);
        uiHandler.removeCallbacksAndMessages(null);
        //uiHandler = null;
       // uiRunnable = null;
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityTestBinding.inflate(getLayoutInflater());
        //setContentView(R.layout.activity_test);
        setContentView(b.getRoot());
        hub = new ViewModelProvider(this).get(Hub.class);
        mediaPlayer = MyMediaPlayer.getInstance();
        displayImgDialog = new DisplayImgDialog(this);
        membersModeList =new ArrayList<>();
        setMembersModel();
        currentPath = getSharedPreferences("playingStatus", MODE_PRIVATE).getString("playingPath", "none");
        currentArtist = getSharedPreferences("currentArtist", MODE_PRIVATE).getString("currentArtist", "none");
        currentTitle = getSharedPreferences("currentTitle", MODE_PRIVATE).getString("currentTitle", "none");


        displayList = new ArrayList<>();
        songsListAdapter = new SongsListAdapter(displayList, getApplicationContext());
        setBitmap(currentPath);
        Observer<HashMap<String, String>> currentHashObserver = new Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> stringObjectHashMap) {
                b.asCurrentSongTitle.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_TITLE));
                b.asCurrentSongArtist.setText(stringObjectHashMap.get(MediaMetadataCompat.METADATA_KEY_ARTIST));
                currentPath = stringObjectHashMap.get("songPath");
                setBitmap(currentPath);
            }
        };
        Hub.getCurrentHashData().observe(this, currentHashObserver);



        b.miniRel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayImgDialog.show();


                try {
                    mediaPlayer.prepare();
                    mediaPlayer.setDataSource(currentPath);
                    mediaPlayer.start();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        b.asPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                out.println("you clicked on the pause play button");
            }
        });




        uiHandler = new Handler();
        MyRunnable myRunnable = new MyRunnable(mediaPlayer, b.asPlayPause);
        b.getRoot().post(myRunnable);

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
            membersModeList.add(membersModel);
        }
        cursor1.close();
    }

}