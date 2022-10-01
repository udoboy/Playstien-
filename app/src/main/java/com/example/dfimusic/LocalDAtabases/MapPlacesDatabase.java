package com.example.dfimusic.LocalDAtabases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MapPlacesDatabase extends SQLiteOpenHelper {
    public static final String MAP_PLACES_TABLE = "mapPlacesTable";
    public static  final String ID = "id";
    public static final String MAP_SONG_PATH = "mapPath";
    public static final String MAP_POSITION= "mapPosition";
    public static final String MAP_NAME = "mapName";

  //  String createPlayListTable = "CREATE TABLE " + PLAY_LIST_TABLE + "(" + PLAY_LIST_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PLAY_LIST_NAME + " TEXT," + PLAY_LIST_ITEM_PATH + " TEXT )";
    String createMapTable = "CREATE TABLE " + MAP_PLACES_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + MAP_SONG_PATH + " TEXT, " + MAP_POSITION + " TEXT, " + MAP_NAME + " TEXT )";
    public MapPlacesDatabase(@Nullable Context context) {
        super(context, "mapPlaces", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(createMapTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean setMapPosition(String path, String position, String mapName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MAP_SONG_PATH , path);
        contentValues.put(MAP_POSITION, position);
        contentValues.put(MAP_NAME, mapName);

       long insert = db.insert(MAP_PLACES_TABLE, null, contentValues);
       if (insert ==-1){
           return false;
       }
       else{
           return true;
       }
    }

    public Cursor getMapPositions(String path){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ MAP_PLACES_TABLE + " WHERE mapPath='"+ path +"'", null);
        return cursor;
    }

  public boolean deletePosition(String id){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        String selectStatement = "SELECT * FROM "+ MAP_PLACES_TABLE + " WHERE "+ "id=?";
        Cursor cursor = sqLiteDatabase.rawQuery(selectStatement, new String[]{id});
        if (cursor.getCount()>0){
            long delete = sqLiteDatabase.delete(MAP_PLACES_TABLE, "id=?", new String[]{id});
            if (delete == -1){
                return false;
            }
            else{
                return true;
            }
        }
        else{
            return false;
        }
  }
}
