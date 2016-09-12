package com.haochen.telephonerecorder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.fragment.EditFragment;
import com.haochen.telephonerecorder.fragment.EditPhoneFragment;
import com.haochen.telephonerecorder.fragment.EditRecordFragment;

import java.io.File;

public class EditActivity extends AppCompatActivity {

    private EditFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Intent intent = getIntent();
        final Bundle bundle = intent.getExtras();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        final FloatingActionButton confirm = (FloatingActionButton) findViewById(R.id.fab_confirm);
        switch (bundle.getInt("type")) {
            case Config.EditType.PHONE: {
                fragment = new EditPhoneFragment();
                fragment.setData(bundle);
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (fragment.checkAvailable(bundle.getString("tel"))) {
                            Bundle b = fragment.resultBundle();
                            intent.putExtras(b);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });
            }
            break;
            case Config.EditType.RECORD: {
                fragment = new EditRecordFragment();
                fragment.setData(bundle);
                findViewById(R.id.fab_confirm).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        File parent = ((File) bundle.getSerializable("old_file")).getParentFile();
                        Bundle b = fragment.resultBundle();
                        File newFile = new File(parent.getAbsolutePath(), b.getString("new_name"));
                        if (fragment.checkAvailable(newFile.getAbsolutePath())) {
                            intent.putExtras(b);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });
            }
            break;
        }
        ft.replace(R.id.content_edit, fragment);
        ft.commit();

        findViewById(R.id.fab_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

}
