package com.example.dfimusic;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.example.dfimusic.LocalDAtabases.Favourites;
import com.example.dfimusic.LocalDAtabases.MapPlacesDatabase;
import com.example.dfimusic.LocalDAtabases.PlayMode;

import java.util.Map;

public class GetCursorData extends AsyncTaskLoader<Cursor> {
    String dataName;
    Favourites favourites;
    PlayMode playMode;
    Cursor allFavourites;
    Cursor modes;
    String path;
    Cursor mapPositions;
    MapPlacesDatabase mapPlacesDatabase;
    public GetCursorData(@NonNull Context context, String dataName, PlayMode playMode) {
        super(context);
        this.dataName = dataName;
        this.playMode = playMode;
    }
    public GetCursorData(@NonNull Context context, String dataName, Favourites favourites) {
        super(context);
        this.dataName = dataName;
        this.favourites = favourites;
    }
    public GetCursorData(@NonNull Context context, String dataName, MapPlacesDatabase mapPlacesDatabase, String path) {
        super(context);
        this.dataName = dataName;
        this.mapPlacesDatabase = mapPlacesDatabase;
        this.path = path;

    }



    @Nullable
    @Override
    public Cursor loadInBackground() {
        if (dataName.equals("favourites")){
            allFavourites= favourites.getFavourites();
            return allFavourites;
        }
        else if (dataName.equals("playMode")){
           // playMode = new PlayMode(getContext());
            modes = playMode.getModes();
            return modes;
        }
        else if (dataName.equals("map")){
            mapPositions = mapPlacesDatabase.getMapPositions(path);
            return mapPositions;
        }
        else{
            return null;
        }

    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        cancelLoad();
    }


    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();
    }

    @Override
    protected boolean onCancelLoad() {
        return super.onCancelLoad();
    }

    @Override
    public void onCanceled(@Nullable Cursor data) {
        super.onCanceled(data);
        data.close();
    }


    @Override
    public boolean cancelLoad() {
        return super.cancelLoad();
    }
}
