package com.example.dfimusic;

import static java.lang.System.out;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaDescription;
import android.os.Build;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import com.example.dfimusic.Models.MyMediaPlayer;
import com.example.dfimusic.Services.MusicService;

public class MediaNotificationManager {
    public static final int NOTIFICATION_ID = 412;
    private static final int REQUEST_CODE= 501;

    private final MusicService musicService;

    private final NotificationCompat.Action mPlayAction;
    private final NotificationCompat.Action mPauserAction;
    private final NotificationManager notificationManager;
    private final NotificationCompat.Action mPlayNextAction;
    private final NotificationCompat.Action mPlayPrevAction;
    private final NotificationCompat.Action mCancelAction;
    public static final String TAG = MediaNotificationManager.class.getSimpleName();
    public static final String CHANNEL_ID  = "channel_id";




    @SuppressLint("ServiceCast")
    public MediaNotificationManager(MusicService musicContext){
        musicService = musicContext;
        notificationManager = (NotificationManager) musicService.getSystemService(Context.NOTIFICATION_SERVICE);

        mPlayAction = new NotificationCompat.Action(R.drawable.ic_play_black,
                "play",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        musicService,
                        PlaybackStateCompat.ACTION_PLAY
                ));

        mPauserAction = new NotificationCompat.Action(
                R.drawable.ic_pause_black,
                "pause",
              MediaButtonReceiver.buildMediaButtonPendingIntent(
                      musicService,
                      PlaybackStateCompat.ACTION_PAUSE
              ));

        mPlayPrevAction = new NotificationCompat.Action(
                R.drawable.ic_skip_previous,
                "playnext",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        musicService,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                ));

        mPlayNextAction = new NotificationCompat.Action(
                R.drawable.ic_play_next_black,
                "playNext",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        musicService,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                ));


        mCancelAction = new NotificationCompat.Action(
                R.drawable.ic_cancel,
                "cancel",

                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        musicService,
                        PlaybackStateCompat.ACTION_STOP
                )

        );
        notificationManager.cancelAll();
    }


    public NotificationManager getNotificationManager(){
        return notificationManager;
    }

    public Notification getNotification(MediaMetadataCompat metadata,
                                        PlaybackStateCompat state,
                                        MediaSessionCompat.Token token){
        boolean isPlaying = state.getState() == PlaybackStateCompat.STATE_PLAYING;
        MediaDescriptionCompat description = metadata.getDescription();
        NotificationCompat.Builder builder = buildNotification(state, token, isPlaying, description);
        return builder.build();

    }

    private NotificationCompat.Builder buildNotification(PlaybackStateCompat state,
                                                         MediaSessionCompat.Token token,
                                                         boolean isPlaying,
                                                         MediaDescriptionCompat description){
        if (isAndroidOorHigher()){
           createChannel();
        }

        NotificationCompat.Builder builder= new NotificationCompat.Builder(musicService, CHANNEL_ID);
        builder
                .addAction(mPlayPrevAction)
                .addAction(isPlaying? mPauserAction : mPlayAction)
                .addAction(mPlayNextAction)
                .addAction(mCancelAction)
                .setLargeIcon(description.getIconBitmap())
                .setSmallIcon(R.drawable.mystatusbaricon)
                .setContentIntent(createContentIntent())
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle()).
                setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(token)
                .setShowActionsInCompactView(0,1,2)
                .setShowCancelButton(true).setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(musicService,
                        PlaybackStateCompat.ACTION_STOP)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setOnlyAlertOnce(true)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(musicService, PlaybackStateCompat.ACTION_PAUSE));

        builder.build();
        return builder;

    }

    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(musicService, DetailsActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
               musicService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }


    // Does nothing on versions of Android earlier than O.
    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            CharSequence name = "PlayStein";
            String description = "Music of the people";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            mChannel.setVibrationPattern(
                    new long[]{0});
            notificationManager.createNotificationChannel(mChannel);
            Log.d(TAG, "createChannel: New channel created");
        } else {
            Log.d(TAG, "createChannel: Existing channel reused");
        }
    }


    public boolean isAndroidOorHigher(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }



}
