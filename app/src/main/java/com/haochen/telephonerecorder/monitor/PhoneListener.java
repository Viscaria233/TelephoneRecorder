package com.haochen.telephonerecorder.monitor;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.recorder.MyFileNameBuilder;
import com.haochen.telephonerecorder.recorder.Recorder3GP;
import com.haochen.telephonerecorder.recorder.RecorderManager;
import com.haochen.telephonerecorder.recorder.RecorderWAV;
import com.haochen.telephonerecorder.sqlite.DBHelper;
import com.haochen.telephonerecorder.util.Time;
import com.haochen.telephonerecorder.sqlite.IdBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Haochen on 2016/7/7.
 */
public class PhoneListener extends PhoneStateListener {
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE: {
                Log.v("haochen", "IDLE");
                RecorderManager recorder = RecorderManager.getInstance();
                if (recorder.isStarted()) {
                    File file = new File(Config.Storage.ROOT_PATH, Config.Storage.LOG_FILE);
                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileWriter fw = new FileWriter(file, true);

                        String time = Time.now();
                        String log = "[" + time + "] Call ended. Record finished. Save as " +
                                recorder.getOutputFile().getAbsolutePath();

                        String event = "Record: ";
                        if (recorder.getType() == RecorderManager.TYPE_IN) {
                            event += "From ";
                        } else if (recorder.getType() == RecorderManager.TYPE_OUT) {
                            event += "To ";
                        }
                        event += recorder.getTel();

                        String id = IdBuilder.newId("history");
                        DBHelper helper = DBHelper.getInstance(null);
                        SQLiteDatabase db = helper.getWritableDatabase();
                        db.execSQL("INSERT INTO history VALUES (?, ?, ?)",
                                new Object[]{id, time, event});
                        db.close();
                        Config.Changed.HISTORY = true;

                        Log.v("haochen", log);

                        fw.write(log + "\n");
                        fw.flush();
                        fw.close();

                        recorder.stop();
                        Config.Changed.RECORD = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                recorder.reset();
            }
            break;
            case TelephonyManager.CALL_STATE_RINGING: {
                Log.v("haochen", "RINGING");
                boolean accept = false;
                DBHelper helper = DBHelper.getInstance(null);
                SQLiteDatabase db = helper.getReadableDatabase();
                Cursor cursor = db.rawQuery("SELECT * FROM phone WHERE enabled = 1 AND tel LIKE ?",
                        new String[]{"%" + incomingNumber});
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
                        String time = Time.now();
                        Log.v("haochen", "[" + time + "] New call from: " + incomingNumber);
                        fw.write("[" + time + "] New call from: " + incomingNumber + "\n");
                        fw.flush();
                        fw.close();

                        RecorderManager recorder = RecorderManager.getInstance();
                        recorder.setTel(incomingNumber);
                        recorder.setType(RecorderManager.TYPE_IN);
                        recorder.setOutputDirectory(new File(Config.Storage.RECORD_PATH));
                        recorder.setFilenameBuilder(new MyFileNameBuilder());
                        switch (Config.Recorder.FORMAT) {
                            case Config.Recorder.FORMAT_3GP:
                                recorder.setPhoneRecorder(new Recorder3GP());
                                break;
                            case Config.Recorder.FORMAT_WAV:
                                recorder.setPhoneRecorder(new RecorderWAV(Config.Recorder.COMPRESS));
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
            case TelephonyManager.CALL_STATE_OFFHOOK: {
                Log.v("haochen", "OFFHOOK");
                RecorderManager recorder = RecorderManager.getInstance();
                if (!"".equals(recorder.getTel()) &&
                        recorder.getType() != RecorderManager.TYPE_NULL) {
                    File file = new File(Config.Storage.ROOT_PATH, Config.Storage.LOG_FILE);
                    try {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileWriter fw = new FileWriter(file, true);
                        String time = Time.now();
                        Log.v("haochen", "[" + time + "] Active! Start recorder.");
                        fw.write("[" + time + "] Active! Start recorder.\n");
                        fw.flush();

                        recorder.start();

                        time = Time.now();
                        Log.v("haochen", "[" + time + "] Success! Record started. " +
                                "Save as " + recorder.getOutputFile().getAbsolutePath());
                        fw.write("[" + time + "] Success! Record started. " +
                                "Save as " + recorder.getOutputFile().getAbsolutePath() + "\n");
                        fw.flush();
                        fw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }
}
