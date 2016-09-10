package com.haochen.telephonerecorder.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Calendar;

/**
 * Created by Haochen on 2016/7/2.
 */
public class IdBuilder {
    public static String newId(String tableName) {
        Calendar calendar = Calendar.getInstance();
        String time = "" + calendar.getTimeInMillis();
        int n = 0;
        String id = time + n;
        DBHelper helper = DBHelper.getInstance(null);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE _id = ?",
                new String[]{id});
        while (cursor.moveToNext()) {
            ++n;
            id = time + n;
            cursor = db.rawQuery("SELECT * FROM " + tableName + " WHERE _id = ?",
                    new String[]{id});
        }
        db.close();
        return id;
    }
}
