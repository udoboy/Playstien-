package com.example.dfimusic.Models;

public class AlbumModel {
    String artist, count, albumName, albumArt, albumId;

    public AlbumModel(String artist, String count, String albumName, String albumArt, String albumId) {
        this.artist = artist;
        this.count = count;
        this.albumName = albumName;
        this.albumArt = albumArt;
        this.albumId = albumId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }
}
