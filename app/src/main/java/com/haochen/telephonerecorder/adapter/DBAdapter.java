package com.haochen.telephonerecorder.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.haochen.telephonerecorder.struct.CheckableItem;
import com.haochen.telephonerecorder.util.DBHelper;

import java.util.List;

/**
 * Created by Haochen on 2016/6/30.
 */
public abstract class DBAdapter<T> extends MyAdapter<T> {

    protected String tableName;
    protected SQLiteDatabase db;

    public DBAdapter(Context context, List<CheckableItem<T>> list, String tableName) {
        super(context, list);
        this.tableName = tableName;
        getDataSet();
    }

    @Override
    protected void beginDelete() {
        db = DBHelper.getInstance(null).getWritableDatabase();
    }

    @Override
    protected void endDelete() {
        db.close();
        db = null;
    }

}
