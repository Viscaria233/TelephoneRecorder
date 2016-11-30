package com.haochen.telephonerecorder.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.common.Record;
import com.haochen.telephonerecorder.fragment.BaseBatchFragment;
import com.haochen.telephonerecorder.common.CheckableItem;
import com.haochen.telephonerecorder.util.AudioFileFilter;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Haochen on 2016/6/30.
 */
public class RecordAdapter extends MyAdapter<Record> {

    public RecordAdapter(Context context) {
        super(context, new ArrayList<CheckableItem<Record>>());
        getDataSet();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_record, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            viewHolder.name = (TextView) convertView.findViewById(R.id.textView_name);
            viewHolder.length = (TextView) convertView.findViewById(R.id.textView_length);
            viewHolder.size = (TextView) convertView.findViewById(R.id.textView_size);
            viewHolder.modified = (TextView) convertView.findViewById(R.id.textView_last_modified);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final CheckableItem<Record> item = data.get(position);
        final Record record = item.getValue();
        viewHolder.name.setText(record.getFile().getName());
        viewHolder.length.setText(record.getLength());
        viewHolder.size.setText(record.getSize());
        viewHolder.modified.setText(record.getModified());
        if (batchMode) {
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
    public BaseBatchFragment getBatchFragment() {
        return new MyBatchFragment() {
            @Override
            public void onCheckedNumberChange(int checkedNumber) {
                switch (checkedNumber) {
                    case 0:
                        edit.setVisibility(View.INVISIBLE);
                        delete.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        edit.setVisibility(View.VISIBLE);
                        delete.setVisibility(View.VISIBLE);
                        break;
                    default:
                        edit.setVisibility(View.INVISIBLE);
                        delete.setVisibility(View.VISIBLE);
                        break;
                }
            }
        };
    }

    @Override
    protected void physicalDelete(int position) {
        if (position < 0 || position >= data.size()) {
            return;
        }
        data.get(position).getValue().getFile().delete();
    }

    @Override
    protected boolean isDataChanged() {
        return Config.Changed.RECORD;
    }

    @Override
    protected void getDataSet() {
        data.clear();
        File path = new File(Config.Storage.RECORD_PATH);
        File[] files = path.listFiles(new AudioFileFilter());
        if (files != null) {
            try {
                MediaPlayer player;
                for (File file : files) {
                    player = new MediaPlayer();
                    player.setDataSource(null, Uri.parse(file.getAbsolutePath()));
                    player.prepare();
                    int duration = player.getDuration() / 1000;
                    player.release();
                    long time = file.lastModified();
                    Record record = new Record();
                    record.setFile(file);
                    record.setLength(String.format("%02d:%02d", duration / 60, duration % 60));
                    record.setSize(String.format("%.2fMB", file.length() / 1024.0 / 1024));
                    record.setModified(new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date(time)));
                    data.add(new CheckableItem<>(record, false));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void resetChangeFlag() {
        Config.Changed.RECORD = false;
    }

    @Override
    public Bundle getDataBundle() {
        if (checkedNumber != 1) {
            return null;
        }
        Bundle bundle = new Bundle();
        int index = getCheckedIndexArray()[0];
        Record record = data.get(index).getValue();
        bundle.putInt("type", Config.EditType.RECORD);
        bundle.putSerializable("old_file", record.getFile());
        return bundle;
    }

    private static class ViewHolder {
        CheckBox checkBox;
        TextView name;
        TextView length;
        TextView size;
        TextView modified;
    }

}
