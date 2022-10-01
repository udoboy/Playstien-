package com.example.dfimusic.Adapters;


import static android.content.Context.MODE_PRIVATE;

import static java.lang.System.out;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.media.session.MediaButtonReceiver;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dfimusic.Hub;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.Models.MyMediaPlayer;
import com.example.dfimusic.MusicPlayerActivity;
import com.example.dfimusic.R;

import java.util.List;

public class DisplayListAdapter extends RecyclerView.Adapter<DisplayListAdapter.ViewHolder> {
    List<AudioModel> songList;
    Context context;

    public DisplayListAdapter(List<AudioModel> songList, Context context) {
        this.songList = songList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.display_songs_layout, parent, false);
        return new DisplayListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
      AudioModel song = songList.get(position);
      holder.songName.setText(song.getTitle());
        String currentPath = context.getSharedPreferences("playingStatus", MODE_PRIVATE).getString("playingPath", "none");

        holder.closeDisplayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                songList.remove(songList.get(position));
                notifyDataSetChanged();
            }
        });

        if (currentPath != null){
            if (position == MyMediaPlayer.currentIndex){
                holder.songName.setTextColor(Color.parseColor("#ee4e34"));
                holder.closeDisplayList.setVisibility(View.GONE);
            }
            else{
                holder.songName.setTextColor(Color.parseColor("#FFFFFF"));
                holder.closeDisplayList.setVisibility(View.VISIBLE);
            }
        }
//
//        A SQLiteConnection object for database '+data+user+0+com_example_dfimusic+databases+modeDb' was leaked!
//                Please fix your application to end transactions in progress properly and to close the database when it is no longer needed.


        holder.closeDisplayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (position == MyMediaPlayer.currentIndex){
                    //write your code here
                }
                else{
                    out.println(position);
                    getCurrentList().remove(song);
                    songList.remove(song);
                    notifyDataSetChanged();
                }

            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                out.println(MyMediaPlayer.currentIndex);

                MyMediaPlayer.currentIndex = getCurrentList().indexOf(song)-1;
                try {
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT).send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView songName;
        ImageView closeDisplayList;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.displaySongName);
            closeDisplayList = itemView.findViewById(R.id.closeDisplayList);
        }
    }

    public int getIndex(String path){
        int songIndex =-1;
        for (AudioModel audioModel: songList){
            if (audioModel.getPath().equals(path)){
              songIndex = songList.indexOf(audioModel);
              break;
            }

        }
        return songIndex;

    }

    public List<AudioModel> getCurrentList(){
        if (Hub.getCurrentMode().equals("shuffle")){
            out.println("getting current from shuffle");
            return Hub.getShuffledSong();

        }
        else{
            out.println("getting current from song model");
            return Hub.getSongsModel();
        }

    }
}
