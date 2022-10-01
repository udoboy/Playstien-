package com.example.dfimusic.Models;

public class GenreModel {
    String genreName, genreId;
    int numberOfGenreSongs;

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getGenreId() {
        return genreId;
    }

    public void setGenreId(String genreId) {
        this.genreId = genreId;
    }

    public GenreModel(String genreName, String genreId, int numberOfGenreSongs) {
        this.genreName = genreName;
        this.genreId = genreId;
        this.numberOfGenreSongs = numberOfGenreSongs;
    }

    public int getNumberOfGenreSongs() {
        return numberOfGenreSongs;
    }

    public void setNumberOfGenreSongs(int numberOfGenreSongs) {
        this.numberOfGenreSongs = numberOfGenreSongs;
    }
}
