package com.haochen.telephonerecorder.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.haochen.telephonerecorder.monitor.OutgoingCallReceiver;
import com.haochen.telephonerecorder.monitor.PhoneListener;

/**
 * Created by Haochen on 2016/7/7.
 */
public class PhoneListenService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        new OutgoingCallReceiver();
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        tm.listen(new PhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
