package com.haochen.telephonerecorder.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.adapter.HistoryAdapter;
import com.haochen.telephonerecorder.common.History;
import com.haochen.telephonerecorder.struct.CheckableItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Haochen on 2016/6/30.
 */
public class HistoryFragment extends MyFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view_button_bar, container, false);
        spinner = (Spinner) view.findViewById(R.id.spinner);


        String[] str = {"", "Time", "Event"};
        spinner.setAdapter(new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, str));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Comparator<History> comparator = null;
                switch (position) {
                    case 1:
                        comparator = new Comparator<History>() {
                            @Override
                            public int compare(History lhs, History rhs) {
                                return rhs.getTime().compareTo(lhs.getTime());
                            }
                        };
                        break;
                    case 2:
                        comparator = new Comparator<History>() {
                            @Override
                            public int compare(History lhs, History rhs) {
                                return lhs.getEvent().compareTo(rhs.getEvent());
                            }
                        };
                        break;
                }

                if (comparator == null) {
                    return;
                }
                List<CheckableItem> data = adapter.getData();
                List<History> temp = new ArrayList<>();
                for (CheckableItem item : data) {
                    temp.add((History) item.getValue());
                }
                History[] array = temp.toArray(new History[1]);
                Arrays.sort(array, comparator);

                data = new ArrayList<>();
                for (History i : array) {
                    data.add(new CheckableItem<>(i, false));
                }
                adapter.setData(data);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });



        listView = (ListView) view.findViewById(R.id.listView);
        adapter = new HistoryAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (isBatchMode()) {
                    return false;
                }
                adapter.setChecked(position, true);
                enterBatchMode();
                return true;
            }
        });

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        ft.replace(R.id.button_bar, getButtonBarFragment());
        ft.commit();


        return view;
    }

    @Override
    protected Fragment getButtonBarFragment() {
        return new ButtonBarFragment() {
            @Override
            public void onClearClick() {
                adapter.clear();
                adapter.notifyDataSetChanged();
            }
        };
    }

    public abstract class ButtonBarFragment extends Fragment {
        private FloatingActionButton clear;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.layout_history, container, false);
            clear = (FloatingActionButton) view.findViewById(R.id.fab_clear);
            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getActivity()).setTitle("Confirm")
                            .setMessage("Please make sure to clear the histories.")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onClearClick();
                                }
                            }).setNegativeButton("No", null).create().show();
                }
            });
            return view;
        }

        public abstract void onClearClick();
    }
}
