package com.example.dfimusic;

import static java.lang.System.out;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.media.session.MediaButtonReceiver;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.media.session.PlaybackStateCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dfimusic.Adapters.DisplayListAdapter;
import com.example.dfimusic.Adapters.MapPositionAdapter;
import com.example.dfimusic.LocalDAtabases.Favourites;
import com.example.dfimusic.LocalDAtabases.MapPlacesDatabase;
import com.example.dfimusic.LocalDAtabases.PlayMode;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.Models.MapModel;
import com.example.dfimusic.Models.MyMediaPlayer;
import com.example.dfimusic.Services.MusicService;
import com.example.dfimusic.databinding.ActivityMusicPlayerBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



public class MusicPlayerActivity extends AppCompatActivity implements MapAdapterClicks {
    public static String TAG = "MusicPlayerActivity";
    ActivityMusicPlayerBinding b;
    MediaPlayer mediaPlayer = MyMediaPlayer.getInstance();
    AudioModel currentSong;
    int orderType;
    List<MapModel> mappedList;
    MapModel mapModel;
    boolean hasHighDuration;
    RotateAnimation rotateAnimation;
    MediaMetadataRetriever mediaMetadataRetriever;
    String currentList;
    GetCursorData getCursorData;
    List<AudioModel> playingList;

    InputMethodManager imm;
    DisplayListAdapter displayListAdapter;
    LinearLayoutManager linearLayoutManager;
    public static byte[] embeddedByte;
    List<String> favouritesList;
    Favourites favourites;
    MapPositionAdapter mapPositionAdapter;
    Hub tHub;
    Hub hub;
    Runnable startRotatingImage;
    int source;
    MpUiRunnable mpUiRunnable;
    MyDialogCreator myDialogCreator;
    static Handler uiHandler;
    //Runnable uiRunnable;
    Thread thread;
    Runnable rotatingImgRun;

    /**This interface methods are called when on actions from the MapPositionAdapter **/
    @Override
    public void onItemClicked(String position) {
        mediaPlayer.seekTo(Integer.parseInt(position));
        myDialogCreator.dismiss();
    }

    @Override
    public void onDeleteClicked() {

    }

    public static class MpUiRunnable implements Runnable{
        ImageView imgOrder, playPause;
        SeekBar seekMusic;
        MediaPlayer mediaPlayer;
        TextView progressTime;
        String order;

        public MpUiRunnable(ImageView imgOrder, ImageView playPause, SeekBar seekBar, MediaPlayer mediaPlayer, TextView progressTime, String order ) {
            this.imgOrder = imgOrder;
            this.playPause = playPause;
            this.seekMusic = seekBar;
            this.mediaPlayer = mediaPlayer;
            this.progressTime = progressTime;
            this.order = order;
        }

