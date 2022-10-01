package com.example.dfimusic.Services;

import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SEEK_TO;

import static java.lang.System.out;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.media.session.MediaButtonReceiver;

import com.example.dfimusic.Hub;
import com.example.dfimusic.MediaNotificationManager;
import com.example.dfimusic.Models.MyMediaPlayer;

import java.util.concurrent.TimeUnit;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnInfoListener, AudioManager.OnAudioFocusChangeListener{
    public MediaPlayer mediaPlayer;
    public static final String TAG  = "MediaSessionService";
    public static final int NOTIFICATION_ID = 888;
    public MediaNotificationManager mMediaNotificationManager;
    private MediaSessionCompat mediaSession;
    AudioManager mAudioManager;
    AudioAttributes audioAttributes;
    AudioFocusRequest focusRequest;
    Handler audioFocusHandler = new Handler();
    int number = 0;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Hub hub = new Hub(this);
        super.onCreate();
        mAudioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MyMediaPlayer.getInstance();
        mMediaNotificationManager = new MediaNotificationManager(this);
        mediaSession = MyMediaPlayer.getMyMediaSessionCompat(this);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS|
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);

       audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
           focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                   .setAudioAttributes(audioAttributes)
                   .setAcceptsDelayedFocusGain(true)
                   .setFocusGain(AudioManager.AUDIOFOCUS_GAIN)
                   .setOnAudioFocusChangeListener(this, audioFocusHandler)
                   .setWillPauseWhenDucked(true).build();
       }

        mediaSession.setCallback(new MediaSessionCompat.Callback(){
            @Override
            public void onPlay() {
                //super.onPlay();
                out.println("onplay");
                mediaPlayer.start();
            }

            @Override
            public void onPause() {
                //super.onPause();
                out.println("onpause");
                mediaPlayer.pause();
            }



            @Override
            public void onSeekTo(long pos) {
                //super.onSeekTo(pos);
                mediaPlayer.seekTo((int) pos);
                MyMediaPlayer.getMyMediaSessionCompat(MusicService.this).setPlaybackState(getState());
            }

            @Override
            public void onSkipToNext() {
                out.println("skip to next");
                super.onSkipToNext();
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                out.println(mediaButtonEvent.getAction());
                number ++;
                if (number == 2){
                    out.println("media button event");
                  KeyEvent keyEvent = (KeyEvent) mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                    //out.println(mediaButtonEvent.getAction());
                    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PREVIOUS ){
                        Hub.playPreviousSong();
                    }
                    else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT){
                        Hub.playNextSong();

                    }
                    else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY){
                        try {
                            MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_PLAY).send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE){
                        try {
                            MediaButtonReceiver.buildMediaButtonPendingIntent(MusicService.this, PlaybackStateCompat.ACTION_PAUSE).send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                    number =0;
                }

               // return super.onMediaButtonEvent(mediaButtonEvent);
                return true;
            }
        });

    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        requestAudioFocus();
        mediaPlayer.setOnCompletionListener(this);
        out.println("service started");
        if ("android.intent.action.MEDIA_BUTTON".equals(intent.getAction())){
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get("android.intent.extra.KEY_EVENT");
            if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PAUSE){
                mediaPlayer.pause();
                prepareForMusic();
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY){
                mediaPlayer.start();

                /** the code below is used to check if the mediaPlayer has started playing, if not then we need to reset the datasource
                 * and start the player again**/
                if (!mediaPlayer.isPlaying()){
                 initMediaPlayer();
                }
                prepareForMusic();
            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT){
                out.println("key code next");
                MyMediaPlayer.currentIndex +=1;
                initMediaPlayer();
                prepareForMusic();

            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PREVIOUS){
                if (MyMediaPlayer.getInstance().getCurrentPosition() >= 4000){
                    MyMediaPlayer.getInstance().seekTo(0);
                    prepareForMusic();
                }
                else{
                    MyMediaPlayer.currentIndex -=1;
                    initMediaPlayer();
                    prepareForMusic();
                }

            }
            else if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_STOP){
                stopSelf();
            }

        }

        else{
          initMediaPlayer();
            prepareForMusic();

        }

        return START_STICKY;
    }


    public void prepareForMusic(){
        mediaSession.setActive(true);
        mediaSession.setMetadata(Hub.getMetadata());
        mediaSession.setPlaybackState(getState());
        Notification notification = mMediaNotificationManager.getNotification(Hub.getMetadata(), getState(), mediaSession.getSessionToken());
        startForeground(NOTIFICATION_ID, notification);
        getSharedPreferences("playingStatus", MODE_PRIVATE).edit().putString("playingPath", Hub.getMetadata().getString("songPath")).apply();
        getSharedPreferences("currentTitle", MODE_PRIVATE).edit().putString("currentTitle", Hub.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_TITLE)).apply();
        getSharedPreferences("currentArtist", MODE_PRIVATE).edit().putString("currentArtist", Hub.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_ARTIST)).apply();

    }
    public void initMediaPlayer(){
        if (requestAudioFocus()){
            try{
                mediaPlayer.reset();
                mediaPlayer.setDataSource(Hub.getMetadata().getString("songPath"));
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void abandonAudioFocus(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            mAudioManager.abandonAudioFocusRequest(focusRequest);
        }
        else{
            mAudioManager.abandonAudioFocus(this);
        }
    }
    public boolean requestAudioFocus(){
        int result;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
           result = mAudioManager.requestAudioFocus(focusRequest);
        }
        else{
           result= mAudioManager.requestAudioFocus(this,AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            return true;
        }
        else{
            return false;
        }

    }

    public PlaybackStateCompat getState(){
        mediaPlayer = MyMediaPlayer.getInstance();
        long actions = mediaPlayer.isPlaying()? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY;
        int state = mediaPlayer.isPlaying()? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
        final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
        stateBuilder.setActions(actions | ACTION_SEEK_TO);
        stateBuilder.setState(state, mediaPlayer.getCurrentPosition(), 1.0f, SystemClock.elapsedRealtime());
        return  stateBuilder.build();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        out.println("music complete");
        if (convert(mediaPlayer.getCurrentPosition() +"").equals(convert(String.valueOf(Hub.getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION))))){
            Hub.playNextSong();
        }

    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        abandonAudioFocus();
        super.onDestroy();
    }

    public String convert(String duration){
        Long millis = Long.parseLong(duration);

        return String.format(("%02d:%02d"), TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
    }

    @Override
    public void onAudioFocusChange(int i) {
        switch (i){
            case AudioManager.AUDIOFOCUS_GAIN:
                if(MyMediaPlayer.getInstance() == null){
                    initMediaPlayer();
                }
                else if (mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
                Log.d(TAG, "onAudioFocusChange: audio focus gained");
                break;
            case  AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                }
                prepareForMusic();
              //  MyMediaPlayer.getMyMediaSessionCompat(this).setPlaybackState(getState());
                abandonAudioFocus();

                Log.d(TAG, "onAudioFocusChange: audio focus lost");
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                abandonAudioFocus();
                if (mediaPlayer.isPlaying()){
                    try {
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this,  PlaybackStateCompat.ACTION_PAUSE).send(1);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
                MyMediaPlayer.getMyMediaSessionCompat(this).setPlaybackState(getState());
                Log.d(TAG, "onAudioFocusChange: audio focus los transient");
                break;

        }
    }





}
