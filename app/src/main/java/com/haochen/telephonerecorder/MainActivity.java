package com.haochen.telephonerecorder;

import android.content.Intent;
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
import com.haochen.telephonerecorder.receiver.OutgoingCallReceiver;
import com.haochen.telephonerecorder.util.DBHelper;
import com.haochen.telephonerecorder.util.PhoneListener;

import java.io.File;

public class MainActivity extends FixedAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MyFragment fragment;

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
        new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        }).start();

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
                    Config.Changed.RECORD = true;
                    if (fragment instanceof RecordFragment) {
                        fragment.checkAndUpdate();
                    }
                }
                break;
        }
    }

}
