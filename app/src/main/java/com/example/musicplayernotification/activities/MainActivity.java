package com.example.musicplayernotification.activities;

import static com.example.musicplayernotification.activities.MusicListActivity.repeatBoolean;
import static com.example.musicplayernotification.activities.MusicListActivity.shuffleBoolean;
import static com.example.musicplayernotification.utilities.ApplicationClass.ACTION_NEXT;
import static com.example.musicplayernotification.utilities.ApplicationClass.ACTION_PLAY;
import static com.example.musicplayernotification.utilities.ApplicationClass.ACTION_PREVIOUS;
import static com.example.musicplayernotification.utilities.ApplicationClass.CHANNEL_ID_2;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.musicplayernotification.R;
import com.example.musicplayernotification.databinding.ActivityMainBinding;
import com.example.musicplayernotification.interfaces.ActionPlaying;
import com.example.musicplayernotification.model.MusicModel;
import com.example.musicplayernotification.receiver.NotificationReceiver;
import com.example.musicplayernotification.services.MusicService;
import com.example.musicplayernotification.utilities.Constants;
import com.example.musicplayernotification.utilities.PreferenceManager;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements ActionPlaying, ServiceConnection{
    private ActivityMainBinding binding;
    public static ArrayList<MusicModel> mMusicFiles = new ArrayList<>();
    private String previousTrackTitle,songTitle,albumName,artistName;
    private PreferenceManager preferenceManager;
    MusicService musicService;
    Uri uri;


    private int position = 0 ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        checkForPermissions();
        init();
        getIntentMethod();
        setListeners();
        setDataIntoSharedPreferences();



    }

    private void getIntentMethod(){
        position = getIntent().getIntExtra("position", -1);

        songTitle = getIntent().getStringExtra("song_title");
        albumName = getIntent().getStringExtra("album_name");
        artistName = getIntent().getStringExtra("artist_name");

        String sender = getIntent().getStringExtra("sender");
        if (sender != null && sender.equals("allMusicFile"))
        {
            mMusicFiles = getAllMusicFiles();
            binding.song.setText(mMusicFiles.get(position).getTitle());
        }

        if(mMusicFiles != null)
        {
            binding.play.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(mMusicFiles.get(position).getPath());
        }

        Intent intent = new Intent(this,MusicService.class);
        intent.putExtra("servicePosition",position);
        startService(intent);

    }

    private void setDataIntoSharedPreferences(){
        preferenceManager.putInt(Constants.KEY_LAST_POSITION,position);
        preferenceManager.putString(Constants.KEY_LAST_MUSIC_LIST,Constants.METHOD_STORAGE_ALL_MUSIC);
        preferenceManager.putString(Constants.KEY_LAST_MUSIC_PATH,mMusicFiles.get(position).getPath());
        preferenceManager.putString(Constants.KEY_LAST_SONG_TITLE,mMusicFiles.get(position).getDisplayName());
        preferenceManager.putString(Constants.KEY_LAST_ALBUM_NAME,mMusicFiles.get(position).getAlbumName());
    }





    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        previousTrackTitle = preferenceManager.getString(Constants.KEY_LAST_SONG_TITLE);
    }

    private void setListeners() {
        binding.next.setOnClickListener(view -> {
            nextBtnClicked();
        });

        binding.play.setOnClickListener(view -> {
            if (musicService.isPlaying()){
                musicService.showNotification(R.drawable.ic_play);
                binding.play.setImageResource(R.drawable.ic_play);
                musicService.pause();
            }else {
                musicService.showNotification(R.drawable.ic_pause);
                binding.play.setImageResource(R.drawable.ic_pause);
                musicService.start();




            }
        });

        binding.previous.setOnClickListener(view -> {
            btn_prevClicked();
        });

        binding.shuffle.setOnClickListener(view -> {
            if (shuffleBoolean)
            {
                shuffleBoolean = false;
                binding.shuffle.setImageResource(R.drawable.shuffle);
            }else {
                shuffleBoolean = true;
                binding.shuffle.setImageResource(R.drawable.ic_shuffle_on);
            }
        });
        binding.repeat.setOnClickListener(view -> {
            if (repeatBoolean)
            {
                repeatBoolean = false;
                binding.repeat.setImageResource(R.drawable.repeat);
            }else {
                repeatBoolean = true;
                binding.repeat.setImageResource(R.drawable.ic_repeat_of);
            }
        });
    }



    public void btn_play_pauseClicked(){
        if (musicService.isPlaying())
        {
            binding.play.setImageResource(R.drawable.ic_play);
            musicService.showNotification(R.drawable.ic_play);
            musicService.pause();
            binding.seekBar.setMax(musicService.getDuration()/1000);
            // setProgress
           /* MainActivity.this.runOnUiThread(() -> {
                if (musicService != null)
                {
                    int mCurrentPosition = musicService.getCurrentPosition();
                    binding.seekBar.setProgress(mCurrentPosition);
                }
                handler.postDelayed(this,1000);
            });*/
        }else {
            musicService.showNotification(R.drawable.ic_pause);
            binding.play.setImageResource(R.drawable.ic_pause);
            musicService.start();
            binding.seekBar.setMax(musicService.getDuration()/1000);
            // setProgressBar
        }
    }


    public void nextBtnClicked(){
        if (musicService.isPlaying())
        {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandomPosition(mMusicFiles.size() - 1);
            }else if (!shuffleBoolean && !repeatBoolean)
            {
                position = ((position+1) % mMusicFiles.size());
            }
            //else position will be position
            setDataIntoSharedPreferences();
            uri = Uri.parse(mMusicFiles.get(position).getPath());
            musicService.createMediaPlayer(position);
            // metaData(uri)
            binding.song.setText(mMusicFiles.get(position).getDisplayName());
            binding.artist.setText(mMusicFiles.get(position).getNameArtist());
            binding.album.setText(mMusicFiles.get(position).getAlbumName());
            binding.seekBar.setMax(musicService.getDuration()/1000);
            // setProgress Seek Bar
            /*MainActivity.this.runOnUiThread(() -> {
                if (musicService != null)
                {
                    int mCurrentPosition = musicService.getCurrentPosition();
                    binding.seekBar.setProgress(mCurrentPosition);
                }

            });*/
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            binding.play.setImageResource(R.drawable.ic_pause);
            musicService.start();

        }else {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandomPosition(mMusicFiles.size() -1);
            }else if (!shuffleBoolean && !repeatBoolean)
            {
                position = ((position+1) % mMusicFiles.size());
            }
            uri = Uri.parse(mMusicFiles.get(position).getPath());
            musicService.createMediaPlayer(position);
            // metaData(uri)
            binding.song.setText(mMusicFiles.get(position).getDisplayName());
            binding.artist.setText(mMusicFiles.get(position).getNameArtist());
            binding.album.setText(mMusicFiles.get(position).getAlbumName());
            binding.seekBar.setMax(musicService.getDuration()/1000);
            // setProgress Seek Bar
           /* MainActivity.this.runOnUiThread(() -> {
                if (musicService != null)
                {
                    int mCurrentPosition = musicService.getCurrentPosition();
                    binding.seekBar.setProgress(mCurrentPosition);
                }

            });*/
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_play);
            binding.play.setImageResource(R.drawable.ic_play);
        }
    }

    public void btn_prevClicked(){
        if (musicService.isPlaying())
        {
            musicService.stop();
            musicService.release();
            position = ((position-1) < 0 ? (mMusicFiles.size() -1) : position-1);
            uri = Uri.parse(mMusicFiles.get(position).getPath());
            musicService.createMediaPlayer(position);
            // metaData(uri)
            binding.song.setText(mMusicFiles.get(position).getDisplayName());
            binding.artist.setText(mMusicFiles.get(position).getNameArtist());
            binding.album.setText(mMusicFiles.get(position).getAlbumName());
            binding.seekBar.setMax(musicService.getDuration()/1000);
            // setProgress Seek Bar
          /* MainActivity.this.runOnUiThread(() -> {
                if (musicService != null)
                {
                    int mCurrentPosition = musicService.getCurrentPosition();
                    binding.seekBar.setProgress(mCurrentPosition);
                }

            });*/
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_pause);
            binding.play.setImageResource(R.drawable.ic_pause);
            musicService.start();

        }else {
            musicService.stop();
            musicService.release();
            position = ((position-1) < 0 ? (mMusicFiles.size() -1) : position-1);
            uri = Uri.parse(mMusicFiles.get(position).getPath());
            musicService.createMediaPlayer(position);
            // metaData(uri)
            binding.song.setText(mMusicFiles.get(position).getDisplayName());
            binding.artist.setText(mMusicFiles.get(position).getNameArtist());
            binding.album.setText(mMusicFiles.get(position).getAlbumName());
            binding.seekBar.setMax(musicService.getDuration()/1000);
            // setProgress Seek Bar
            MainActivity.this.runOnUiThread(() -> {
                if (musicService != null)
                {
                    int mCurrentPosition = musicService.getCurrentPosition();
                    binding.seekBar.setProgress(mCurrentPosition);
                }

            });
            musicService.OnCompleted();
            musicService.showNotification(R.drawable.ic_play);
            binding.play.setImageResource(R.drawable.ic_play);
        }
    }

    private int getRandomPosition(int i) {
        // between 0 and list size -1
        Random random = new Random();
        return random.nextInt(i+1);
    }


    private ArrayList<MusicModel> getAllMusicFiles(){
        ArrayList<MusicModel> list = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,

        };



        String selection = MediaStore.Audio.Media.IS_MUSIC + " !=0 ";

        Cursor cursor = getContentResolver().query(uri, projection, selection,
                null, null);
        // Cursor start
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                String disName = cursor.getString(2);
                String size = cursor.getString(3);
                String duration = cursor.getString(4);
                String path = cursor.getString(5);
                String date_added = cursor.getString(6);
                String album = cursor.getString(7);
                String artist = cursor.getString(8);

                MusicModel musicFiles = new MusicModel(id, title, disName, size, duration, path, date_added,album,artist,artist);


                list.add(musicFiles);


            }
            cursor.close();
        }
        return list;
    }

    private void checkForPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            requestPermissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    ActivityResultLauncher<String> requestPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result)
                {
                    Toast.makeText(MainActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                }
            }
    );




    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MainActivity.this,MusicListActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this,MusicService.class);
        bindService(intent,this,BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
        Toast.makeText(this, "Connected"+musicService, Toast.LENGTH_SHORT).show();
        binding.seekBar.setMax(musicService.getDuration() / 1000);
        // metaData(uri)
        binding.song.setText(mMusicFiles.get(position).getTitle());
        binding.album.setText(mMusicFiles.get(position).getAlbumName());
        binding.artist.setText(mMusicFiles.get(position).getNameArtist());
        musicService.showNotification(R.drawable.ic_pause);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        musicService = null;
    }



}