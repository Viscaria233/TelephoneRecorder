package com.haochen.telephonerecorder.fragment;

import android.app.Fragment;
import android.content.Context;
import android.widget.ListView;

import com.haochen.telephonerecorder.adapter.MyAdapter;

/**
 * Created by Haochen on 2016/7/10.
 */
public abstract class MyFragment extends Fragment {

    protected MyAdapter adapter;

    public MyFragment(MyAdapter adapter) {
        this.adapter = adapter;
    }

}
