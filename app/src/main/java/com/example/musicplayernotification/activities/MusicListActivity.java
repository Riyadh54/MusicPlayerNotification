package com.example.musicplayernotification.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayernotification.adapter.MusicFilesAdapter;
import com.example.musicplayernotification.databinding.ActivityMusicListBinding;
import com.example.musicplayernotification.model.MusicModel;
import com.example.musicplayernotification.utilities.Constants;
import com.example.musicplayernotification.utilities.PreferenceManager;

import java.io.IOException;
import java.util.ArrayList;

public class MusicListActivity extends AppCompatActivity{
    private ArrayList<MusicModel> mMusicFiles = new ArrayList<>();
    static MusicFilesAdapter musicFilesAdapter;
    private ActivityMusicListBinding binding;
    private  String folderPath,folderName,artistName,albumName,musicListName;
    private int position;
    private PreferenceManager preferenceManager;
    static boolean shuffleBoolean = false, repeatBoolean = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMusicListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        checkForPermissions();
        showMusicFiles();
        init();
        checkPlayMusicConditionForTheFirstTime();
        getTrackInformation();



    }



    private void getTrackInformation() {
        position = preferenceManager.getInt(Constants.KEY_LAST_POSITION);
        musicListName = preferenceManager.getString(Constants.KEY_LAST_MUSIC_LIST);
        folderPath = preferenceManager.getString(Constants.KEY_LAST_MUSIC_PATH);
        folderName = preferenceManager.getString(Constants.KEY_LAST_SONG_TITLE);
        albumName = preferenceManager.getString(Constants.KEY_LAST_ALBUM_NAME);

    }

    private void setDataIntoSharedPreferences(){
        preferenceManager.putInt(Constants.KEY_LAST_POSITION,position);
        preferenceManager.putString(Constants.KEY_LAST_MUSIC_LIST,Constants.METHOD_STORAGE_ALL_MUSIC);
        preferenceManager.putString(Constants.KEY_LAST_MUSIC_PATH,mMusicFiles.get(position).getPath());
        preferenceManager.putString(Constants.KEY_LAST_SONG_TITLE,mMusicFiles.get(position).getDisplayName());
        preferenceManager.putString(Constants.KEY_LAST_ALBUM_NAME,mMusicFiles.get(position).getAlbumName());
    }


    private void checkPlayMusicConditionForTheFirstTime() {
        folderPath = preferenceManager.getString(Constants.KEY_LAST_MUSIC_PATH);
        if (folderPath.equals("abc"))
        {
            if (getAllMusicFiles().size()>0)
            {
                position = 0;
                folderPath = getAllMusicFiles().get(0).getPath();
                setDataIntoSharedPreferences();
            }else {
               // showToast("no music in this phone");
            }
        }else {
            //showToast("folder path not empty");
        }

    }

    private void init(){

        preferenceManager = new PreferenceManager(getApplicationContext());
    }


    private void showMusicFiles() {
        mMusicFiles = getAllMusicFiles();
        musicFilesAdapter = new MusicFilesAdapter(mMusicFiles, this,0,"AllMusic");
        binding.musicRv.setAdapter(musicFilesAdapter);
        binding.musicRv.setLayoutManager(new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false));
        musicFilesAdapter.notifyDataSetChanged();
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
        if (ContextCompat.checkSelfPermission(MusicListActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            requestPermissions.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    ActivityResultLauncher<String> requestPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result)
                {
                    Toast.makeText(MusicListActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                    showMusicFiles();
                }
            }
    );

    private void showToast(String message){
        Toast.makeText(this, ""+message, Toast.LENGTH_SHORT).show();
    }





}