package com.example.dfimusic.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dfimusic.LocalDAtabases.PlayListDatabase;
import com.example.dfimusic.PlaylistActivity;
import com.example.dfimusic.R;

import java.util.ArrayList;
import java.util.List;

public class PlayListNamesAdapter extends RecyclerView.Adapter<PlayListNamesAdapter.ViewHolder> {
    List<String> playListNamesList;
    Context context;
    String path;
    List<String> playListItemPath;

    public PlayListNamesAdapter(List<String> playListNamesList, Context context, String path) {
        this.playListNamesList = playListNamesList;
        this.context = context;
        this.path = path;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_layout_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String currentPlaylist = playListNamesList.get(position);
        playListItemPath = new ArrayList<>();
        holder.txtPlayListName.setText(currentPlaylist);



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (path!= "null"){
                    playListItemPath.clear();
                    PlayListDatabase playListDatabase = new PlayListDatabase(context);
                    Cursor cursor = playListDatabase.getPlayListItems(currentPlaylist);
                    while (cursor.moveToNext()){
                        String path = cursor.getString(2);
                        playListItemPath.add(path);
                    }
                    if(playListItemPath!= null){
                        if (playListItemPath.contains(path)){
                            AlertDialog.Builder alertdialogue = new AlertDialog.Builder(context);
                            alertdialogue.setMessage("Song already exists in this playlist");
                            alertdialogue.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ((Activity)context).finish();
                                }
                            });

                            alertdialogue.setNegativeButton("Add to another Playlist", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });

                            alertdialogue.show();
                        }
                        else{
                            boolean insert= playListDatabase.addPlayListPathItem(path, currentPlaylist);
                            if (insert){
                                Toast.makeText(context, "Added to Playlist " + currentPlaylist, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(context, PlaylistActivity.class);
                                intent.putExtra("playListName", currentPlaylist);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            }
                            else{
                                Toast.makeText(context, "Something went wrong ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }


                }
                else{
                    Intent intent = new Intent(context, PlaylistActivity.class);
                    intent.putExtra("playListName", currentPlaylist);
                    context.startActivity(intent);
                }
            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                String text = "Delete " + currentPlaylist + " playlist";
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.getMenu().add(text);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getTitle().equals(text)){
                            PlayListDatabase playListDatabase = new PlayListDatabase(context);
                            boolean deleted = playListDatabase.deleteAllPlaylistTableItems(currentPlaylist);
                            if (deleted == true){
                                Toast.makeText(context, "Playlist deleted successfully", Toast.LENGTH_SHORT).show();
                                playListNamesList.remove(currentPlaylist);
                                notifyDataSetChanged();
                            }
                            else{
                                Toast.makeText(context, "An error occurred please try again", Toast.LENGTH_SHORT).show();
                            }
                        }
                        return true;
                    }

                });
                popupMenu.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return playListNamesList.size();
    }

    public class  ViewHolder extends RecyclerView.ViewHolder{
        TextView txtPlayListName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlayListName = itemView.findViewById(R.id.txtPlaylistName);

        }
    }
}
