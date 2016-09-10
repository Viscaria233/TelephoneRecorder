package com.haochen.telephonerecorder.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateUtils;
import android.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Haochen on 2016/6/30.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "com.haochen.telephonerecorder.db";
    private static int DB_VERSION = 1;

    private static DBHelper instance;

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS phone (" +
                "_id TEXT PRIMARY KEY," +
                "name TEXT DEFAULT \'\'," +
                "tel TEXT NOT NULL," +
                "enabled INTEGER DEFAULT 0 CHECK (enabled = 0 OR enabled = 1))");
        db.execSQL("CREATE TABLE IF NOT EXISTS history (" +
                "_id TEXT PRIMARY KEY," +
                "time TEXT NOT NULL," +
                "event TEXT NOT NULL)");

//        String[][] t = {
//                {"xxx", "15927231362"},
//                {"曲洁", "13326699598"},
//                {"费梦嫄", "13871117982"}
//        };
//        for (int i = 0; i < 3; ++i) {
//            db.execSQL("INSERT INTO phone VALUES (?, ?, ?, ?)",
//                    new Object[]{i, t[i][0], t[i][1], 1});
//        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
