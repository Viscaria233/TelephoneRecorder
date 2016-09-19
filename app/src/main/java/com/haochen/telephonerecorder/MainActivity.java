package com.haochen.telephonerecorder;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;

import com.haochen.telephonerecorder.apibugfixes.FixedAppCompatActivity;
import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.fragment.FilterFragment;
import com.haochen.telephonerecorder.fragment.HistoryFragment;
import com.haochen.telephonerecorder.fragment.MyFragment;
import com.haochen.telephonerecorder.fragment.RecordFragment;
import com.haochen.telephonerecorder.monitor.OutgoingCallReceiver;
import com.haochen.telephonerecorder.sqlite.DBHelper;
import com.haochen.telephonerecorder.monitor.PhoneListener;

import java.io.File;

public class MainActivity extends FixedAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MyFragment fragment;

    private static class Preference {
        public static final String NAME = "config";
        public static final String FIRST_LAUNCH = "first_launch";
        public static final String ROOT_PATH = "root_path";
        public static final String RECORD_PATH = "record_path";
        public static final String LOG_FILE = "log_file";
        public static final String FORMAT = "format";
        public static final String COMPRESS = "compress";
    }

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
        SharedPreferences sp = getSharedPreferences(Preference.NAME, Activity.MODE_PRIVATE);

        boolean firstLaunch = sp.getBoolean(Preference.FIRST_LAUNCH, true);
        if (firstLaunch) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(Preference.FIRST_LAUNCH, false);
            
            String root = Environment.getExternalStorageDirectory() + "/TelephoneRecorder";
            editor.putString(Preference.ROOT_PATH, root);
            editor.putString(Preference.RECORD_PATH, root + "/Record");
            editor.putString(Preference.LOG_FILE, "log.txt");
            editor.putInt(Preference.FORMAT, Config.Recorder.FORMAT_WAV);
            editor.putBoolean(Preference.COMPRESS, false);
            editor.apply();
        }

        Config.Storage.ROOT_PATH = sp.getString(Preference.ROOT_PATH, "");
        Config.Storage.RECORD_PATH = sp.getString(Preference.RECORD_PATH, "");
        Config.Storage.LOG_FILE = sp.getString(Preference.LOG_FILE, "");
        Config.Recorder.FORMAT = sp.getInt(Preference.FORMAT, 0);
        Config.Recorder.COMPRESS = sp.getBoolean(Preference.COMPRESS, false);
        Config.Changed.PHONE = false;
        Config.Changed.RECORD = false;
        Config.Changed.HISTORY = false;
        File file = new File(Config.Storage.RECORD_PATH);
        if (!file.exists()) {
            file.mkdirs();
        }

        DBHelper.getInstance(this);

        new OutgoingCallReceiver();
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        tm.listen(new PhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);

        showFilterView();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fragment.isBatchMode()) {
            fragment.exitBatchMode();
        } else {
            if (fragment instanceof RecordFragment) {
                ((RecordFragment) fragment).pause();
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
        final int id = item.getItemId();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                fragment.exitBatchMode();
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
//            }
//        }).start();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFilterView() {
        fragment = new FilterFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, fragment);
        ft.commit();
    }

    private void showRecordView() {
        fragment = new RecordFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, fragment);
        ft.commit();
    }

    private void showHistoryView() {
        fragment = new HistoryFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment, fragment);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }
        Bundle b = data.getExtras();
        if (b != null && b.getSerializable(FixedAppCompatActivity.KEY) != null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        switch (requestCode) {
            case Config.Request.SETTING:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    Config.Storage.RECORD_PATH = bundle.getString("path");
                    Config.Recorder.FORMAT = bundle.getInt("format");
                    Config.Recorder.COMPRESS = bundle.getBoolean("compress");

                    SharedPreferences sp = getSharedPreferences(Preference.NAME, Activity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.remove(Preference.RECORD_PATH);
                    editor.remove(Preference.FORMAT);
                    editor.remove(Preference.COMPRESS);

                    editor.putString(Preference.RECORD_PATH, Config.Storage.RECORD_PATH);
                    editor.putInt(Preference.FORMAT, Config.Recorder.FORMAT);
                    editor.putBoolean(Preference.COMPRESS, Config.Recorder.COMPRESS);

                    editor.apply();

                    Config.Changed.RECORD = true;
                    if (fragment instanceof RecordFragment) {
                        fragment.checkAndUpdate();
                    }
                }
                break;
        }
    }

}
