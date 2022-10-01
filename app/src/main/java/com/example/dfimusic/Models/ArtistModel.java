package com.example.dfimusic.Models;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

public class ArtistModel {
    String artistName, numberOfSongs, sampleImagePath;


    public String getSampleImagePath() {
        return sampleImagePath;
    }

    public void setSampleImagePath(String sampleImagePath) {
        this.sampleImagePath = sampleImagePath;
    }

    public ArtistModel(String artistName, String numberOfSongs, String sampleImagePath) {
        this.artistName = artistName;
        this.numberOfSongs = numberOfSongs;
        this.sampleImagePath = sampleImagePath;


    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getNumberOfSongs() {
        return numberOfSongs;
    }

    public void setNumberOfSongs(String numberOfSongs) {
        this.numberOfSongs = numberOfSongs;
    }


}
