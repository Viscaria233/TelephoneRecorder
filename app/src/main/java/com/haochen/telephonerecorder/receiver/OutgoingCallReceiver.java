package com.haochen.telephonerecorder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.util.DBHelper;
import com.haochen.telephonerecorder.util.DateUtil;
import com.haochen.telephonerecorder.util.RecorderManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutgoingCallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            String tel = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            boolean accept = false;
            DBHelper helper = DBHelper.getInstance(null);
            SQLiteDatabase db = helper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM phone WHERE enabled = 1 AND tel LIKE ?",
                    new String[]{"%" + tel});
            if (cursor.moveToNext()) {
                accept = true;
            }
            db.close();

            if (accept) {
                File file = new File(Config.Storage.ROOT_PATH, Config.Storage.LOG_FILE);
                try {
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    FileWriter fw = new FileWriter(file, true);
                    String time = DateUtil.getDateTime();
                    Log.v("haochen", "[" + time + "] New outgoing call to: " + tel);
                    fw.write("[" + time + "] New outgoing call to: " + tel + "\n");
                    fw.flush();
                    RecorderManager recorder = RecorderManager.getInstance();
                    if (!recorder.isStarted()) {
                        time = DateUtil.getDateTime();
                        Log.v("haochen", "[" + time + "] Start recorder......");
                        fw.write("[" + time + "] Start recorder......\n");
                        fw.flush();
                        recorder.setTel(tel);
                        recorder.setType(RecorderManager.TYPE_OUT);
                        recorder.start();
                        time = DateUtil.getDateTime();
                        Log.v("haochen", "[" + time + "] Success! Record started. " +
                                "Save as " + recorder.getOutputFile().getAbsolutePath());
                        fw.write("[" + time + "] Success! Record started. " +
                                "Save as " + recorder.getOutputFile().getAbsolutePath() + "\n");
                        fw.flush();
                    } else {
                        time = DateUtil.getDateTime();
                        Log.v("haochen", "[" + time + "] Failed to start recorder. " +
                                "Recorder is already started.\n");
                        fw.write("[" + time + "] Failed to start recorder. " +
                                "Recorder is already started.\n");
                        fw.flush();
                    }
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
