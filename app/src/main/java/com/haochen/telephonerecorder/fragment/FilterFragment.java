package com.haochen.telephonerecorder.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import com.haochen.telephonerecorder.AddActivity;
import com.haochen.telephonerecorder.ContactActivity;
import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.adapter.PhoneAdapter;
import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.common.Contact;
import com.haochen.telephonerecorder.common.Phone;
import com.haochen.telephonerecorder.struct.CheckableItem;
import com.haochen.telephonerecorder.util.DBHelper;
import com.haochen.telephonerecorder.util.IdBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Haochen on 2016/6/30.
 */
public class FilterFragment extends MyFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_view_button_bar, container, false);
        spinner = (Spinner) view.findViewById(R.id.spinner);

        String[] str = {"", "Name", "Tel"};
        spinner.setAdapter(new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, str));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Comparator<Phone> comparator = null;
                switch (position) {
                    case 1:
                        comparator = new Comparator<Phone>() {
                            @Override
                            public int compare(Phone lhs, Phone rhs) {
                                return lhs.getName().compareTo(rhs.getName());
                            }
                        };
                        break;
                    case 2:
                        comparator = new Comparator<Phone>() {
                            @Override
                            public int compare(Phone lhs, Phone rhs) {
                                return lhs.getTel().compareTo(rhs.getTel());
                            }
                        };
                        break;
                }

                if (comparator == null) {
                    return;
                }
                List<CheckableItem> data = adapter.getData();
                List<Phone> temp = new ArrayList<>();
                for (CheckableItem item : data) {
                    temp.add((Phone) item.getValue());
                }
                Phone[] array = temp.toArray(new Phone[1]);
                Arrays.sort(array, comparator);

                data = new ArrayList<>();
                for (Phone i : array) {
                    data.add(new CheckableItem<>(i, false));
                }
                adapter.setData(data);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });



        listView = (ListView) view.findViewById(R.id.listView);
        adapter = new PhoneAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (isBatchMode()) {
                    return false;
                }
//                adapter.checkAndUpdate();
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
            public void updateAdapter() {
                adapter.checkAndUpdate();
            }
        };
    }

//    @Override
//    public boolean enterBatchMode() {
//        if (isBatchMode()) {
//            return false;
//        }
//        BaseBatchFragment fragment = adapter.getBatchFragment();
//        fragment.setOnEditClickListener(this);
//        fragment.setOnDeleteCompleteListener(this);
//        adapter.setOnCheckedNumberChangeListener(fragment);
//        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
//        ft.replace(R.id.button_bar, fragment);
//        ft.commit();
//        setBatchMode(true);
//        adapter.notifyDataSetChanged();
//        return true;
//    }

//    @Override
//    public boolean exitBatchMode() {
//        if (!isBatchMode()) {
//            return false;
//        }
//        FixedFragment fragment = new ButtonBarFragment();
//        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
//        ft.replace(R.id.button_bar, fragment);
//        ft.commit();
//        setBatchMode(false);
//        adapter.setOnCheckedNumberChangeListener(null);
//        adapter.allCancel();
//        adapter.notifyDataSetChanged();
//        return true;
//    }

    public abstract class ButtonBarFragment extends Fragment {
        private FloatingActionButton contact;
        private FloatingActionButton add;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.layout_phone, container, false);
            contact = (FloatingActionButton) view.findViewById(R.id.fab_contact);
            add = (FloatingActionButton) view.findViewById(R.id.fab_add);
            contact.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ContactActivity.class);
                    startActivityForResult(intent, Config.Request.CONTACT);
                }
            });
            add.setOnClickListener(new View.OnClickListener() {
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
//                        handler.sendEmptyMessage(AdapterHandler.UPDATE);
                        updateAdapter();
                    }
                    break;
                case Config.Request.CONTACT:
                    if (resultCode == Activity.RESULT_OK) {
                        Bundle bundle = data.getExtras();
                        Object[] contactObj = (Object[]) bundle.getSerializable("contacts");
                        Object[] idObj = (Object[]) bundle.getSerializable("ids");
                        Contact[] contacts = new Contact[idObj.length];
                        String[] ids = new String[idObj.length];
                        for (int i = 0; i < contactObj.length; ++i) {
                            contacts[i] = (Contact) contactObj[i];
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
//                        handler.sendEmptyMessage(AdapterHandler.UPDATE);
                        updateAdapter();
                    }
                    break;
            }
        }

        public abstract void updateAdapter();
    }

}