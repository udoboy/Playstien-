package com.example.dfimusic.Adapters;

import static android.content.Context.MODE_PRIVATE;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;



import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dfimusic.DetailsActivity;
import com.example.dfimusic.DisplayItemsActivity;
import com.example.dfimusic.Hub;
import com.example.dfimusic.Models.ArtistModel;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.R;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class ArtistsListAdapter extends RecyclerView.Adapter<ArtistsListAdapter.ViewHolder> {
    public static final String TAG = "artistlistadapter";
    List<ArtistModel> artistModelList;
    Context context;
    List<AudioModel>getImagesList;


    public ArtistsListAdapter(List<ArtistModel> artistModelList, Context context) {
        this.artistModelList = artistModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_item, parent, false);
        return new ArtistsListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArtistModel artistModel = artistModelList.get(position);
        getImagesList = new ArrayList<>();
        if(artistModel.getArtistName()!= null && artistModel.getNumberOfSongs() != null){
            holder.artistName.setText(artistModel.getArtistName());
            if (Integer.parseInt(artistModel.getNumberOfSongs())>1){
                holder.numberOfSongs.setText(artistModel.getNumberOfSongs()+ " Songs");
            }
            else{
                holder.numberOfSongs.setText(artistModel.getNumberOfSongs()+ " Song");
            }

        }


        Uri artwork = getArtWork(artistModel.getSampleImagePath());
            try{
                holder.songCover.setImageURI(artwork);
                if (holder.songCover.getDrawable() == null){
                    holder.songCover.setImageResource(R.drawable.iconme);

                }
            }
            catch (Exception e){
                Log.d(TAG, "onBindViewHolder: no album art found exception ");
            }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DisplayItemsActivity.class);
                intent.putExtra("nameList", "artist");
                intent.putExtra("focusName", artistModel.getArtistName());
                intent.putExtra("albumId", artistModel.getSampleImagePath());
                System.out.println(artistModel.getSampleImagePath());
                context.startActivity(intent);
            }
        });
    }

    public synchronized Uri getArtWork(String albumId){
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        if (albumId != null){
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(albumId));
            return albumArtUri;
        }
        else{
            Uri albumArt = ContentUris.withAppendedId(sArtworkUri, R.drawable.iconme);
            return albumArt;
        }

    }


    @Override
    public int getItemCount() {
        return artistModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView artistName;
        TextView numberOfSongs;
        CircleImageView songCover;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artistName = itemView.findViewById(R.id.aiArtistName);
            numberOfSongs = itemView.findViewById(R.id.aiNumberOfSongs);
            songCover = itemView.findViewById(R.id.aiSongCover);
        }
    }
}
