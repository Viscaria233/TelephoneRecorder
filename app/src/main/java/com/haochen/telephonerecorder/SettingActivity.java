package com.haochen.telephonerecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.util.DateUtil;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SettingActivity extends AppCompatActivity {

    private TextView path;
    private ImageButton browse;
    private Spinner format;
    private Spinner compress;
    private FloatingActionButton cancel;
    private FloatingActionButton confirm;

    private int formatInt;
    private boolean isCompress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initial();
        eventPerformed();
    }

    private void initial() {
        path = (TextView) findViewById(R.id.textView_path);
        browse = (ImageButton) findViewById(R.id.imageButton_browse);
        format = (Spinner) findViewById(R.id.spinner_format);
        compress = (Spinner) findViewById(R.id.spinner_compress);
        cancel = (FloatingActionButton) findViewById(R.id.fab_cancel);
        confirm = (FloatingActionButton) findViewById(R.id.fab_confirm);

        path.setText(Config.Storage.RECORD_PATH);
        format.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"3gp", "wav"}));
        formatInt = Config.Recorder.FORMAT;
        switch (formatInt) {
            case Config.Recorder.FORMAT_3GP:
                format.setSelection(0);
                break;
            case Config.Recorder.FORMAT_WAV:
                format.setSelection(1);
                break;
        }
        compress.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Compress", "Uncompress"}));
        isCompress = Config.Recorder.COMPRESS;
        compress.setSelection(isCompress ? 0 : 1);
    }

    private void eventPerformed() {
        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, PathActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("origin_path", path.getText().toString());
                intent.putExtras(bundle);
                startActivityForResult(intent, Config.Request.PATH);
            }
        });
        format.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        formatInt = Config.Recorder.FORMAT_3GP;
                        break;
                    case 1:
                        formatInt = Config.Recorder.FORMAT_WAV;
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        compress.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        isCompress = true;
                        break;
                    case 1:
                        isCompress = false;
                        break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= getIntent();
                Bundle bundle = new Bundle();
                bundle.putString("path", path.getText().toString());
                bundle.putInt("format", formatInt);
                bundle.putBoolean("compress", isCompress);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Config.Request.PATH: {
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    path.setText(bundle.getString("path"));
                }
            }
            break;
        }
    }
}
