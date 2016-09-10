package com.haochen.telephonerecorder;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.haochen.telephonerecorder.util.DBHelper;

public class AddActivity extends AppCompatActivity {

    private EditText name;
    private EditText tel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        name = (EditText) findViewById(R.id.editText_name);
        tel = (EditText) findViewById(R.id.editText_tel);

        findViewById(R.id.fab_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.fab_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String n = name.getText().toString();
                String t = tel.getText().toString();
                if ("".equals(t)) {
                    Snackbar.make(tel, "Tel can not be empty.", Snackbar.LENGTH_LONG).show();
                } else {
                    DBHelper helper = DBHelper.getInstance(null);
                    SQLiteDatabase db = helper.getReadableDatabase();
                    Cursor cursor = db.rawQuery("SELECT * FROM phone WHERE tel LIKE ?",
                            new String[]{"%" + t});
                    boolean exists = cursor.moveToNext();
                    db.close();
                    if (exists) {
                        Snackbar.make(tel, "This tel is already exists.",
                                Snackbar.LENGTH_LONG).show();
                    } else {
                        Intent intent = getIntent();
                        Bundle bundle = new Bundle();
                        bundle.putString("name", n);
                        bundle.putString("tel", t);
                        intent.putExtras(bundle);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                }
            }
        });
    }

}
