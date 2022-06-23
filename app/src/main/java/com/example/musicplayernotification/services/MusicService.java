package com.example.musicplayernotification.services;

import static com.example.musicplayernotification.activities.MainActivity.mMusicFiles;
import static com.example.musicplayernotification.utilities.ApplicationClass.ACTION_NEXT;
import static com.example.musicplayernotification.utilities.ApplicationClass.ACTION_PLAY;
import static com.example.musicplayernotification.utilities.ApplicationClass.ACTION_PREVIOUS;
import static com.example.musicplayernotification.utilities.ApplicationClass.CHANNEL_ID_2;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.musicplayernotification.R;
import com.example.musicplayernotification.activities.MainActivity;
import com.example.musicplayernotification.interfaces.ActionPlaying;
import com.example.musicplayernotification.model.MusicModel;
import com.example.musicplayernotification.receiver.NotificationReceiver;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    IBinder myBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    Uri uri;
    ArrayList<MusicModel> musicFiles = new ArrayList<>();
    int position = -1;
    ActionPlaying actionPlaying;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "Method");
        return myBinder;
    }



    public class MyBinder extends Binder{
        public MusicService getService(){
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition",-1);
        String actionName = intent.getStringExtra("ActionName");

        if (myPosition != -1)
        {
            playMedia(myPosition);
        }
        if (actionName != null)
        {
            switch (actionName)
            {
                case "playPause":
                    if (actionPlaying != null)
                    {
                        actionPlaying.btn_play_pauseClicked();
                    }
                    break;
                case "next":
                    if (actionPlaying != null)
                    {
                        actionPlaying.nextBtnClicked();
                    }
                    break;
                case "previous":
                    if (actionPlaying != null)
                    {
                        actionPlaying.btn_prevClicked();
                    }
                    break;
            }
        }

        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        musicFiles = mMusicFiles;
        position = startPosition;
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicFiles != null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
            }
        }else {
            createMediaPlayer(position);
            mediaPlayer.start();
        }
    }


    public void start(){
        mediaPlayer.start();
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    public void stop(){
        mediaPlayer.stop();
    }

    public void pause(){
        mediaPlayer.pause();
    }

   public void release(){
        mediaPlayer.release();
    }

    public void reset(){
        mediaPlayer.reset();
    }

    public void prepare() throws IOException {
        mediaPlayer.prepare();
    }
   public int getDuration(){
       return mediaPlayer.getDuration();
    }

   public void seekTo(int pos){
        mediaPlayer.seekTo(pos);
    }

   public void createMediaPlayer(int positionInner){
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        mediaPlayer = MediaPlayer.create(getBaseContext(),uri);
    }

    public void setDataResources(Context context,Uri uri) throws IOException {
        mediaPlayer.setDataSource(context,uri);
    }
    public int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    public void OnCompleted(){
       mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (actionPlaying != null)
        {
            actionPlaying.nextBtnClicked();
            if (mediaPlayer != null)
            {
                createMediaPlayer(position);
                mediaPlayer.start();
                OnCompleted();
            }
        }

    }

    public void setCallBack(ActionPlaying actionPlaying){
        this.actionPlaying = actionPlaying;
    }

    public void showNotification(int playPauseBtn){
        // Create Remote Views
        RemoteViews notificationLayoutExpanded = new RemoteViews(getPackageName(), R.layout.notification_large);
        RemoteViews notificationLayoutSmall = new RemoteViews(getPackageName(), R.layout.notification_small);
        // set Title
        notificationLayoutExpanded.setTextViewText(R.id.songTitle,musicFiles.get(position).getTitle());
        notificationLayoutSmall.setTextViewText(R.id.songTitle,musicFiles.get(position).getTitle());
        // play icon
        notificationLayoutExpanded.setImageViewResource(R.id.playBtnLarge,playPauseBtn);
        notificationLayoutSmall.setImageViewResource(R.id.playBtnLarge,playPauseBtn);

        // Pending Intents
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent,0);

        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PREVIOUS);
        PendingIntent prevPendingIntent= PendingIntent.getBroadcast(this,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_NEXT);
        PendingIntent nextPendingIntent= PendingIntent.getBroadcast(this,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class).setAction(ACTION_PLAY);
        PendingIntent pausePendingIntent= PendingIntent.getBroadcast(this,0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        // Buttons
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.playBtnLarge,pausePendingIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.nextBtnLarge,nextPendingIntent);
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.previousBtnLarge,prevPendingIntent);
        // Buttons
        notificationLayoutSmall.setOnClickPendingIntent(R.id.playBtnLarge,pausePendingIntent);
        notificationLayoutSmall.setOnClickPendingIntent(R.id.nextBtnLarge,nextPendingIntent);
        notificationLayoutSmall.setOnClickPendingIntent(R.id.previousBtnLarge,prevPendingIntent);

        byte[] picture = null;
        picture = getAlbumArt(mMusicFiles.get(position).getPath());
        Bitmap thumb = null;
        if (picture != null){
            thumb = BitmapFactory.decodeByteArray(picture,0,picture.length);
        }else {
            thumb = BitmapFactory.decodeResource(getResources(),R.drawable.ic_baseline_music_note_24);
        }


        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID_2)
                .setSmallIcon(R.drawable.ic_baseline_music_note_24)
                .setLargeIcon(thumb)
                // .setContentIntent(contentIntent)
                .setCustomContentView(notificationLayoutSmall)
                .setCustomBigContentView(notificationLayoutExpanded)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        //startForeground(0,notification); that not work
        startForeground(1,notification);

        int play_btn = R.drawable.ic_play;
        if (playPauseBtn == play_btn)
        {
            stopForeground(false);
        }

    }

    private byte[] getAlbumArt(String uriPath){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uriPath);
        byte[] art = retriever.getEmbeddedPicture();
        return art;
    }
}