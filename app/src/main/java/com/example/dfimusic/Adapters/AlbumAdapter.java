package com.example.dfimusic.Adapters;

import static com.example.dfimusic.Services.MusicService.TAG;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dfimusic.DisplayItemsActivity;
import com.example.dfimusic.Models.AlbumModel;
import com.example.dfimusic.R;

import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    List<AlbumModel> albumModelList;
    Context context;

    public AlbumAdapter(List<AlbumModel> albumModelList, Context context) {
        this.albumModelList = albumModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_list_layout, parent, false);
        return new AlbumAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlbumModel albumModel = albumModelList.get(position);
        holder.artistName.setText(albumModel.getArtist());
        holder.albumName.setText(albumModel.getAlbumName());
        if (Integer.valueOf(albumModel.getCount())>1){
            holder.count.setText(albumModel.getCount()+ " Songs");
        }
        else{
            holder.count.setText(albumModel.getCount()+ " Song");
        }

        try{
            holder.albumArtWork.setImageURI(getArtWork(albumModel.getAlbumId()));
            if (holder.albumArtWork.getDrawable() == null){
                holder.albumArtWork.setImageResource(R.drawable.iconme);
            }
        }
        catch (Exception e){
            Log.d(TAG, "onBindViewHolder: no album art found exception");
        }





        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, DisplayItemsActivity.class);
                intent.putExtra("nameList", "albums");
                intent.putExtra("focusName", albumModel.getAlbumName());
                intent.putExtra("albumId", albumModel.getAlbumId());
                context.startActivity(intent);
            }
        });


    }

    public synchronized Uri getArtWork(String albumId){
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, Long.parseLong(albumId));
        return albumArtUri;
    }

    @Override
    public int getItemCount() {
        return albumModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView albumName, artistName, count;
        ImageView albumArtWork;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumName = itemView.findViewById(R.id.alAlbumName);
            artistName = itemView.findViewById(R.id.alArtistName);
            count = itemView.findViewById(R.id.alCount);
            albumArtWork = itemView.findViewById(R.id.imgAlbumArtwork);
        }
    }
}
