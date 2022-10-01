package com.example.dfimusic.LocalDAtabases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class PlayMode extends SQLiteOpenHelper {
    public static final String MODE_TABLE = "mode_table";
    public static final String MODE_NAME = "mode_name";
    public static final String ID = "id";
    public PlayMode(@Nullable Context context) {
        super(context, "modeDb", null, 1);
    }
    //String createTableStatement = "CREATE TABLE " + GOALS_TABLE + "(" + NAME_OF_GOAL + " TEXT PRIMARY KEY)";
    String createModeTable = "CREATE TABLE "+ MODE_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + MODE_NAME + " TEXT)";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(createModeTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    public boolean updateMode(String mode){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(MODE_NAME, mode);

        long update = db.update(MODE_TABLE, cv, "id=?", new String[]{"1"});
        if (update == -1){
            return false;
        }
        else {
            return true;
        }


    }
    public boolean createMode(String mode){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(MODE_NAME, mode);
        long insert = database.insert(MODE_TABLE, null, cv);
        if (insert == -1){
            return false;
        }
        else{
            return true;
        }
    }



    public Cursor getModes(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + MODE_TABLE, null );
        return  cursor;

    }
}
