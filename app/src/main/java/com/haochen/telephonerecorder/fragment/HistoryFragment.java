package com.haochen.telephonerecorder.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.haochen.telephonerecorder.MainActivity;
import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.adapter.MyAdapter;

/**
 * Created by Haochen on 2016/6/30.
 */
public class HistoryFragment extends MyFragment {

    public HistoryFragment(MyAdapter adapter) {
        super(adapter);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_history, container, false);
        view.findViewById(R.id.fab_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity()).setTitle("Confirm")
                        .setMessage("Please make sure to clear the histories.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                adapter.clear();
                                adapter.notifyDataSetChanged();
                            }
                        }).setNegativeButton("No", null).create().show();
            }
        });

        return view;
    }
}
