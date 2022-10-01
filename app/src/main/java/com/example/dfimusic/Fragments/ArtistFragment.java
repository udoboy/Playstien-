package com.example.dfimusic.Fragments;

import static android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dfimusic.Adapters.ArtistsListAdapter;
import com.example.dfimusic.Hub;
import com.example.dfimusic.Models.ArtistModel;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.databinding.FragmentArtistBinding;

import java.io.File;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.RecursiveTask;


public class ArtistFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    FragmentArtistBinding b;
    List<ArtistModel> artistModelList;
    ArtistsListAdapter artistsListAdapter;
    String sampleImagePath;
    Hub hub;
    List<AudioModel> allSongsList;
    static final int ARTIST_LOADER_ID= 3;
    static final int ALL_SONGS_LOADER_ID = 4;

    @Override
    public void onDestroyView() {
        b.artistsRecyclerView.setAdapter(null);
        hub = null;
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        b = FragmentArtistBinding.inflate(inflater, container, false);
        //return inflater.inflate(R.layout.fragment_artist, container, false);

        allSongsList = new ArrayList<>();
        artistModelList = new ArrayList<>();
        artistsListAdapter = new ArtistsListAdapter(artistModelList, getActivity());


        b.artistsRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        b.artistsRecyclerView.setLayoutManager(linearLayoutManager);
        b.artistsRecyclerView.setAdapter(artistsListAdapter);
        b.artistsRecyclerView.setHasFixedSize(true);
        hub = new ViewModelProvider(getActivity()).get(Hub.class);

        setListOrder(getContext().getSharedPreferences("artist_sort", Context.MODE_PRIVATE).getString("artist_sort", "none"));

        Observer<Integer> sortChangeObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                setListOrder(getContext().getSharedPreferences("artists_sort", Context.MODE_PRIVATE).getString("artists_sort", "none"));
            }
        };

        hub.getSortChangeInteger().observe(getActivity(), sortChangeObserver);

        return b.getRoot();
    }

    @Override
    public void onStart() {
        getActivity().getSupportLoaderManager().restartLoader(ALL_SONGS_LOADER_ID, null, this);
        getActivity().getSupportLoaderManager().restartLoader(ARTIST_LOADER_ID, null, this);
        super.onStart();
    }

    public synchronized String getSampleImagePath(String artistName){
        for (AudioModel audioModel: allSongsList){
            if (audioModel.getArtist().equals(artistName)){
                sampleImagePath = audioModel.getAlbumId();
                break;
            }
        }

        return sampleImagePath;
    }

    public void setListOrder(String order){
        if (order.equals("Alphabetical ascending")){
            Collections.sort(artistModelList, new Comparator<ArtistModel>() {
                @Override
                public int compare(ArtistModel artistModel, ArtistModel t1) {
                    return t1.getArtistName().compareToIgnoreCase(artistModel.getArtistName());
                }
            });
        }
        else if (order.equals("Alphabetical descending")){
            Collections.sort(artistModelList, new Comparator<ArtistModel>() {
                @Override
                public int compare(ArtistModel artistModel, ArtistModel t1) {
                    return artistModel.getArtistName().compareTo(t1.getArtistName());
                }
            });
        }
        artistsListAdapter.notifyDataSetChanged();
    }




    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        b.fetchingProgress.setVisibility(View.VISIBLE);
        b.artistsRecyclerView.setVisibility(View.GONE);

        if (id == 4){
            String[] projection ={
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DATE_ADDED
            };

            String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
            return new CursorLoader(getContext(), MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection,
                    null, null);

        }
        else if (id == ARTIST_LOADER_ID){
            String [] projection = {
                    MediaStore.Audio.Artists.ARTIST,
                    MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
            };
            return new CursorLoader(getContext(),MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    projection, null, null, null);

        }
        else{
            return null;
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == ALL_SONGS_LOADER_ID){
            allSongsList.clear();
            while(cursor.moveToNext()){
                AudioModel songData = new AudioModel(cursor.getString(1),
                        cursor.getString(0),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getString(8)
                );

                if (new File(songData.getPath()).exists()){
                    allSongsList.add(songData);
                }
            }
        }
        else if (loader.getId() == ARTIST_LOADER_ID){
            artistModelList.clear();
            while(cursor.moveToNext()){
                ArtistModel artistModel = new ArtistModel(cursor.getString(0), cursor.getString(1),getSampleImagePath(cursor.getString(0)));
                artistModelList.add(artistModel);
            }
            artistsListAdapter.notifyDataSetChanged();

        }

        b.fetchingProgress.setVisibility(View.GONE);
        b.artistsRecyclerView.setVisibility(View.VISIBLE);
        b.txtShowSongs.setText(artistModelList.size() + " Artists");


    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}