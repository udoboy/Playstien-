package com.example.dfimusic.Models;

import android.os.Parcelable;

import java.io.Serializable;

public class AudioModel  implements Serializable{
    String path, title, duration, artist, displayName, album, albumId, songId, dateAdded;


    public AudioModel(String path, String title, String duration, String artist, String displayName, String album, String albumId, String songId, String dateAdded ) {
        this.path = path;
        this.title = title;
        this.duration = duration;
        this.artist = artist;
        this.displayName = displayName;
        this.album = album;
        this.albumId = albumId;
        this.songId = songId;
        this.dateAdded = dateAdded;



    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
}
