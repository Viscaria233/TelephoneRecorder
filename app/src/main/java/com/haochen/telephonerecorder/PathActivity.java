package com.haochen.telephonerecorder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.util.FilePicker;

import java.io.File;

public class PathActivity extends AppCompatActivity {

    private ListView listView;
    private TextView currentPath;
    private TextView currentName;
    private FilePicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(R.id.listView);
        currentPath = (TextView) findViewById(R.id.textView_current_path);
        currentName = (TextView) findViewById(R.id.textView_current_name);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        picker = new FilePicker(this, listView,
                new File(bundle.getString("origin_path")),true, false);
        picker.setOnPathChangeListener(new FilePicker.OnPathChangeListener() {
            @Override
            public void onPathChange() {
                File path = picker.getPath();
                currentPath.setText(path.getAbsolutePath());
                currentName.setText(path.getName());
            }
        });

        findViewById(R.id.fab_parent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker.back();
            }
        });

        findViewById(R.id.fab_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.fab_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putString("path", picker.getPath().getAbsolutePath());
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        picker.launch();

    }

}
