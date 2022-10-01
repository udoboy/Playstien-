package com.example.dfimusic.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.hardware.lights.LightState;
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

import com.example.dfimusic.Adapters.AlbumAdapter;
import com.example.dfimusic.Hub;
import com.example.dfimusic.Models.AlbumModel;
import com.example.dfimusic.Models.ArtistModel;
import com.example.dfimusic.R;
import com.example.dfimusic.databinding.FragmentAlbumsBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class AlbumsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    FragmentAlbumsBinding b;
    List<AlbumModel> albumModelList;
    AlbumAdapter albumAdapter;
    static final int ALBUM_LOADER_ID = 2;
    Hub hub;

    @Override
    public void onDestroyView() {
        b.abAlbumsRec.setAdapter(null);
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        b = FragmentAlbumsBinding.inflate(inflater, container, false);

        hub = new ViewModelProvider(getActivity()).get(Hub.class);

        Observer<Integer> sortChangeObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                setListOrder(getContext().getSharedPreferences("albums_sort", Context.MODE_PRIVATE).getString("albums_sort", "none"));
            }
        };

        hub.getSortChangeInteger().observe(getActivity(), sortChangeObserver);


        return b.getRoot();
        //return inflater.inflate(R.layout.fragment_albums, container, false);
    }

    @Override
    public void onStart() {
        getActivity().getSupportLoaderManager().restartLoader(ALBUM_LOADER_ID, null, this);
        super.onStart();
    }

    public void setListOrder(String order) {
        if (order.equals("Alphabetical ascending")) {
            Collections.sort(albumModelList, new Comparator<AlbumModel>() {
                @Override
                public int compare(AlbumModel albumModel, AlbumModel t1) {
                    return t1.getAlbumName().compareToIgnoreCase(albumModel.getAlbumName());
                }
            });
        } else if (order.equals("Alphabetical descending")) {
            Collections.sort(albumModelList, new Comparator<AlbumModel>() {
                @Override
                public int compare(AlbumModel albumModel, AlbumModel t1) {
                    return albumModel.getAlbumName().compareToIgnoreCase(t1.getAlbumName());
                }
            });
        }
        albumAdapter.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        b.abAlbumsRec.setVisibility(View.GONE);
        b.abfetchingProgress.setVisibility(View.VISIBLE);
        b.abAlbumsRec.setVisibility(View.GONE);
        String[] projection = {
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.ALBUM_ART,
                MediaStore.Audio.Albums._ID

        };
        return new CursorLoader(getContext(), MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        System.out.println("albums loader finished");
        albumModelList = new ArrayList<>();
        albumModelList.clear();
        while (cursor.moveToNext()) {
            AlbumModel albumModel = new AlbumModel(
                    cursor.getString(2),
                    cursor.getString(1),
                    cursor.getString(0),
                    cursor.getString(3),
                    cursor.getString(4)
            );
            albumModelList.add(albumModel);
        }

        albumAdapter = new AlbumAdapter(albumModelList, getActivity());
        b.abAlbumsRec.setHasFixedSize(true);
        b.abAlbumsRec.setLayoutManager(new LinearLayoutManager(getActivity()));
        b.abAlbumsRec.setAdapter(albumAdapter);
       // albumAdapter.notifyDataSetChanged();
        b.abAlbumsRec.setVisibility(View.VISIBLE);

        setListOrder(getContext().getSharedPreferences("albums_sort", Context.MODE_PRIVATE).getString("albums_sort", "none"));
        b.abfetchingProgress.setVisibility(View.GONE);
        b.abAlbumsRec.setVisibility(View.VISIBLE);
        b.txtShowSongs.setText(albumModelList.size() + " Albums");

    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }



}