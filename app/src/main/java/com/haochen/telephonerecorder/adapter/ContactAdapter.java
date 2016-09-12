package com.haochen.telephonerecorder.adapter;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.common.Contact;
import com.haochen.telephonerecorder.fragment.BaseBatchFragment;
import com.haochen.telephonerecorder.struct.CheckableItem;
import com.haochen.telephonerecorder.util.DBHelper;
import com.haochen.telephonerecorder.util.IdBuilder;

import java.util.ArrayList;

/**
 * Created by Haochen on 2016/7/13.
 */
public class ContactAdapter extends MyAdapter<Contact> {

    private String[] tels;

    public ContactAdapter(Context context) {
        super(context, new ArrayList<CheckableItem<Contact>>());
        getDataSet();

        DBHelper helper = DBHelper.getInstance(null);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT tel FROM phone", null);
        tels = new String[cursor.getCount()];
        int i = 0;
        while (cursor.moveToNext()) {
            tels[i++] = cursor.getString(0);
        }
        db.close();
    }

    @Override
    public BaseBatchFragment getBatchFragment() {
        return null;
    }

    @Override
    protected void physicalDelete(int position) {
    }

    @Override
    protected boolean isDataChanged() {
        return false;
    }

    @Override
    protected void resetChangeFlag() {
    }


    @Override
    protected void getDataSet() {
        data.clear();
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] {
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                }, null, null, null);
        if (cursor == null) {
            return;
        }
        Contact contact;
        while (cursor.moveToNext()) {
            contact = new Contact();
            contact.setTel(cursor.getString(0));
            contact.setName(cursor.getString(1));
            data.add(new CheckableItem<>(contact, false));
        }
        cursor.close();
    }

    @Override
    public Bundle getDataBundle() {
        int[] index = getCheckedIndexArray();
        Contact[] contacts = new Contact[index.length];
        String[] ids = new String[index.length];
        for (int i = 0; i < index.length; ++i) {
            contacts[i] = ((CheckableItem<Contact>) getItem(index[i])).getValue();
            ids[i] = IdBuilder.newId("phone");
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable("contacts", contacts);
        bundle.putSerializable("ids", ids);
        return bundle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_contact, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
            viewHolder.name = (TextView) convertView.findViewById(R.id.textView_name);
            viewHolder.tel = (TextView) convertView.findViewById(R.id.textView_tel);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final CheckableItem<Contact> item = data.get(position);
        final Contact contact = item.getValue();
        final View view = convertView;
        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean exists = false;
                for (String s : tels) {
                    if (s.endsWith(contact.getTel()) || contact.getTel().endsWith(s)) {
                        exists = true;
                        break;
                    }
                }

                if (exists) {
                    Snackbar.make(view, "This tel is already exists.", Snackbar.LENGTH_LONG)
                            .show();
                    ((CheckBox) v).setChecked(false);
                } else {
                    boolean isChecked = ((CheckBox) v).isChecked();
                    item.setChecked(isChecked);
                    if (isChecked) {
                        ++checkedNumber;
                    } else {
                        --checkedNumber;
                    }
                    onCheckedNumberChange();
                }
            }
        });
        viewHolder.checkBox.setChecked(item.isChecked());
        viewHolder.name.setText(contact.getName());
        viewHolder.tel.setText(contact.getTel());
        return convertView;
    }

    private static class ViewHolder {
        CheckBox checkBox;
        TextView name;
        TextView tel;
    }

}
