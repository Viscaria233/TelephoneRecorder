package com.haochen.telephonerecorder;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.haochen.telephonerecorder.adapter.HistoryAdapter;
import com.haochen.telephonerecorder.adapter.MyAdapter;
import com.haochen.telephonerecorder.adapter.PhoneAdapter;
import com.haochen.telephonerecorder.adapter.RecordAdapter;
import com.haochen.telephonerecorder.callback.OnDeleteCompleteListener;
import com.haochen.telephonerecorder.callback.OnEditClickListener;
import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.common.Record;
import com.haochen.telephonerecorder.fragment.BaseBatchFragment;
import com.haochen.telephonerecorder.fragment.HistoryFragment;
import com.haochen.telephonerecorder.fragment.MyFragment;
import com.haochen.telephonerecorder.fragment.PhoneFragment;
import com.haochen.telephonerecorder.fragment.RecordFragment;
import com.haochen.telephonerecorder.receiver.OutgoingCallReceiver;
import com.haochen.telephonerecorder.struct.CheckableItem;
import com.haochen.telephonerecorder.util.DBHelper;
import com.haochen.telephonerecorder.util.PhoneListener;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnEditClickListener, OnDeleteCompleteListener {

    private ListView listView;
    private MyAdapter adapter;
    private MyAdapter phoneAdapter;
    private MyAdapter recordAdapter;
    private MyAdapter historyAdapter;

    private MyFragment selectedFragment;
    private BaseBatchFragment batchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initial();
    }

    private void initial() {
        Config.Storage.ROOT_PATH = Environment.getExternalStorageDirectory() + "/TelephoneRecorder";
        Config.Storage.RECORD_PATH = Config.Storage.ROOT_PATH + "/Record";
        Config.Storage.LOG_FILE = "log.txt";
        Config.Changed.PHONE = false;
        Config.Changed.RECORD = false;
        Config.Changed.HISTORY = false;
        Config.Recorder.FORMAT = Config.Recorder.FORMAT_WAV;
        Config.Recorder.COMPRESS = false;
        File file = new File(Config.Storage.RECORD_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }
        Config.BATCH_MODE = false;
        DBHelper.getInstance(this);

//        Config.Service.STARTED = true;
//        Config.Service.SERVICE = new Intent(this, PhoneListenService.class);
//        startService(Config.Service.SERVICE);

        new OutgoingCallReceiver();
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        tm.listen(new PhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);

        if (phoneAdapter == null) {
            phoneAdapter = new PhoneAdapter(this);
        }
        if (recordAdapter == null) {
            recordAdapter = new RecordAdapter(this);
        }
        if (historyAdapter == null) {
            historyAdapter = new HistoryAdapter(this);
        }
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (Config.BATCH_MODE) {
                    return true;
                }
                adapter.checkAndUpdate();
                adapter.setChecked(position, true);
                enterBatchMode();
                adapter.notifyDataSetChanged();
                return true;
            }
        });
        adapter = phoneAdapter;
        showFilterView();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (batchFragment != null) {
            exitBatchMode();
        } else {
            if (selectedFragment instanceof RecordFragment) {
                ((RecordFragment) selectedFragment).pause();
            }
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showSettingView();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        Config.BATCH_MODE = false;
        int id = item.getItemId();
        if (id == R.id.nav_filter) {
            showFilterView();
        } else if (id == R.id.nav_record) {
            showRecordView();
        } else if (id == R.id.nav_history) {
            showHistoryView();
        } else if (id == R.id.nav_setting) {
            showSettingView();
        } else if (id == R.id.nav_help) {
            showHelpView();
        } else if (id == R.id.nav_about) {
            showAboutView();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFilterView() {
        adapter.allCancel();
        adapter = phoneAdapter;
        adapter.checkAndUpdate();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(null);
        selectedFragment = new PhoneFragment();
        selectedFragment.setAdapter(adapter);
        batchFragment = null;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_button_bar, selectedFragment);
        ft.commit();
    }

    private void showRecordView() {
        adapter.allCancel();
        adapter = recordAdapter;
        adapter.checkAndUpdate();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!Config.BATCH_MODE) {
                    File file = ((CheckableItem<Record>) adapter.getItem(position))
                            .getValue().getFile();
                    try {
                        RecordFragment fragment = ((RecordFragment) selectedFragment);
                        fragment.reset();
                        fragment.setFile(file);
                        fragment.prepare();
                        fragment.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        selectedFragment = new RecordFragment();
        selectedFragment.setAdapter(adapter);
        batchFragment = null;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_button_bar, selectedFragment);
        ft.commit();
    }

    private void showHistoryView() {
        adapter.allCancel();
        adapter = historyAdapter;
        batchFragment = null;
        adapter.checkAndUpdate();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(null);
        selectedFragment = new HistoryFragment();
        selectedFragment.setAdapter(adapter);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_button_bar, selectedFragment);
        ft.commit();
    }

    private void showSettingView() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, Config.Request.SETTING);
    }

    private void showHelpView() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    private void showAboutView() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void enterBatchMode() {
        batchFragment = adapter.createBatchFragment();
        batchFragment.setOnEditClickListener(this);
        batchFragment.setOnDeleteCompleteListener(this);
        adapter.setOnCheckedNumberChangeListener(batchFragment);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_button_bar, batchFragment);
        ft.commit();
        Config.BATCH_MODE = true;
    }

    public void exitBatchMode() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_button_bar, selectedFragment);
        ft.commit();
        adapter.setOnCheckedNumberChangeListener(null);
        batchFragment.setOnDeleteCompleteListener(null);
        batchFragment.setOnEditClickListener(null);
        batchFragment = null;
        Config.BATCH_MODE = false;
        adapter.allCancel();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onEditClick() {
        Bundle bundle = adapter.getDataBundle();
        if (bundle != null) {
            Intent intent = new Intent(this, EditActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, Config.Request.EDIT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Config.Request.EDIT:
                exitBatchMode();
                if (resultCode == RESULT_OK) {
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
                    adapter.checkAndUpdate();
                }
                break;
            case Config.Request.SETTING:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Config.Storage.RECORD_PATH = bundle.getString("path");
                    Config.Recorder.FORMAT = bundle.getInt("format");
                    Config.Recorder.COMPRESS = bundle.getBoolean("compress");
                    Config.Changed.RECORD = true;
                    if (adapter instanceof RecordAdapter) {
                        adapter.checkAndUpdate();
                    }
                }
                break;
        }
    }

    @Override
    public void onDeleteComplete() {
        exitBatchMode();
    }
}
