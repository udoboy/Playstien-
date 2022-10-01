package com.example.dfimusic.LocalDAtabases;

import static java.lang.System.out;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;


public class
PlayListDatabase extends SQLiteOpenHelper {
    public static final String PLAY_LIST_NAME_TABLE = "play_list_name_table";
    public static final String PLAY_LIST_NAME = "play_list_name";
    public static final String PLAY_LIST_NAME_ID = "play_list_name_id";
    public static final String PLAY_LIST_TABLE = "play_list_table";
    public static final String PLAY_LIST_ITEM_PATH = "play_list_item_path";
    public static final String PLAY_LIST_ITEM_ID = "play_list_item_id";


    public PlayListDatabase(@Nullable Context context) {
        super(context, "playlists", null, 3);
    }

    //String createFavouriteTable = "CREATE TABLE "+ FAVOURITE_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + FAVOURITE_PATH + " TEXT)";
    String createPlaylistNameTable = "CREATE TABLE " + PLAY_LIST_NAME_TABLE + "(" + PLAY_LIST_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PLAY_LIST_NAME + " TEXT)";
    String createPlayListTable = "CREATE TABLE " + PLAY_LIST_TABLE + "(" + PLAY_LIST_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PLAY_LIST_NAME + " TEXT," + PLAY_LIST_ITEM_PATH + " TEXT )";
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(createPlaylistNameTable);
        sqLiteDatabase.execSQL(createPlayListTable);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        out.println("upgrade was called");

    }



    public boolean addPlaylistName(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PLAY_LIST_NAME , name);

        long insert  = db.insert(PLAY_LIST_NAME_TABLE, null, contentValues);
        if (insert == -1){
            return false;
        }
        else{
            return true;
        }

    }

    public Cursor getPlayListNames(){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("SELECT * FROM "+ PLAY_LIST_NAME_TABLE,null );
        return cursor;
    }

    public boolean addPlayListPathItem(String path, String playListTableName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(PLAY_LIST_NAME, playListTableName );
        contentValues.put(PLAY_LIST_ITEM_PATH, path);

        long insert = db.insert(PLAY_LIST_TABLE, null, contentValues);
        if (insert == -1){
            return false;
        }
        else{
            return true;
        }
    }

    public Cursor getPlayListItems(String playListTableName){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(" SELECT * FROM " + PLAY_LIST_TABLE + " WHERE play_list_name= '"+ playListTableName +"'", null);
        return cursor;

    }

    public boolean removeFromPlaylist(String path, String playListTableName){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectStatement = "SELECT * FROM " + PLAY_LIST_TABLE + " WHERE play_list_item_path =?";
        Cursor cursor = db.rawQuery(selectStatement, new String[]{path});

        if (cursor.getCount() >0){
            long result = db.delete(PLAY_LIST_TABLE, "play_list_item_path =? AND play_list_name =?", new String[]{path, playListTableName});
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


    public boolean deletePlaylist(String playListName){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectStatement = "SELECT * FROM " + PLAY_LIST_NAME_TABLE + " WHERE play_list_name =? ";
        Cursor cursor = db.rawQuery(selectStatement, new String[]{playListName});

        if (cursor.getCount() >0){
            long result = db.delete(PLAY_LIST_NAME_TABLE, "play_list_name =?", new String[]{playListName});
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

    public boolean deleteAllPlaylistTableItems(String playListName){
        SQLiteDatabase db = this.getWritableDatabase();
        String selectStatement = "SELECT * FROM " + PLAY_LIST_TABLE + " WHERE play_list_name =?";
        Cursor cursor = db.rawQuery(selectStatement, new String[]{playListName});

        if (cursor.getCount()>0){
            long result = db.delete(PLAY_LIST_TABLE, "play_list_name =?", new String[]{playListName});
            if (result == -1){
                return false;
            }
            else{
                boolean deletedall = deletePlaylist(playListName);
                if (deletedall){
                    out.println("playlist deleted");
                }
                else{
                    out.println("playlist not deleted");
                }
                return true;
            }
        }
        else{
           boolean deleteAll = deletePlaylist(playListName);
           if (deleteAll){
               return true;
           }
           else{
               return false;
           }

        }

    }


}
