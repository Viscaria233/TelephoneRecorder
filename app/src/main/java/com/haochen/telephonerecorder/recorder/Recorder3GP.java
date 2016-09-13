package com.haochen.telephonerecorder.recorder;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

/**
 * Created by Haochen on 2016/7/13.
 */
public class Recorder3GP extends PhoneRecorder {
    private MediaRecorder recorder;

    public Recorder3GP() {
        super(false);
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }

    @Override
    public String getOutputFormat() {
        return ".3gp";
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
