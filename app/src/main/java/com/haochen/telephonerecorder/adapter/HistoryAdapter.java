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
import com.haochen.telephonerecorder.fragment.BaseBatchFragment;
import com.haochen.telephonerecorder.struct.CheckableItem;
import com.haochen.telephonerecorder.util.DBHelper;

import java.util.ArrayList;

/**
 * Created by Haochen on 2016/6/30.
 */
public class HistoryAdapter extends DBAdapter<HistoryAdapter.History> {

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

        final CheckableItem<History> item = list.get(position);
        History history = item.getValue();
        viewHolder.time.setText(history.time);
        viewHolder.event.setText(history.event);
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
        return new HistoryBatchFragment(context);
    }

    @Override
    protected void physicalDelete(int position) {
        CheckableItem item = list.get(position);
        db.execSQL("DELETE FROM " + tableName + " WHERE _id = ?",
                new Object[]{((History) item.getValue()).id});
    }

    @Override
    protected boolean isDataChanged() {
        return Config.Changed.HISTORY;
    }

    @Override
    protected void getDataSet() {
        list.clear();
        DBHelper helper = DBHelper.getInstance(null);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id, time, event FROM " + tableName, null);
        History history;
        while (cursor.moveToNext()) {
            history = new History();
            history.id = cursor.getString(0);
            history.time = cursor.getString(1);
            history.event = cursor.getString(2);
            list.add(new CheckableItem<History>(history, false));
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

    public static class History {
        String id;
        String time;
        String event;
    }

    public class HistoryBatchFragment extends MyBatchFragment {

        public HistoryBatchFragment(Context context) {
            super(context);
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
    }

}
