package com.example.musicplayernotification.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.musicplayernotification.activities.MainActivity;
import com.example.musicplayernotification.model.MusicModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.example.musicplayernotification.R;

import java.util.ArrayList;

public class MusicFilesAdapter extends RecyclerView.Adapter<MusicFilesAdapter.ViewHolder> {
    private ArrayList<MusicModel> musicList;
    private Context context;
    BottomSheetDialog bottomSheetDialog;
    private int viewType;
    public  static int position001;
    String typeAdapter;
    String putExtraType;




    public MusicFilesAdapter(ArrayList<MusicModel> videoList, Context context,int viewType,String typeAdapter) {
        this.musicList = videoList;
        this.context = context;
        this.viewType = viewType;
        this.typeAdapter = typeAdapter;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicFilesAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        position001 = position;
        holder.musicName.setText(musicList.get(position).getDisplayName());
        String size = musicList.get(position).getSize();
        holder.musicSize.setText(android.text.format.Formatter.formatFileSize(context, Long.parseLong(size)));

        byte[] image = getAlbumArt(musicList.get(position).getPath());

        if (image != null){
            Glide.with(context).load(image).
                    into(holder.thumbnail_card);
        }else {
            Glide.with(context)
                    .load(R.drawable.ic_baseline_music_note_24)
                    .into(holder.thumbnail_card);
        }


        if (typeAdapter.equals("ByArtist"))
        {
            putExtraType = "MusicAllFilesArtist";

        }else if (typeAdapter.equals("ByAlbums"))
        {
            putExtraType = "MusicAllFilesAlbums";
        }
        else
        {
            putExtraType = "MusicFilesAdapter";
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("position", position);
            intent.putExtra("sender", "allMusicFile");
            intent.putExtra("song_title", musicList.get(position).getDisplayName());
            intent.putExtra("album_name", musicList.get(position).getAlbumName());
            intent.putExtra("artist_name", musicList.get(position).getNameArtist());
            intent.putExtra("FromIntent", "MusicFilesActivity");
            intent.putExtra(putExtraType,true);
            context.startActivity(intent);
            ((Activity) context).finish();
        });


    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView menu_more,thumbnail_card;
        TextView musicName,musicSize;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            menu_more = itemView.findViewById(R.id.music_menu_more);
            musicName = itemView.findViewById(R.id.MusicName);
            musicSize = itemView.findViewById(R.id.MusicSize);
            thumbnail_card = itemView.findViewById(R.id.thumbnail_card);
        }
    }
    void updateMusicFiles(ArrayList<MusicModel> files){
        musicList = new ArrayList<>();
        musicList.addAll(files);
        notifyDataSetChanged();

    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever =new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art =retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    public String timeConversion(long value){
        String videoTime;
        int duration = (int) value;
        int hrs =(duration/3600000); /// hrs / milli Second
        int mns =(duration/60000) % 60000;  /// minute / milli Second
        int scs = duration%60000/1000;

        if (hrs>0){
            videoTime = String.format("%02d:%02d:%02d",hrs,mns,scs);
        }else {
            videoTime = String.format("%02d:%02d",mns,scs);
        }
        return videoTime;
    }



}
