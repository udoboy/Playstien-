package com.example.dfimusic.Models;

public class MapModel {
    String mapName, mapPosition, mapPath;
    int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MapModel(String mapName, String mapPosition, String mapPath, int id ) {
        this.mapName = mapName;
        this.mapPosition = mapPosition;
        this.mapPath = mapPath;
        this.id = id;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getMapPosition() {
        return mapPosition;
    }

    public void setMapPosition(String mapPosition) {
        this.mapPosition = mapPosition;
    }

    public String getMapPath() {
        return mapPath;
    }

    public void setMapPath(String mapPath) {
        this.mapPath = mapPath;
    }
}
