package com.haochen.telephonerecorder.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.ListView;
import android.widget.Spinner;

import com.haochen.telephonerecorder.EditActivity;
import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.adapter.MyAdapter;
import com.haochen.telephonerecorder.callback.OnDeleteCompleteListener;
import com.haochen.telephonerecorder.callback.OnEditClickListener;
import com.haochen.telephonerecorder.callback.OnEditCommitListener;
import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.util.DBHelper;

import java.io.File;

/**
 * Created by Haochen on 2016/7/10.
 */
public abstract class MyFragment extends Fragment
        implements OnEditClickListener, OnEditCommitListener, OnDeleteCompleteListener {

    protected Spinner spinner;
    protected ListView listView;
    protected MyAdapter adapter;

    public boolean enterBatchMode() {
        if (isBatchMode()) {
            return false;
        }
        BaseBatchFragment fragment = adapter.getBatchFragment();
        fragment.setOnEditClickListener(this);
        fragment.setOnEditCommitListener(this);
        fragment.setOnDeleteCompleteListener(this);
        adapter.setOnCheckedNumberChangeListener(fragment);
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.button_bar, fragment);
        ft.commit();
        setBatchMode(true);
        adapter.notifyDataSetChanged();
        return true;
    }

    public boolean exitBatchMode() {
        if (!isBatchMode()) {
            return false;
        }
        Fragment fragment = getButtonBarFragment();
        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.button_bar, fragment);
        ft.commit();
        setBatchMode(false);
        adapter.setOnCheckedNumberChangeListener(null);
        adapter.allCancel();
        adapter.notifyDataSetChanged();
        return true;
    }

    protected abstract Fragment getButtonBarFragment();

    public void setBatchMode(boolean batchMode) { adapter.setBatchMode(batchMode); }
    public boolean isBatchMode() { return adapter.isBatchMode(); }

    public void checkAndUpdate() {
        adapter.checkAndUpdate();
    }

    @Override
    public void onEditClick() {
        Bundle bundle = adapter.getDataBundle();
        if (bundle != null) {
            Intent intent = new Intent(getActivity(), EditActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, Config.Request.EDIT);
        }
    }

    @Override
    public void onEditCommit() {
        exitBatchMode();
    }

    @Override
    public void onDeleteComplete() {
        exitBatchMode();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Config.Request.EDIT:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    switch (bundle.getInt("type")) {
                        case Config.EditType.PHONE: {
                            DBHelper helper = DBHelper.getInstance(null);
                            SQLiteDatabase db = helper.getWritableDatabase();
                            db.execSQL("UPDATE phone SET name = ?, tel = ? WHERE _id = ?",
                                    new Object[]{
                                            bundle.getString("new_name"),
                                            bundle.getString("new_tel"),
                                            bundle.getString("id")
                                    });
                            db.close();
                            Config.Changed.PHONE = true;
                        }
                        break;
                        case Config.EditType.RECORD: {
                            File file = (File) bundle.getSerializable("old_file");
                            String newName = bundle.getString("new_name");
                            File newFile = new File(file.getParentFile().getAbsolutePath(), newName);
                            file.renameTo(newFile);
                            Config.Changed.RECORD = true;
                        }
                        break;
                    }
                    checkAndUpdate();
                }
                break;
        }
    }
}