        @Override
        public void run() {
            if (mediaPlayer != null){
                seekMusic.setProgress(mediaPlayer.getCurrentPosition());
                progressTime.setText(convert(mediaPlayer.getCurrentPosition() + ""));


                if (mediaPlayer.isPlaying()){
                    playPause.setImageResource(R.drawable.ic_music_playing);

                }
                else{
                    playPause.setImageResource(R.drawable.ic_pause);
                }

                if (order.equals("ordered")){
                    imgOrder.setImageResource(R.drawable.ic_order_music_main);
                }
                else if(order.equals("shuffle")){
                    imgOrder.setImageResource(R.drawable.ic_shuffle);
                }
                else if (order.equals("repeat_one")){
                    imgOrder.setImageResource(R.drawable.ic_repeat_one);
                }
                else if (order.equals("repeat_all")){
                    imgOrder.setImageResource(R.drawable.ic_repeat_all);
                }

            }
            uiHandler.post(this);
        }

    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: activity destroyed");
        uiHandler.removeCallbacksAndMessages(null);
        uiHandler.removeCallbacks(mpUiRunnable);
        b.getRoot().removeCallbacks(startRotatingImage);
        imm = null;
        startRotatingImage = null;
        hub = null;
        mappedList = null;
        tHub = null;
        rotateAnimation = null;
        if(thread != null){
            thread.interrupt();
        }
        b.getRoot().removeCallbacks(mpUiRunnable);
        b.getRoot().removeCallbacks(rotatingImgRun);
        getCursorData = null;
        displayListAdapter = null;
        embeddedByte = null;
        b.dPlayingSongsRec.setAdapter(null);
        b.mpPlaylist.setOnClickListener(null);
        b.playPause.setOnClickListener(null);
        b.mpFavourite.setOnClickListener(null);
        b.seekMusic.setOnSeekBarChangeListener(null);
        b.searchSeek.setOnClickListener(null);
        b.imgOrder.setOnClickListener(null);
        b.imgNext.setOnClickListener(null);
        b.imgPrevious.setOnClickListener(null);
        b.mpPlaylist.setOnClickListener(null);
        b.mpplayNext.setOnClickListener(null);
        b.songCoverImage.setOnClickListener(null);
        mapPositionAdapter = null;
        b.titleTv.setOnClickListener(null);
        b.txtArtist.setOnClickListener(null);
        playingList = null;
        favourites.close();
        favourites = null;
        favouritesList = null;
        myDialogCreator = null;

        //b = null;
        finish();

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityMusicPlayerBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        getSupportActionBar().hide();
        hub = new ViewModelProvider(this).get(Hub.class);
        tHub= new Hub(getApplicationContext());
        source = getIntent().getIntExtra("source", -1);



        //arraylist initializations
        playingList = new ArrayList<>();
        favouritesList = new ArrayList<>();
        favourites = new Favourites(getApplicationContext());



        displayListAdapter = new DisplayListAdapter(playingList, getApplicationContext());
        b.titleTv.setSelected(true);

        switch (getCurrentMode()){
            case "ordered":
                setOrder(1);
                currentList = "mainList";
                break;
            case "shuffle":
                setOrder(2);
                currentList = "shuffledList";
                break;
            case "repeat_one":
                setOrder(3);
                currentList = "mainList";
                break;
            case "repeat_all":
                setOrder(4);
                currentList = "mainList";
                break;
            default:
                setOrder(1);
                currentList = "mainList";
                break;
        }

        getMusicDetails(1, 2);

        uiHandler = new Handler();
        mpUiRunnable = new MpUiRunnable(b.imgOrder, b.playPause, b.seekMusic, mediaPlayer, b.progressTime, getOrder());
        b.getRoot().post(mpUiRunnable);

