package com.haochen.telephonerecorder.fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.util.DBHelper;

/**
 * Created by Haochen on 2016/7/12.
 */
public class EditPhoneFragment extends EditFragment {

    private EditText name;
    private EditText tel;

    @Override
    public boolean checkAvailable(String origin) {
        String t = tel.getText().toString();
        if ("".equals(t)) {
            Snackbar.make(tel, "Tel can not be empty.", Snackbar.LENGTH_LONG).show();
            return false;
        }
        DBHelper helper = DBHelper.getInstance(null);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM phone WHERE tel <> ? AND tel = ?",
                new String[]{origin, t});
        boolean exist = cursor.moveToNext();
        cursor.close();
        db.close();
        if (exist) {
            Snackbar.make(tel, "This tel is already exists.", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    @Override
    public Bundle resultBundle() {
        data.putString("new_name", name.getText().toString());
        data.putString("new_tel", tel.getText().toString());
        return data;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_phone, null);
        name = (EditText) view.findViewById(R.id.editText_name);
        tel = (EditText) view.findViewById(R.id.editText_tel);
        name.setText(data.getString("name"));
        tel.setText(data.getString("tel"));
        return view;
    }
}
