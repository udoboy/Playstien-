package com.example.dfimusic;

import static java.lang.System.in;
import static java.lang.System.out;
import static java.lang.System.setOut;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.dfimusic.Adapters.SongsListAdapter;
import com.example.dfimusic.LocalDAtabases.PlayMode;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.Models.MyMediaPlayer;
import com.example.dfimusic.databinding.ActivityDetailsBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {
    ActivityDetailsBinding b;
    ArrayList<AudioModel> songsList;
    SongsListAdapter songsListAdapter;
    ManagePermissions permissions;

    @Override
    protected void onDestroy() {
        b.c1.setOnClickListener(null);
        b.c2.setOnClickListener(null);
        b.c3.setOnClickListener(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 7:
                if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                else{
                    AlertDialog.Builder alertdialog = new AlertDialog.Builder(getApplicationContext());
                    alertdialog.setMessage("PlayStein requires the Storage permission, please enable in the settings");
                    alertdialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    return;
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        getSupportActionBar().hide();
        songsList = new ArrayList<>();
        songsListAdapter = new SongsListAdapter(songsList, getApplicationContext());
        permissions = new ManagePermissions(this);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q){
            if (!permissions.checkWriteExternalPermission()){
                permissions.requestWritePermission();
            }
        }


        if (permissions.checkReadExternalPermission()){
            if(Hub.getSongsModel() ==  null || Hub.getShuffledSong() == null){
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
                        songsList.add(songData);
                    }
                }

                if (songsList.size() == 0){
                    //write your code here
                }
                Collections.reverse(songsList);
                songsListAdapter.notifyDataSetChanged();
                songsListAdapter.shuffleSongs();
                songsListAdapter.setOrderedList();
                cursor.close();

            }



            PlayMode playMode = new PlayMode(getApplicationContext());
            Cursor modes = playMode.getModes();
            if (modes.moveToFirst()){

            }
            else{
                MyMediaPlayer.currentIndex = 0;
                boolean insert =  playMode.createMode("ordered");
                if (insert){

                }
                else{
                    Toast.makeText(getApplicationContext(), "There was a problem initializing your play modes", Toast.LENGTH_SHORT).show();
                }
            }
            modes.close();
            playMode.close();

            /** the code below will check if the user has already set a sort order for the lists if not then it is a new user and we need to
             * set the default sort order as Date added descending for allSongs fragment and Alphabetical descending for other fragments **/
            if (getSharedPreferences("all_songs_sort", MODE_PRIVATE).getString("all_songs_sort", "none").equals("none")){
                getSharedPreferences("all_songs_sort", MODE_PRIVATE).edit().putString("all_songs_sort", "Date added descending").apply();
                getSharedPreferences("artists_sort", MODE_PRIVATE ).edit().putString("artists_sort", "Alphabetical descending").apply();
                getSharedPreferences("albums_sort", MODE_PRIVATE ).edit().putString("albums_sort", "Alphabetical descending").apply();
                getSharedPreferences("genre_sort", MODE_PRIVATE ).edit().putString("genre_sort", "Alphabetical descending").apply();
            }


        }
        else{
            out.println("No read external storage permission");
        }


        b.c1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.bufferProgress.setVisibility(View.VISIBLE);
                startActivity(new Intent(getApplicationContext(), SongOptionsActivity.class));
            }
        });

        b.c2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.bufferProgress.setVisibility(View.VISIBLE);
              Intent intent = new Intent(getApplicationContext(), FavouritesListActivity.class);
              startActivity(intent);
            }
        });

        b.c3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.bufferProgress.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), PlayListOptionsActivity.class);
                startActivity(intent);
            }
        });
    }



    @Override
    protected void onPostResume() {
        b.bufferProgress.setVisibility(View.GONE);
        super.onPostResume();
    }
}