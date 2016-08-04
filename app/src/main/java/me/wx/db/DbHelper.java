package me.wx.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ein on 2016/8/4.
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "download.db";
    private static final int VERSION = 1;
    private static final String TABLE_CREATE = "create table thread_info(_id integer primary key," +
            "thread_id integer,url text,start integer,end integer,finished integer)";
    private static final String TABLE_DROP = " drop table if exists thread_info";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TABLE_DROP);
        db.execSQL(TABLE_CREATE);
    }
}
