package com.haochen.telephonerecorder.util;

import android.media.MediaRecorder;

import com.haochen.telephonerecorder.common.Config;
import com.haochen.telephonerecorder.recorder.PhoneRecorder;
import com.haochen.telephonerecorder.recorder.Recorder3GP;
import com.haochen.telephonerecorder.recorder.RecorderWAV;

import java.io.File;
import java.io.IOException;

/**
 * Created by Haochen on 2016/7/6.
 */
public class RecorderManager {

    private PhoneRecorder recorder;
    private static RecorderManager instance = new RecorderManager();
    private String tel;
    private File outputFile;

    private int type;

    public static final int TYPE_NULL = 0;
    public static final int TYPE_IN = 1;
    public static final int TYPE_OUT = 2;

    private State state;

    public static RecorderManager getInstance() {
        return instance;
    }

    private RecorderManager() {
        initial();
    }

    private void initial() {
        state = new InitialState();
        switch (Config.Recorder.FORMAT) {
            case Config.Recorder.FORMAT_3GP:
                recorder = new Recorder3GP();
                break;
            case Config.Recorder.FORMAT_WAV:
                recorder = new RecorderWAV(Config.Recorder.COMPRESS);
                break;
        }
        tel = "";
        outputFile = null;
        type = TYPE_NULL;
    }

    public void setTel(String tel) {
        state.setTel(tel);
    }

    public void setType(int type) {
        state.setType(type);
    }

    public void start() throws IOException {
        state.start();
    }

    public void stop() {
        state.stop();
    }

    private void prepare() throws IOException {
        File file = new File(Config.Storage.RECORD_PATH, generateFileName());
        this.outputFile = file;
        recorder.setOutputFile(file);
        recorder.prepare();
    }

    private String generateFileName() {
        if (type == TYPE_NULL) {
            return "";
        }
        if ("".equals(tel)) {
            return "";
        }
        String name = "";
        switch (type) {
            case TYPE_IN:
                name = "From_";
                break;
            case TYPE_OUT:
                name = "To_";
                break;
        }
        name += tel + "_" + System.currentTimeMillis();
        switch (Config.Recorder.FORMAT) {
            case Config.Recorder.FORMAT_3GP:
                name += ".3gp";
                break;
            case Config.Recorder.FORMAT_WAV:
                name += ".wav";
                break;
        }
        return name;
    }

    public void reset() {
        recorder.release();
        instance.initial();
    }

    public String getTel() {
        return tel;
    }

    public boolean isStarted() {
        return state instanceof WorkingState;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public int getType() {
        return type;
    }

    private abstract class State {
        public abstract void start() throws IOException;
        public abstract void stop();
        public abstract void setTel(String tel);
        public abstract void setType(int type);
    }

    private class InitialState extends State {
        @Override
        public void start() throws IOException {}
        @Override
        public void stop() {}
        @Override
        public void setTel(String tel) {
            RecorderManager.this.tel = tel;
            if (!"".equals(tel) && type != TYPE_NULL) {
                state = new ReadyState();
            }
        }
        @Override
        public void setType(int type) {
            RecorderManager.this.type = type;
            if (!"".equals(tel) && type != TYPE_NULL) {
                state = new ReadyState();
            }
        }
    }

    private class ReadyState extends State {
        @Override
        public void start() throws IOException {
            prepare();
            recorder.start();
            state = new WorkingState();
        }
        @Override
        public void stop() {}
        @Override
        public void setTel(String tel) {
            RecorderManager.this.tel = tel;
            if ("".equals(tel)) {
                state = new InitialState();
            }
        }
        @Override
        public void setType(int type) {
            RecorderManager.this.type = type;
            if (type == TYPE_NULL) {
                state = new InitialState();
            }
        }
    }

    private class WorkingState extends State {
        @Override
        public void start() throws IOException {}
        @Override
        public void stop() {
            recorder.stop();
            state = new ReadyState();
        }
        @Override
        public void setTel(String tel) {}
        @Override
        public void setType(int type) {}
    }

}
