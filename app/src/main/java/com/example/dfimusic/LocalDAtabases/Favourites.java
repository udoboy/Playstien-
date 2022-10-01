package com.example.dfimusic.LocalDAtabases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Favourites extends SQLiteOpenHelper {
    public static final String FAVOURITE_TABLE = "favouriteTable";
    public static final String FAVOURITE_PATH = "favoritePath";
    public static final String ID = "id";


    //String createModeTable = "CREATE TABLE "+ MODE_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + MODE_NAME + " TEXT)";
    public Favourites(@Nullable Context context) {
        super(context, "favouriteDb", null, 1);

    }

    String createFavouriteTable = "CREATE TABLE "+ FAVOURITE_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FAVOURITE_PATH + " TEXT)";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(createFavouriteTable);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean addFavourite(String path){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FAVOURITE_PATH, path);

        long insert = db.insert(FAVOURITE_TABLE, null, contentValues);
        if (insert == -1){
            return  false;
        }
        else{
            return true;
        }

    }

    public Cursor getFavourites(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + FAVOURITE_TABLE, null);
        return cursor;

    }

//    public boolean deleteGoal(String name){
//        SQLiteDatabase db = this.getWritableDatabase();
//        String selectStatement = "SELECT * FROM "+ GOALS_TABLE + " WHERE goalName =?";
//        Cursor cursor = db.rawQuery(selectStatement, new String[]{name});
//
//        if (cursor.getCount()>0){
//            long result = db.delete(GOALS_TABLE, "goalName =?", new String[]{name});
//            if (result ==-1){
//                return false;
//            }
//            else{
//                return true;
//            }
//        }else{
//            return false;
//        }
//    }

    public boolean removeFavourite(String path){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectStatement = "SELECT * FROM "+ FAVOURITE_TABLE + " WHERE favoritePath =?";
        Cursor cursor =db.rawQuery(selectStatement, new String[]{path});

        if (cursor.getCount() >0){
            long result = db.delete(FAVOURITE_TABLE, "favoritePath =?", new String[]{path});
            if (result == -1){
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
