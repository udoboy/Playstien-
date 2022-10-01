package com.example.dfimusic.Fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
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

import com.example.dfimusic.Adapters.SongsListAdapter;
import com.example.dfimusic.Hub;
import com.example.dfimusic.Models.AudioModel;
import com.example.dfimusic.databinding.FragmentAllSongsBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;


public class AllSongsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    FragmentAllSongsBinding b;
    static SongsListAdapter songsListAdapter;
    ArrayList<AudioModel> songsList;
    Hub hub;
    static  final int LOADER_ID = 001;

    @Override
    public void onDestroyView() {
        b.fssongsRec.setAdapter(null);
        songsListAdapter =null;
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        b = FragmentAllSongsBinding.inflate(inflater, container, false);

        hub = new ViewModelProvider(getActivity()).get(Hub.class);

        b.fssongsRec.setVerticalScrollBarEnabled(true);

        // return inflater.inflate(R.layout.fragment_all_songs, container, false);
        return b.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        getActivity().getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
        super.onStart();
    }



    public void setListOrder(String order){
        if (order.equals("Alphabetical ascending")){
            Collections.sort(songsList, new Comparator<AudioModel>() {
                @Override
                public int compare(AudioModel audioModel, AudioModel t1) {
                    return t1.getTitle().compareToIgnoreCase(audioModel.getTitle());
                }
            });
        }
        else if (order.equals("Alphabetical descending")){
            Collections.sort(songsList, new Comparator<AudioModel>() {
                @Override
                public int compare(AudioModel audioModel, AudioModel t1) {
                    return audioModel.getTitle().compareToIgnoreCase(t1.getTitle());
                }
            });
        }
        else if(order.equals("Date added ascending")){
            System.out.println("date added ascending");
            Collections.sort(songsList, new Comparator<AudioModel>() {
                @Override
                public int compare(AudioModel audioModel, AudioModel t1) {
                    return Integer.valueOf(audioModel.getDateAdded()).compareTo(Integer.valueOf(t1.getDateAdded()));
                }
            });
        }
        else if (order.equals("Date added descending")){
            Collections.sort(songsList, new Comparator<AudioModel>() {
                @Override
                public int compare(AudioModel audioModel, AudioModel t1) {
                    return Integer.valueOf(t1.getDateAdded()).compareTo(Integer.valueOf(audioModel.getDateAdded()));
                }
            });
        }

        songsListAdapter.notifyDataSetChanged();
   }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        b.fssongsRec.setVisibility(View.GONE);
        b.fetchingProgress.setVisibility(View.VISIBLE);
            String[] projection = {
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
        System.out.println("laoder created");
        //onLoadFinished(null, cursor);
        return new CursorLoader(getContext(),MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection, selection,null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        songsList = new ArrayList<>();
        songsList.clear();
    while (data.moveToNext()){
            AudioModel songData = new AudioModel(data.getString(1), data.getString(0), data.getString(2),
        data.getString(3),
        data.getString(4),
        data.getString(5),
        data.getString(6),
        data.getString(7),
        data.getString(8));
            songsList.add(songData);
        }
        songsListAdapter = new SongsListAdapter(songsList, getActivity());
        b.fssongsRec.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        b.fssongsRec.setLayoutManager(linearLayoutManager);
        b.fssongsRec.setAdapter(songsListAdapter);
        b.fssongsRec.setVerticalScrollBarEnabled(true);
        songsListAdapter.notifyDataSetChanged();
        b.txtShowSongs.setText(songsList.size() +" Songs");

    setListOrder(getContext().getSharedPreferences("all_songs_sort", Context.MODE_PRIVATE).getString("all_songs_sort", "none"));
    b.fetchingProgress.setVisibility(View.GONE);
    b.fssongsRec.setVisibility(View.VISIBLE);
        Observer<Integer> sortChangeObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                setListOrder(getContext().getSharedPreferences("all_songs_sort", Context.MODE_PRIVATE).getString("all_songs_sort", "none"));
            }
        };

        hub.getSortChangeInteger().observe(getActivity(), sortChangeObserver);
        Observer<HashMap<String, String>> currentHashObserver = new Observer<HashMap<String, String>>() {
            @Override
            public void onChanged(HashMap<String, String> stringStringHashMap) {
                songsListAdapter.notifyDataSetChanged();
            }
        };
        Hub.getCurrentHashData().observe((LifecycleOwner) getContext(), currentHashObserver);


    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

}







