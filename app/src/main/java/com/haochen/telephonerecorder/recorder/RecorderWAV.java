package com.haochen.telephonerecorder.recorder;

import android.util.Log;

import com.haochen.telephonerecorder.util.WAVRecorder;

import java.io.File;
import java.io.IOException;

/**
 * Created by Haochen on 2016/7/13.
 */
public class RecorderWAV extends PhoneRecorder {

    private WAVRecorder recorder;

    public RecorderWAV(boolean compress) {
        super(compress);
        recorder = WAVRecorder.getInstanse(compress);
    }

    @Override
    public void setOutputFile(File file) {
        recorder.setOutputFile(file.getAbsolutePath());
    }

    @Override
    public void prepare() throws IOException {
        recorder.prepare();
    }

    @Override
    public void start() {
        recorder.start();
    }

    @Override
    public void stop() {
        recorder.stop();
    }

    @Override
    public void release() {
        recorder.release();
    }
}
