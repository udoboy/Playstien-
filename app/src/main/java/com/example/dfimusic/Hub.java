package com.example.dfimusic;




import static java.lang.System.out;

import android.app.PendingIntent;
import android.content.Context;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.media.MediaMetadataRetriever;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.media.session.MediaButtonReceiver;

import com.example.dfimusic.LocalDAtabases.PlayMode;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.Models.MyMediaPlayer;


import java.util.HashMap;
import java.util.List;

public class Hub extends ViewModel {

    private static final String TAG = "tag";
    public static List<AudioModel> songsModel;
    public static List<AudioModel> shuffledSong;
    public static Context context;
    private static MutableLiveData<HashMap<String, String>> currentHashData;
    private static AudioModel currentSong;
    public MutableLiveData<Integer> sortChangeInteger;


    public Hub(Context context) {
        this.context = context;

    }

    public Hub() {
    }

    public static MutableLiveData<HashMap<String, String>> getCurrentHashData(){
        if (currentHashData== null){
            currentHashData = new MutableLiveData<>();
        }
        return currentHashData;

    }




    public MutableLiveData<Integer> getSortChangeInteger(){
        if (sortChangeInteger == null){
            sortChangeInteger = new MutableLiveData<>();
        }
        return sortChangeInteger;
    }

    public static List<AudioModel> getShuffledSong() {
        return shuffledSong;
    }

    public static void setShuffledSong(List<AudioModel> shuffledSong) {
        Hub.shuffledSong = shuffledSong;
    }

    public static void setSongsModel(List<AudioModel> songsModel) {
        Hub.songsModel = songsModel;
    }

    public static List<AudioModel> getSongsModel() {
        return songsModel;
    }



    public static AudioModel getCurrentSong(){
        String currentMode = getCurrentMode();
        if(MyMediaPlayer.currentIndex ==-1){
            out.println("Hub: current index is -1");
        }
        else{
            if (currentMode.equals("ordered") || currentMode.equals("repeat_one") || currentMode.equals("repeat_all")){
                if (MyMediaPlayer.currentIndex >= getSongsModel().size()){
                    if (currentMode.equals("repeat_all")){
                        MyMediaPlayer.currentIndex = 0;
                    }
                    else{
                        MyMediaPlayer.currentIndex = getSongsModel().size()-1;
                    }

                    out.println(MyMediaPlayer.currentIndex);
                    currentSong = getSongsModel().get(MyMediaPlayer.currentIndex);
                }
                else{
                    currentSong = getSongsModel().get(MyMediaPlayer.currentIndex);
                }

            }

            else if (currentMode.equals("shuffle")){
                if (MyMediaPlayer.currentIndex >= getShuffledSong().size()){
                    MyMediaPlayer.currentIndex = getSongsModel().size()-1;
                    currentSong = getShuffledSong().get(MyMediaPlayer.currentIndex);
                }
                else{
                    currentSong = getShuffledSong().get(MyMediaPlayer.currentIndex);
                }

            }
        }


        return currentSong;
    }

    public static void setCurrentSong(AudioModel currentAudioModel){
        currentSong = currentAudioModel;
    }


    public static void playNextSong(){
        String currentMode= getCurrentMode();
        if (currentMode.equals("ordered")){
            MyMediaPlayer.getInstance().setLooping(false);
            if (MyMediaPlayer.currentIndex== songsModel.size()-1){
                Toast.makeText(context, "PlayStein: Max length reached", Toast.LENGTH_SHORT).show();
                try {
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE).send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
                return;
            }
            else{
                try {
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT).send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }
        else if (currentMode.equals("shuffle")){
            MyMediaPlayer.getInstance().setLooping(false);
            if (MyMediaPlayer.currentIndex== songsModel.size()-1){
                MyMediaPlayer.currentIndex =0;
                MyMediaPlayer.getInstance().reset();
            }
            else{
                try {
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT).send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }

        }
        else if (currentMode.equals("repeat_one")){
            MyMediaPlayer.getInstance().seekTo(0);
            MyMediaPlayer.getInstance().setLooping(true);


        }
        else if (currentMode.equals("repeat_all")){
            MyMediaPlayer.getInstance().setLooping(false);
            if (MyMediaPlayer.currentIndex== songsModel.size()-1){
                MyMediaPlayer.currentIndex = -1;
                try {
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT).send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
            else{
                try {
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT).send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public static void playPreviousSong(){
        String currentMode = getCurrentMode();
        if (MyMediaPlayer.currentIndex ==0){
           if (currentMode.equals("repeat_all")){
               MyMediaPlayer.currentIndex = getSongsModel().size();
               try {
                   MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).send();
               } catch (PendingIntent.CanceledException e) {
                   e.printStackTrace();
               }
           }
           else if (currentMode.equals("shuffle")){
               MyMediaPlayer.currentIndex = getShuffledSong().size();
               try {
                   MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).send();
               } catch (PendingIntent.CanceledException e) {
                   e.printStackTrace();
               }
           }
           else{
               Toast.makeText(context, "PlayStein: Minimum length reached, cannot perform operation", Toast.LENGTH_SHORT).show();
               return;
           }

        }
        else{
            try {
                MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS).send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }



        }

    }

    public static String getCurrentMode(){
        String mode = "";
        PlayMode playMode = new PlayMode(context);
        Cursor cursor = playMode.getModes();
        while (cursor.moveToNext()){
            mode = cursor.getString(1);
        }
        cursor.close();
        playMode.close();

        return mode;
    }

    public static MediaMetadataCompat getMetadata(){
        AudioModel currentSong = getCurrentSong();
        MediaMetadataCompat.Builder builder= new MediaMetadataCompat.Builder();
        builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong.getArtist());
        builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.getTitle());
        builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, Long.parseLong(currentSong.getDuration()));
        builder.putString("songPath", getCurrentSong().getPath());
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getCurrentBitmap() );


        HashMap<String, String> rawHashData= new HashMap<>();
        rawHashData.put(MediaMetadataCompat.METADATA_KEY_ARTIST, currentSong.getArtist());
        rawHashData.put(MediaMetadataCompat.METADATA_KEY_TITLE, currentSong.getTitle());
        rawHashData.put(MediaMetadataCompat.METADATA_KEY_DURATION, currentSong.getDuration());
        rawHashData.put("songPath", currentSong.getPath());
        getCurrentHashData().setValue(rawHashData);
        return  builder.build();
    }

    public static Bitmap getCurrentBitmap(){
        Bitmap bitmap;
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(getCurrentSong().getPath());
        byte[] currentByte = mediaMetadataRetriever.getEmbeddedPicture();
        if (currentByte != null){
            bitmap = BitmapFactory.decodeByteArray(currentByte,0, currentByte.length);
        }
        else{
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.iconme);
        }

        return bitmap;
    }
}
