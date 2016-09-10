package com.haochen.telephonerecorder.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.haochen.telephonerecorder.AddActivity;
import com.haochen.telephonerecorder.ContactActivity;
import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.adapter.ContactAdapter;
import com.haochen.telephonerecorder.adapter.MyAdapter;
import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.util.DBHelper;
import com.haochen.telephonerecorder.util.IdBuilder;

import java.io.Serializable;
import java.sql.ResultSet;

/**
 * Created by Haochen on 2016/6/30.
 */
public class PhoneFragment extends MyFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_phone, container, false);
        view.findViewById(R.id.fab_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ContactActivity.class);
                startActivityForResult(intent, Config.Request.CONTACT);
            }
        });
        view.findViewById(R.id.fab_add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddActivity.class);
                startActivityForResult(intent, Config.Request.ADD);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Config.Request.ADD:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    String name = bundle.getString("name");
                    String tel = bundle.getString("tel");
                    String id = IdBuilder.newId("phone");
                    DBHelper helper = DBHelper.getInstance(null);
                    SQLiteDatabase db = helper.getWritableDatabase();
                    db.execSQL("INSERT INTO phone VALUES (?, ?, ?, ?)",
                            new Object[]{id, name, tel, true});
                    db.close();
                    Config.Changed.PHONE = true;
                    adapter.checkAndUpdate();
                }
                break;
            case Config.Request.CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Object[] contactObj = (Object[]) bundle.getSerializable("contacts");
                    Object[] idObj = (Object[]) bundle.getSerializable("ids");
                    ContactAdapter.Contact[] contacts = new ContactAdapter.Contact[idObj.length];
                    String[] ids = new String[idObj.length];
                    for (int i = 0; i < contactObj.length; ++i) {
                        contacts[i] = (ContactAdapter.Contact) contactObj[i];
                        ids[i] = (String) idObj[i];
                    }

                    DBHelper helper = DBHelper.getInstance(null);
                    SQLiteDatabase db = helper.getWritableDatabase();
                    for (int i = 0; i < contacts.length; ++i) {
                        db.execSQL("INSERT INTO phone VALUES (?, ?, ?, ?)",
                                new Object[]{
                                        ids[i],
                                        contacts[i].getName(),
                                        contacts[i].getTel(),
                                        true
                                });
                    }
                    db.close();
                    Config.Changed.PHONE = true;
                    adapter.checkAndUpdate();
                }
                break;
        }
    }
}
