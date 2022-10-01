package com.example.dfimusic.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dfimusic.LocalDAtabases.MapPlacesDatabase;
import com.example.dfimusic.MapAdapterClicks;
import com.example.dfimusic.Models.MapModel;
import com.example.dfimusic.R;

import java.util.List;
import java.util.Map;

public class MapPositionAdapter extends RecyclerView.Adapter<MapPositionAdapter.ViewHolder> {
    List<MapModel> mapPositionsList;
    Context context;
    MapAdapterClicks mapAdapterClicks;

    public MapPositionAdapter(List<MapModel> mapPositionsList, Context context, MapAdapterClicks mapAdapterClicks) {
        this.mapPositionsList = mapPositionsList;
        this.context = context;
        this.mapAdapterClicks = mapAdapterClicks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_positions_layout, parent, false);
        return new MapPositionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        MapModel currentMap = mapPositionsList.get(position);
        holder.mapPositionName.setText(currentMap.getMapName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapAdapterClicks.onItemClicked(currentMap.getMapPosition());
            }
        });

        holder.deleteMapVew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapPlacesDatabase mapPlacesDatabase = new MapPlacesDatabase(context);
               boolean deleted = mapPlacesDatabase.deletePosition(String.valueOf(currentMap.getId()));
               if (deleted){
                   mapPositionsList.remove(position);
                   notifyDataSetChanged();
               }
                else{
                   Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
               }
                
            }
        });
    }

    @Override
    public int getItemCount() {
        return mapPositionsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView mapPositionName;
        ImageView deleteMapVew;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mapPositionName = itemView.findViewById(R.id.txtMapPositionName);
            deleteMapVew = itemView.findViewById(R.id.deleteMapPosition);
        }
    }
}
