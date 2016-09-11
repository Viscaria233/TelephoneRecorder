package com.haochen.telephonerecorder.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.common.History;
import com.haochen.telephonerecorder.fragment.BaseBatchFragment;
import com.haochen.telephonerecorder.struct.CheckableItem;
import com.haochen.telephonerecorder.util.DBHelper;

import java.util.ArrayList;

/**
 * Created by Haochen on 2016/6/30.
 */
public class HistoryAdapter extends DBAdapter<History> {

    public HistoryAdapter(Context context) {
        super(context, new ArrayList<CheckableItem<History>>(), "history");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_history, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            viewHolder.time = (TextView) convertView.findViewById(R.id.textView_time);
            viewHolder.event = (TextView) convertView.findViewById(R.id.textView_event);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final CheckableItem<History> item = data.get(position);
        History history = item.getValue();
        viewHolder.time.setText(history.getTime());
        viewHolder.event.setText(history.getEvent());
        if (Config.BATCH_MODE) {
            viewHolder.checkBox.setVisibility(CheckBox.VISIBLE);
            viewHolder.checkBox.setChecked(item.isChecked());
            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean isChecked = ((CheckBox) v).isChecked();
                    item.setChecked(isChecked);
                    if (isChecked) {
                        ++checkedNumber;
                    } else {
                        --checkedNumber;
                    }
                    onCheckedNumberChange();
                }
            });
        } else {
            viewHolder.checkBox.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    @Override
    public BaseBatchFragment createBatchFragment() {
        return new MyBatchFragment() {
            @Override
            public void onStart() {
                super.onStart();
                edit.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCheckedNumberChange(int checkedNumber) {
                switch (checkedNumber) {
                    case 0:
                        delete.setVisibility(View.INVISIBLE);
                        break;
                    default:
                        delete.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };
    }

    @Override
    protected void physicalDelete(int position) {
        CheckableItem item = data.get(position);
        db.execSQL("DELETE FROM " + tableName + " WHERE _id = ?",
                new Object[]{((History) item.getValue()).getId()});
    }

    @Override
    protected boolean isDataChanged() {
        return Config.Changed.HISTORY;
    }

    @Override
    protected void getDataSet() {
        data.clear();
        DBHelper helper = DBHelper.getInstance(null);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id, time, event FROM " + tableName, null);
        History history;
        while (cursor.moveToNext()) {
            history = new History();
            history.setId(cursor.getString(0));
            history.setTime(cursor.getString(1));
            history.setEvent(cursor.getString(2));
            data.add(new CheckableItem<>(history, false));
        }
        db.close();
    }

    @Override
    protected void resetChangeFlag() {
        Config.Changed.HISTORY = false;
    }

    @Override
    public Bundle getDataBundle() {
        return null;
    }

    private static class ViewHolder {
        CheckBox checkBox;
        TextView time;
        TextView event;
    }

}
