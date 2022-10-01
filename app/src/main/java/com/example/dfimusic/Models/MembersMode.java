package com.example.dfimusic.Models;

public class MembersMode {
    String audioId, genreId;

    public MembersMode(String audioId, String genreId) {
        this.audioId = audioId;
        this.genreId = genreId;
    }

    public String getAudioId() {
        return audioId;
    }

    public void setAudioId(String audioId) {
        this.audioId = audioId;
    }

    public String getGenreId() {
        return genreId;
    }

    public void setGenreId(String genreId) {
        this.genreId = genreId;
    }
}