        Observer<HashMap<String, String>> currentHashObserver = new Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> stringStringHashMap) {
                getMusicDetails(2, 3);
                displayListAdapter.notifyDataSetChanged();
            }
        };
        Hub.getCurrentHashData().observe(this, currentHashObserver);

        b.dPlayingSongsRec.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        b.dPlayingSongsRec.setAdapter(displayListAdapter);
        b.dPlayingSongsRec.setLayoutManager(linearLayoutManager);

        //On event listeners
        b.showPlayingList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPlayingList();
                TranslateAnimation animation = new TranslateAnimation(0,0,1000,0);
                animation.setDuration(500);
                b.songListRec.setVisibility(View.VISIBLE);
                b.songListRec.setAnimation(animation);
                linearLayoutManager.scrollToPositionWithOffset(displayListAdapter.getIndex(currentSong.getPath()), 180);
            }
        });

        b.mpplayNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentList().add(MyMediaPlayer.currentIndex+1 , currentSong);
                Toast.makeText(getApplicationContext(), "Added to queue", Toast.LENGTH_SHORT).show();

            }
        });
        b.mpPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PlayListOptionsActivity.class);
                intent.putExtra("currentPath", currentSong.getPath());
                intent.putExtra("songName", currentSong.getTitle());
                startActivity(intent);
            }
        });

        b.closeList.setOnClickListener(view -> closeList());

        b.imgPrevious.setOnClickListener(view -> playPreviousSong());

        b.imgNext.setOnClickListener(view -> playNextSong());

        b.playPause.setOnClickListener(view -> pausePlay());

        b.searchSeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 hasHighDuration = false;
                MapPlacesDatabase mapPlacesDatabase = new MapPlacesDatabase(getApplicationContext());
                getCursorData = new GetCursorData(getApplicationContext(), "map", mapPlacesDatabase, currentSong.getPath());
                mappedList = new ArrayList<>();
                mapPositionAdapter = new MapPositionAdapter(mappedList, getApplicationContext(), MusicPlayerActivity.this);
                Cursor mappedPlaces = getCursorData.loadInBackground();
                while(mappedPlaces.moveToNext()){
                   mapModel = new MapModel(mappedPlaces.getString(3), mappedPlaces.getString(2), mappedPlaces.getString(1), mappedPlaces.getInt(0));
                   mappedList.add(mapModel);
                }
                mapPositionAdapter.notifyDataSetChanged();
                mappedPlaces.close();
                mapPlacesDatabase.close();

                myDialogCreator = new MyDialogCreator(MusicPlayerActivity.this,
                        R.layout.seeksearch_item, true, null);

                EditText s1 = myDialogCreator.findViewById(R.id.s1);
                EditText s2 = myDialogCreator.findViewById(R.id.s2);
                EditText s3 = myDialogCreator.findViewById(R.id.s3);
                EditText s4=  myDialogCreator.findViewById(R.id.s4);
                EditText se = myDialogCreator.findViewById(R.id.se);
                if (TimeUnit.MILLISECONDS.toHours(Long.parseLong(currentSong.getDuration())) >=1){
                    se.setVisibility(View.VISIBLE);
                    hasHighDuration = true;
                }
                else{
                    se.setVisibility(View.GONE);
                    hasHighDuration = false;
                }

                StringBuilder seekValue = new StringBuilder(hasHighDuration? 5:4);
                RecyclerView mappedRec = myDialogCreator.findViewById(R.id.mappedRec);
                mappedRec.setHasFixedSize(true);
                mappedRec.setAdapter(mapPositionAdapter);
                mappedRec.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                Button btnGo = myDialogCreator.findViewById(R.id.btnGo);

                se.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if(hasHighDuration){
                            if(editable.toString().isEmpty()){
                                seekValue.deleteCharAt(0);
                            }
                            else{
                                seekValue.append(editable);
                                s1.requestFocus();
                            }
                        }
                    }
                });


                s1.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (hasHighDuration){
                            if (se.getText().toString().isEmpty()){
                                se.setText("0");
                            }
                            if (editable.toString().isEmpty()){
                                se.requestFocus();
                                seekValue.deleteCharAt(1);
                            }
                            else{
                                seekValue.append(editable);
                                s2.requestFocus();
                            }

                        }
                        else{
                            if (editable.toString().isEmpty()){
                                seekValue.deleteCharAt(0);
                            }
                            else{
                                seekValue.append(editable);
                                s2.requestFocus();
                            }
                        }


                    }
                });
                s2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (hasHighDuration){
                            if (editable.toString().isEmpty()){
                                seekValue.deleteCharAt(2);
                                s1.requestFocus();
                            }
                            else{
                                s3.requestFocus();
                                seekValue.append(editable);
                            }
                        }
                        else {
                            if (editable.toString().isEmpty()){
                                s1.requestFocus();
                                seekValue.deleteCharAt(1);
                            }
                            else{
                                s3.requestFocus();
                                seekValue.append(editable);
                            }
                        }



                    }
                });
                s3.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (hasHighDuration){
                            if (editable.toString().isEmpty()){
                                s2.requestFocus();
                                seekValue.deleteCharAt(3);
                            }
                            else{
                                s4.requestFocus();
                                seekValue.append(editable);
                            }
                        }
                        else{
                            if (editable.toString().isEmpty()){
                                s2.requestFocus();
                                seekValue.deleteCharAt(2);
                            }
                            else{
                                s4.requestFocus();
                                seekValue.append(editable);
                            }
                        }




                    }
                });
                s4.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (hasHighDuration){
                            if (editable.toString().isEmpty()){
                                s3.requestFocus();
                                seekValue.deleteCharAt(2);
                            }
                            else {
                                seekValue.append(editable);
                            }
                        }
                        else{
                            if (editable.toString().isEmpty()){
                                s3.requestFocus();
                                seekValue.deleteCharAt(3);

                            }
                            else{
                                seekValue.append(editable);

                            }
                        }

                    }
                });


                btnGo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        out.println("seek value"+seekValue);
                        if (hasHighDuration){
                            revConvertHigh(seekValue.toString());
                            myDialogCreator.dismiss();
                        }
                        else{
                            revConvert(seekValue.toString());
                            myDialogCreator.dismiss();
                        }

                    }
                });

                myDialogCreator.show();

            }
        });

        b.imgOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getOrder().equals("ordered")){
                    setOrder(2);
                    initMode(getOrder());
                    MyMediaPlayer.currentIndex = Hub.getShuffledSong().indexOf(currentSong);
                    currentList = "shuffledList";

                }
                else if (getOrder().equals("shuffle")){
                    setOrder(3);
                    initMode(getOrder());
                    MyMediaPlayer.currentIndex = Hub.getSongsModel().indexOf(currentSong);
                    currentList = "mainList";
                }
                else if (getOrder().equals("repeat_one")){
                    setOrder(4);
                    initMode(getOrder());
                    MyMediaPlayer.currentIndex = Hub.getSongsModel().indexOf(currentSong);
                    currentList = "mainList";

                }
                else {
                    setOrder(1);
                    initMode(getOrder());
                    MyMediaPlayer.currentIndex = Hub.getSongsModel().indexOf(currentSong);
                    currentList = "mainList";

                }
                mpUiRunnable = new MpUiRunnable(b.imgOrder, b.playPause, b.seekMusic, mediaPlayer, b.progressTime, getOrder());
               // mpUiRunnable.run();
                b.getRoot().post(mpUiRunnable);
            }
        });

        b.mpFavourite.setOnClickListener(view -> makeFavourite());

        b.seekMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mediaPlayer != null && b){
                    mediaPlayer.seekTo(i);
                    MyMediaPlayer.getMyMediaSessionCompat(getApplicationContext()).setPlaybackState(new MusicService().getState());

                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        b.titleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                prepareDialog(String.valueOf(mediaPlayer.getCurrentPosition()));
            }
        });

        b.txtArtist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               prepareDialog(String.valueOf(mediaPlayer.getCurrentPosition()));
            }
        });

       playMusic(source);
       rotatingImgRun = new Runnable() {
           @Override
           public void run() {
               if (mediaPlayer.isPlaying()){
                   b.songCoverImage.setAnimation(rotateAnimation);
                   rotateAnimation.start();
               }
               else{
                   b.songCoverImage.setAnimation(null);
               }

           }
       };

       startRotatingImage = new Runnable() {
           @Override
           public void run() {
               rotateAnimation = new RotateAnimation(0, 360,
                       Animation.RELATIVE_TO_SELF,
                       0.5f,
                       Animation.RELATIVE_TO_SELF,
                       0.5f);
               rotateAnimation.setDuration(40000);
               rotateAnimation.setRepeatCount(Animation.INFINITE);
               b.getRoot().post(rotatingImgRun);
           }
       };



    }

    public void prepareDialog(String currentPosition){
        myDialogCreator = new MyDialogCreator(MusicPlayerActivity.this,R.layout.map_position_layout,true, null  );
        TextView txtSaveMap = myDialogCreator.findViewById(R.id.txtSaveMapPosition);
        EditText edtSongName = myDialogCreator.findViewById(R.id.edtMapName);
        Button btnSaveMap= myDialogCreator.findViewById(R.id.btnSaveMapPosition);
        txtSaveMap.setSelected(true);
        txtSaveMap.setText("Save position "+ convert(String.valueOf(mediaPlayer.getCurrentPosition())) +  " on " + currentSong.getTitle());
        edtSongName.setText(convert(String.valueOf(mediaPlayer.getCurrentPosition())));
        edtSongName.selectAll();
        edtSongName.requestFocus();
        myDialogCreator.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        myDialogCreator.show();


        btnSaveMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveMapPosition(currentSong.getPath(), currentPosition,edtSongName.getText().toString());
                myDialogCreator.dismiss();
            }
        });
    }

    public void saveMapPosition(String path, String position, String mapName){
        MapPlacesDatabase mapPlacesDatabase = new MapPlacesDatabase(getApplicationContext());
        boolean saved = mapPlacesDatabase.setMapPosition(path, position, mapName);
        if (saved){
            Toast.makeText(getApplicationContext(), "Position saved", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "An error occurred please try again", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    protected void onStart() {
        super.onStart();
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 10;
            final int halfWidth = width / 10;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public Bitmap decodeFromBitmap(Resources res, int reqWidth, int reqHeight, byte[] backgroundByte){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
       // BitmapFactory.decodeByteArray(backgroundByte,0, backgroundByte.length, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return  BitmapFactory.decodeByteArray(backgroundByte,0, backgroundByte.length, options);

    }



    public void checkFavourite(){
        favouritesList.clear();
        favourites = new Favourites(getApplicationContext());
        GetCursorData getCursorData = new GetCursorData(getApplicationContext(), "favourites", favourites);
        Cursor allFavourites = getCursorData.loadInBackground();
        if (allFavourites != null){
            while (allFavourites.moveToNext()){
                String cur = allFavourites.getString(1);
                favouritesList.add(cur);
            }
        }


        if (favouritesList.contains(currentSong.getPath())){
            b.mpFavourite.setImageResource(R.drawable.ic_fav_red_filled);
            b.mpFavourite.setTag("fav");
        }
        else{
            b.mpFavourite.setImageResource(R.drawable.ic_baseline_favorite_border_24);
            b.mpFavourite.setTag("noFav");

        }

        allFavourites.close();
        favourites.close();
        getCursorData.cancelLoadInBackground();


    }

    public void makeFavourite(){

        if(b.mpFavourite.getTag().equals("fav")){
            boolean deleted = favourites.removeFavourite(currentSong.getPath());
            if (deleted){
                //Write your code here
            }
            else{
                //Write your code here
            }
        }
        else if(b.mpFavourite.getTag().equals("noFav")){
            boolean inserted = favourites.addFavourite(currentSong.getPath());
            if (inserted){
                Toast.makeText(getApplicationContext(), "Added to favourites", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }

        }
        favourites.close();
        checkFavourite();
    }

    public List<AudioModel> getCurrentList(){
        String currentMode = Hub.getCurrentMode();
        if (currentMode.equals("shuffle")){
            return Hub.getShuffledSong();
        }
        else{
            return Hub.getSongsModel();
        }

    }

    public void closeList(){
        TranslateAnimation animation = new TranslateAnimation(0,0,0,1000);
        animation.setDuration(500);
        b.songListRec.setAnimation(animation);
        b.songListRec.setVisibility(View.GONE);
    }



    public String getCurrentMode(){
        String mode = "";
        PlayMode playMode = new PlayMode(getApplicationContext());
        getCursorData = new GetCursorData(getApplicationContext(), "playMode", playMode);
        Cursor cursor = getCursorData.loadInBackground();
        while (cursor.moveToNext()){
             mode = cursor.getString(1);
        }
        cursor.close();
        playMode.close();
        getCursorData.cancelLoad();
        getCursorData.cancelLoadInBackground();


        return mode;
    }

    public void prepareBitmap(){
        Log.d(TAG, "prepareBitmap: preparing to display bitmap");
        mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(currentSong.getPath());
        embeddedByte = mediaMetadataRetriever.getEmbeddedPicture();

        if (embeddedByte != null){
            Bitmap bitmap = decodeFromBitmap(getResources(), 512, 512, embeddedByte);
            b.songCoverImage.setImageBitmap(bitmap);
        }
        else{
            b.songCoverImage.setImageResource(R.drawable.my_display_disk);

        }

        setRotation();

    }


    public void setRotation(){
       thread = new Thread(startRotatingImage);
        thread.start();
    }

    public void setOrder(int type){
        if(type ==1){
            orderType = 1;
        }
        else if (type ==2){
            orderType = 2;

        }
        else if (type == 3){
            orderType = 3;

        }

       else if (type == 4){
           orderType = 4;

        }

       else{
          orderType = 0;
        }

    }

    public void initMode(String mode){
        PlayMode playMode = new PlayMode(getApplicationContext());
        getCursorData = new GetCursorData(getApplicationContext(), "playMode", playMode);
        Cursor modes = getCursorData.loadInBackground();
        if(modes.moveToFirst()){
            boolean update =playMode.updateMode(mode);
            if (update){
                Cursor cursor = getCursorData.loadInBackground();
                while (cursor.moveToNext()){
                    Toast.makeText(getApplicationContext(), cursor.getString(1), Toast.LENGTH_SHORT).show();
                }
                cursor.close();
                playMode.close();
                modes.close();
                getCursorData.cancelLoad();
                getCursorData.cancelLoadInBackground();

            }
            else{
                playMode.close();
                modes.close();
                getCursorData.cancelLoad();
                getCursorData.cancelLoadInBackground();
            }

        }
        else{
           boolean insert =  playMode.createMode(mode);
           if (!insert){

           }
           else{

           }
           playMode.close();
           getCursorData.cancelLoad();
           getCursorData.cancelLoadInBackground();
           modes.close();
        }

    }

    public String getOrder(){
        if (orderType ==1){
            return "ordered";
        }
        else if(orderType == 2){
            return "shuffle";
        }
        else if (orderType ==3){
            return "repeat_one";
        }
        else if (orderType == 4){
            return "repeat_all";
        }
        else{
            return null;
        }
    }


    public void playNextSong(){
        b.getRoot().removeCallbacks(rotatingImgRun);
        Hub.playNextSong();
    }

    public void playPreviousSong(){
        b.getRoot().removeCallbacks(rotatingImgRun);
        Hub.playPreviousSong();
    }

    public void pausePlay(){
      if (mediaPlayer.isPlaying()){
          b.playPause.setImageResource(R.drawable.ic_music_playing);
          try {
              MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_PAUSE).send();
          } catch (PendingIntent.CanceledException e) {
              e.printStackTrace();
          }
      }
      else{
         b.playPause.setImageResource(R.drawable.ic_pause);
          try {
              MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(), PlaybackStateCompat.ACTION_PLAY).send();
          } catch (PendingIntent.CanceledException e) {
              e.printStackTrace();
          }
      }

    }

    public void getMusicDetails(int source, int ref){
        String currentMode = getCurrentMode();
        if (currentMode.equals("ordered") || currentMode.equals("repeat_one") || currentMode.equals("repeat_all")){
            currentSong =Hub.getCurrentSong();
            setResourcesWithMusic();
            currentList= "mainList";
        }
        else if (currentMode.equals("shuffle")){
            if (source ==1){
                out.println("called using 1");
                 currentSong = Hub.getShuffledSong().get(Hub.getShuffledSong().indexOf(Hub.getSongsModel().get(MyMediaPlayer.currentIndex)));
                 MyMediaPlayer.currentIndex = Hub.getShuffledSong().indexOf(currentSong);
                setResourcesWithMusic();
                currentList = "shuffledList";
            }
            else{
                 currentSong = Hub.getCurrentSong();
                setResourcesWithMusic();
                currentList = "shuffledList";
            }
        }
        b.seekMusic.setProgress(0);
        b.seekMusic.setMax(Integer.parseInt(currentSong.getDuration()));
        checkFavourite();
    }

    public void getPlayingList(){
        if (currentList !=null){
            if (currentList.equals("mainList")){
                playingList.clear();
                for (AudioModel model: Hub.songsModel){
                    playingList.add(model);
                }
                //Collections.reverse(playingList);
                displayListAdapter.notifyDataSetChanged();
                b.dTxtNumberofSongs.setText(playingList.size() + " Songs");
            }
            else if (currentList.equals("shuffledList")){

                playingList.clear();
                for (AudioModel model: Hub.shuffledSong){
                    playingList.add(model);
                }
                //Collections.reverse(playingList);
                displayListAdapter.notifyDataSetChanged();
                b.dTxtNumberofSongs.setText(playingList.size() +" Songs");

            }
        }

    }

    public void setResourcesWithMusic(){
        b.titleTv.setText(currentSong.getTitle());
        b.txtArtist.setText(currentSong.getArtist());
        b.totalTime.setText(convert(currentSong.getDuration()));
        prepareBitmap();
    }

    public void playMusic(int source){
        if (source ==1){
            Intent playMusicService = new Intent(getApplicationContext(), MusicService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                startForegroundService(playMusicService);
            }
            else{
                startService(playMusicService);
            }
        }


    }

    /** this function will convert the duration in milliseconds to a 00:00 format readable to the user**/
    public static String convert(String duration){
        Long millis = Long.parseLong(duration);
        return String.format(("%02d:%02d"),TimeUnit.MILLISECONDS.toMinutes(millis),
        TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    /** this function will convert the text seekvalue which is the form of 00:00 to milliseconds and make the mediaplayer seek to that position
     * then update the playbackstate so that the notification can also seek to the position**/
    public void revConvert(String seekValue){
        if(seekValue.length() <4){
            Toast.makeText(getApplicationContext(), "Invalid position", Toast.LENGTH_SHORT).show();
        }
        else{
            long minOne = Long.parseLong(seekValue.substring(0,2));
            long minTwo = Long.parseLong(seekValue.substring(2,4));
            long seekPosition = TimeUnit.MINUTES.toMillis(minOne)+ TimeUnit.SECONDS.toMillis(minTwo);
            if (seekPosition > Long.parseLong(currentSong.getDuration())){
                Toast.makeText(getApplicationContext(), "Invalid position", Toast.LENGTH_SHORT).show();
            }
            else{
                mediaPlayer.seekTo((int) seekPosition);
                MyMediaPlayer.getMyMediaSessionCompat(getApplicationContext()).setPlaybackState(new MusicService().getState());
            }

        }
    }

    public void revConvertHigh(String seekValue){
        if(seekValue.length() < 4){
            Toast.makeText(getApplicationContext(), "Invalid position", Toast.LENGTH_SHORT).show();
        }
        else{
            long minOne = Long.parseLong(seekValue.substring(0,3));
            long minTwo = Long.parseLong(seekValue.substring(3,4));
            long seekPosition =TimeUnit.MINUTES.toMillis(minOne) + TimeUnit.SECONDS.toMillis(minTwo);
            if (seekPosition > Long.parseLong(currentSong.getDuration())) {
                Toast.makeText(getApplicationContext(), "Invalid position", Toast.LENGTH_SHORT).show();
            }
            else{
                mediaPlayer.seekTo((int) seekPosition);
                MyMediaPlayer.getMyMediaSessionCompat(getApplicationContext()).setPlaybackState(new MusicService().getState());
            }
        }
    }

}





