package com.haochen.telephonerecorder.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.callback.OnEditCommitListener;
import com.haochen.telephonerecorder.common.Config;

import java.io.File;

/**
 * Created by Haochen on 2016/7/12.
 */
public class EditRecordFragment extends EditFragment {

    private EditText name;

    public EditRecordFragment(Bundle data) {
        super(data);
    }

    @Override
    public boolean checkAvailable(String origin) {
        return true;
    }

    @Override
    public Bundle resultBundle() {
        data.putString("new_name", name.getText().toString());
        return data;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.edit_record, null);
        name = (EditText) view.findViewById(R.id.editText_name);
        name.setText(data.getString("name"));
        return view;
    }
}
