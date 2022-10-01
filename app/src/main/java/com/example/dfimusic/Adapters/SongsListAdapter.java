package com.example.dfimusic.Adapters;

import static android.content.Context.MODE_PRIVATE;

import static java.lang.System.loadLibrary;
import static java.lang.System.out;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.RecyclerView;

import com.example.dfimusic.Hub;
import com.example.dfimusic.LocalDAtabases.Favourites;
import com.example.dfimusic.LocalDAtabases.PlayListDatabase;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.Models.MyMediaPlayer;
import com.example.dfimusic.Models.OptionsDialog;
import com.example.dfimusic.MusicPlayerActivity;
import com.example.dfimusic.PlayListOptionsActivity;
import com.example.dfimusic.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.ViewHolder> {
    ArrayList<AudioModel> songList;
    Context context;
    List<String> songsPath;
    AudioModel song;
    List<AudioModel> shuffledList;
    List<AudioModel> mainList;
    String currentPlayListName;

    public SongsListAdapter() {
    }

    public SongsListAdapter(ArrayList<AudioModel> songList, Context context) {
        this.songList = songList;
        this.context = context;
    }

    public SongsListAdapter(Context context) {
        this.context = context;
    }

    public SongsListAdapter(ArrayList<AudioModel> songList, Context context, String currentPlayListName) {
        this.songList = songList;
        this.context = context;
        this.currentPlayListName = currentPlayListName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.songs_item, parent, false);
        return new SongsListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
         song = songList.get(position);
        songsPath = new ArrayList<>();
        OptionsDialog optionsDialog = new OptionsDialog(context);
        TextView songName = optionsDialog.findViewById(R.id.opSongName);
        ImageView songCover = optionsDialog.findViewById(R.id.opImageView);
        LinearLayout oplPlayNext = optionsDialog.findViewById(R.id.opLPlayNext);
        LinearLayout oplAddFav = optionsDialog.findViewById(R.id.opLAddFav);
        LinearLayout oplAddPlayList = optionsDialog.findViewById(R.id.opLAddPlayList);
        TextView txtAddFav = optionsDialog.findViewById(R.id.oplTxtAddFav);
        LinearLayout shareMusicLayout = optionsDialog.findViewById(R.id.opLShareMusic);
        ImageView oplImgFav = optionsDialog.findViewById(R.id.oplImgFavHeart);
        songName.setSelected(true);
        List<String> favouritesList = new ArrayList<>();


        String currentPath = context.getSharedPreferences("playingStatus", MODE_PRIVATE).getString("playingPath", "none");

        if (currentPath != "none"){
            if (song.getPath().equals(currentPath)){
                holder.songName.setTextColor(Color.parseColor("#ee4e34"));
                holder.metaData.setTextColor(Color.parseColor("#ee4e34"));
            }
            else{
                holder.songName.setTextColor(Color.parseColor("#FFFFFF"));
                holder.metaData.setTextColor(Color.parseColor("#FFFFFF"));
            }
        }


        for (AudioModel audioModel: songList){
            songsPath.add(audioModel.getPath());
        }

        holder.songName.setText(song.getTitle());

        if (song.getArtist().equals("<unknown>")){
            holder.metaData.setText("-unknown-");
        }
        else{
            holder.metaData.setText(song.getArtist());

        }

        shareMusicLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("audio/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(songList.get(position).getPath()));
                shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(shareIntent, "Share Music"));
            }
        });
        oplAddPlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PlayListOptionsActivity.class);
                intent.putExtra("currentPath",songList.get(position).getPath());
                intent.putExtra("songName", songList.get(position).getTitle());
                context.startActivity(intent);
            }
        });

        oplAddFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Favourites favourites = new Favourites(context);
                if (txtAddFav.getText().equals("Add to favourite")){
                    String currentPath = songList.get(position).getPath();

                    if (favouritesList.contains(currentPath)){
                        Toast.makeText(context, "Already a favourite", Toast.LENGTH_SHORT).show();
                    }

                    else{
                        boolean inserted =favourites.addFavourite(songList.get(position).getPath());
                        if (inserted == true){
                            Toast.makeText(context, "Added to Favourites", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(context, "Something went wrong please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else if (txtAddFav.getText().equals("Remove from favourite")){
                   boolean deleted= favourites.removeFavourite(songList.get(position).getPath());
                   if (deleted){
                       Toast.makeText(context, "Removed From Favourite", Toast.LENGTH_SHORT).show();
                   }
                   else{
                       Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
                   }


                   /** The code below is used to check if we are in the following list activity
                    * if true then the list is updated to show that an item has been deleted**/
                   if (context.toString().contains("FavouritesListActivity")){
                       songList.remove(songList.get(position));
                       notifyDataSetChanged();

                   }
                   else {
                   }
                }

                optionsDialog.dismiss();
                favourites.close();
            }
        });

        oplPlayNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentList().add(MyMediaPlayer.currentIndex+1 , songList.get(position));
                optionsDialog.dismiss();
                Toast.makeText(context, "Added to queue", Toast.LENGTH_SHORT).show();
            }
        });

        holder.imgPopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Hub thub = new Hub(context);
                favouritesList.clear();
                    if (getCurrentList() == null){
                        shuffleSongs();
                        setOrderedList();
                    }
                    String currentPath = songList.get(position).getPath();
                    Favourites favourites = new Favourites(context);
                    Cursor allFavourites = favourites.getFavourites();
                    while (allFavourites.moveToNext()){
                        String cur = allFavourites.getString(1);
                        favouritesList.add(cur);
                    }
                    allFavourites.close();

                    if (favouritesList.contains(currentPath)){
                        oplImgFav.setImageResource(R.drawable.ic_fav_color_filled);
                        txtAddFav.setText("Remove from favourite");
                    }
                    else{
                        oplImgFav.setImageResource(R.drawable.ic_baseline_favorite_border_24);
                        txtAddFav.setText("Add to favourite");
                    }
                    optionsDialog.show();
                    songName.setText(songList.get(position).getTitle());

                    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(songList.get(position).getPath());
                    byte[] songCoverByte = mediaMetadataRetriever.getEmbeddedPicture();
                    if (songCoverByte != null){
                        Bitmap songCoverBitmap = BitmapFactory.decodeByteArray(songCoverByte,0, songCoverByte.length);
                        songCover.setImageBitmap(songCoverBitmap);
                    }
                    else{
                        songCover.setImageResource(R.drawable.iconme);
                    }
                    favourites.close();
                    allFavourites.close();
                    thub = null;
                }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (new File(song.getPath()).exists()){
                    shuffleSongs();
                    setOrderedList();
                    MyMediaPlayer.getInstance().reset();
                    MyMediaPlayer.currentIndex = position;
                    Intent intent = new Intent(context, MusicPlayerActivity.class);
                    intent.putExtra("source", 1);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }


            }
        });
        if (context.toString().contains("PlaylistActivity")){
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(context, view);
                    popupMenu.getMenu().add("Remove from this Playlist");
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                           if (menuItem.getTitle().equals("Remove from this Playlist")){
                               PlayListDatabase playListDatabase = new PlayListDatabase(context);
                               if (currentPlayListName != null){
                                   boolean deleted = playListDatabase.removeFromPlaylist(song.getPath(), currentPlayListName);
                                   if (deleted){
                                       Toast.makeText(context, "Song removed successfully", Toast.LENGTH_SHORT).show();
                                       songList.remove(song);
                                       notifyDataSetChanged();
                                       playListDatabase.close();

                                   }
                                   else{
                                       Toast.makeText(context, "A problem occurred pleas try again", Toast.LENGTH_SHORT).show();
                                       playListDatabase.close();
                                   }
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
    }

    public void shuffleSongs(){
        shuffledList = new ArrayList<>();
        shuffledList.clear();
        for (AudioModel audioModel: songList){
            shuffledList.add(audioModel);
        }
        Collections.shuffle(shuffledList);
        Hub.setShuffledSong(shuffledList);

    }
    public void setOrderedList(){
        mainList = new ArrayList<>();
        for (AudioModel audioModel: songList){
            mainList.add(audioModel);
        }
        Hub.setSongsModel(mainList);
    }

    public void playPath(String path){
        for (AudioModel audioModel: Hub.getSongsModel()){
            if (audioModel.getPath().equals(path)){
                MyMediaPlayer.currentIndex = Hub.getSongsModel().indexOf(audioModel);
                break;
            }
        }
        Intent intent = new Intent(context, MusicPlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("source", 2);
        context.startActivity(intent);
    }

    public List<AudioModel> getCurrentList(){
        String currentMode = Hub.getCurrentMode();
        if (currentMode.equals("shuffle")){
            return Hub.getShuffledSong();
        }
        else{
            return Hub.getSongsModel();
        }

    }


    @Override
    public int getItemCount() {
        return songList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView songName , metaData;
        ImageView imgPopUp;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.sSongName);
            metaData = itemView.findViewById(R.id.sMetaData);
            imgPopUp = itemView.findViewById(R.id.imgPopUp);
        }

    }
}
