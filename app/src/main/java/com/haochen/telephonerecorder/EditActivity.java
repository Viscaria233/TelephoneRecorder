package com.haochen.telephonerecorder;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.fragment.EditFragment;
import com.haochen.telephonerecorder.fragment.EditPhoneFragment;
import com.haochen.telephonerecorder.fragment.EditRecordFragment;

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
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        switch (bundle.getInt("type")) {
            case Config.EditType.PHONE:
                fragment = new EditPhoneFragment(bundle);
                break;
            case Config.EditType.RECORD:
                fragment = new EditRecordFragment(bundle);
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

        findViewById(R.id.fab_confirm).setOnClickListener(new View.OnClickListener() {
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

}
