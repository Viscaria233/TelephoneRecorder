package com.haochen.telephonerecorder.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.common.Phone;
import com.haochen.telephonerecorder.fragment.BaseBatchFragment;
import com.haochen.telephonerecorder.struct.CheckableItem;
import com.haochen.telephonerecorder.util.DBHelper;

import java.util.ArrayList;

/**
 * Created by Haochen on 2016/6/30.
 */
public class PhoneAdapter extends DBAdapter<Phone> {

    public PhoneAdapter(Context context) {
        super(context, new ArrayList<CheckableItem<Phone>>(), "phone");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_phone, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            viewHolder.name = (TextView) convertView.findViewById(R.id.textView_name);
            viewHolder.tel = (TextView) convertView.findViewById(R.id.textView_tel);
            viewHolder.call = (ImageButton) convertView.findViewById(R.id.imageButton_call);
            viewHolder.enabled = (Switch) convertView.findViewById(R.id.switch_enabled);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final CheckableItem<Phone> item = data.get(position);
        final Phone phone = item.getValue();
        viewHolder.name.setText(phone.getName());
        viewHolder.tel.setText(phone.getTel());
        viewHolder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.getTel()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        viewHolder.enabled.setChecked(phone.isEnabled());
        if (Config.BATCH_MODE) {
            viewHolder.checkBox.setVisibility(View.VISIBLE);
            viewHolder.checkBox.setChecked(item.isChecked());
            viewHolder.enabled.setEnabled(false);
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
            viewHolder.enabled.setEnabled(true);
            viewHolder.enabled.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DBHelper helper = DBHelper.getInstance(null);
                    SQLiteDatabase db = helper.getWritableDatabase();
                    db.execSQL("UPDATE phone SET enabled = ? WHERE _id = ?",
                            new Object[]{((Switch)v).isChecked() ? 1 : 0, phone.getId()});
                    db.close();
                    Config.Changed.PHONE = true;
                }
            });
        }
        return convertView;
    }

    @Override
    public BaseBatchFragment createBatchFragment() {
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
        CheckableItem item = data.get(position);
        db.execSQL("DELETE FROM " + tableName + " WHERE _id = ?",
                new Object[]{((Phone) item.getValue()).getId()});
    }

    @Override
    protected boolean isDataChanged() {
        return Config.Changed.PHONE;
    }

    @Override
    protected void getDataSet() {
        data.clear();
        DBHelper helper = DBHelper.getInstance(null);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT _id, name, tel, enabled FROM " + tableName, null);
        Phone phone;
        while (cursor.moveToNext()) {
            phone = new Phone();
            phone.setId(cursor.getString(0));
            phone.setName(cursor.getString(1));
            phone.setTel(cursor.getString(2));
            phone.setEnabled(cursor.getInt(3) == 1);
            data.add(new CheckableItem<>(phone, false));
        }
        db.close();
    }

    @Override
    protected void resetChangeFlag() {
        Config.Changed.PHONE = false;
    }

    @Override
    public Bundle getDataBundle() {
        if (checkedNumber != 1) {
            return null;
        }
        Bundle bundle = new Bundle();
        int index = getCheckedIndexArray()[0];
        Phone phone = data.get(index).getValue();
        bundle.putInt("type", Config.EditType.PHONE);
        bundle.putString("id", phone.getId());
        bundle.putString("name", phone.getName());
        bundle.putString("tel", phone.getTel());
        return bundle;
    }

    private static class ViewHolder {
        CheckBox checkBox;
        TextView name;
        TextView tel;
        ImageButton call;
        Switch enabled;
    }

}
