package com.example.dfimusic.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.dfimusic.Fragments.AlbumsFragment;
import com.example.dfimusic.Fragments.AllSongsFragment;
import com.example.dfimusic.Fragments.ArtistFragment;
import com.example.dfimusic.Fragments.GenereFragment;


public class OptionsFragmentAdapter extends FragmentPagerAdapter {
    public OptionsFragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
       switch(position){
           case 0:
               return new AllSongsFragment();
           case 1:
               return new ArtistFragment();
           case 2:
               return  new AlbumsFragment();
           case 3:
               return new GenereFragment();
           default:
               return new AllSongsFragment();
       }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title = null;
        if (position ==0){
            title = "Songs";
        }
        if (position ==1){
            title = "Artists";
        }
        if (position ==2){
            title = "Albums";
        }
        if (position == 3){
            title = "Genre";
        }
       // return super.getPageTitle(position);
        return title;
    }
}
