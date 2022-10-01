package com.example.dfimusic.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dfimusic.Adapters.GenreAdapter;
import com.example.dfimusic.Hub;
import com.example.dfimusic.Models.AlbumModel;
import com.example.dfimusic.Models.GenreModel;
import com.example.dfimusic.R;
import com.example.dfimusic.databinding.FragmentGenereBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GenereFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    FragmentGenereBinding b;
    static final int GENRE_LOADER_ID = 6;


    GenreAdapter genreAdapter;
    List<String> genreList;
    List<GenreModel> genreModelList;
    Hub hub;

    @Override
    public void onDestroyView() {
        b.genreRecyclerView.setAdapter(null);
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        b = FragmentGenereBinding.inflate(inflater, container, false);
        genreModelList = new ArrayList<>();
        genreList = new ArrayList<>();
        genreAdapter = new GenreAdapter(genreModelList, getActivity());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        b.genreRecyclerView.setLayoutManager(gridLayoutManager);
        b.genreRecyclerView.setAdapter(genreAdapter);
        hub = new ViewModelProvider(getActivity()).get(Hub.class);

        //getGenreList();

        Observer<Integer> sortChangeObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                setListOrder(getContext().getSharedPreferences("genre_sort", Context.MODE_PRIVATE).getString("genre_sort", "none"));
            }
        };

        hub.getSortChangeInteger().observe(getActivity(), sortChangeObserver);

       // return inflater.inflate(R.layout.fragment_genere, container, false);
        return b.getRoot();

    }

    @Override
    public void onStart() {
        getActivity().getSupportLoaderManager().restartLoader(GENRE_LOADER_ID,null, this);
        super.onStart();
    }


    public int getNumberOfGenreSongs(Long genreId){
        Uri uri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId);
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
        int cursorCount = cursor.getCount();
        if (cursor == null){
            cursor.close();
            return 0;
        }
        else{
            cursor.close();
            return cursorCount;
        }

    }

    public void setListOrder(String order){
        if (order.equals("Alphabetical ascending")){
            Collections.sort(genreModelList, new Comparator<GenreModel>() {
                @Override
                public int compare(GenreModel genreModel, GenreModel t1) {
                    if(t1.getGenreName() != null){
                        return t1.getGenreName().compareToIgnoreCase(genreModel.getGenreName());
                    }
                    else{
                        return -1;
                    }

                }
            });
        }
        else if (order.equals("Alphabetical descending")){
           Collections.sort(genreModelList, new Comparator<GenreModel>() {
               @Override
               public int compare(GenreModel genreModel, GenreModel t1) {
                   if (t1.getGenreName() != null){
                       return genreModel.getGenreName().compareToIgnoreCase(t1.getGenreName());
                   }
                   else{
                       return -1;
                   }

               }
           });
        }
       genreAdapter.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        b.fetchingProgress.bringToFront();
        b.genreRecyclerView.setVisibility(View.GONE);
        b.fetchingProgress.setVisibility(View.VISIBLE);
        String[] projection ={
                MediaStore.Audio.Genres.NAME,
                MediaStore.Audio.Genres._ID,

        };
        return new CursorLoader(getContext(),MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, projection,
                null,null,null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        genreModelList.clear();
        if(cursor != null){
            while (cursor.moveToNext()){
                if(cursor.getString(1)!= null){
                    GenreModel genreModel = new GenreModel(cursor.getString(0), cursor.getString(1), getNumberOfGenreSongs(Long.parseLong(cursor.getString(1))));
                    if (genreModel.getGenreName() != null && genreModel.getGenreId() != null && genreModel.getNumberOfGenreSongs()>0){
                        genreModelList.add(genreModel);
                    }

                }
                else{
                    GenreModel genreModel = new GenreModel(cursor.getString(0), cursor.getString(1), 0);
                    if (genreModel.getGenreName() != null && genreModel.getGenreId() != null && genreModel.getNumberOfGenreSongs()>0){
                        genreModelList.add(genreModel);
                    }

                }
            }
            System.out.println("the genre shared prefs" +getContext().getSharedPreferences("genre_sort", Context.MODE_PRIVATE).getString("genre_sort", "none") );
            setListOrder(getContext().getSharedPreferences("genre_sort", Context.MODE_PRIVATE).getString("genre_sort", "none"));
        }
        else{
            Toast.makeText(getActivity(), "No genres found", Toast.LENGTH_SHORT).show();
        }
        genreAdapter.notifyDataSetChanged();

        b.genreRecyclerView.setVisibility(View.VISIBLE);
        b.fetchingProgress.setVisibility(View.GONE);
        b.txtShowSongs.setText(genreModelList.size() + " Genres");



    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}