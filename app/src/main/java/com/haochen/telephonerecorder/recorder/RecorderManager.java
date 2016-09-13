package com.haochen.telephonerecorder.recorder;

import com.haochen.telephonerecorder.common.Config;

import java.io.File;
import java.io.IOException;

/**
 * Created by Haochen on 2016/7/6.
 */
public class RecorderManager {

    private PhoneRecorder phoneRecorder;
    private static RecorderManager instance = new RecorderManager();
    private String tel;
    private File outputDirectory;
    private FilenameBuilder fileNameBuilder;
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
        state = new InitialState();
        tel = "";
        outputDirectory = null;
        fileNameBuilder = null;
        outputFile = null;
        type = TYPE_NULL;
    }

    public void setPhoneRecorder(PhoneRecorder phoneRecorder) {
        state.setPhoneRecorder(phoneRecorder);
    }

    public void setOutputDirectory(File directory) {
        if (directory.isDirectory()) {
            state.setOutputDirectory(directory);
        }
    }

    public void setFilenameBuilder(FilenameBuilder builder) {
        state.setFilenameBuilder(builder);
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
        StringBuffer name = new StringBuffer(fileNameBuilder.build(tel, type)
                + '.' + phoneRecorder.getOutputFormat());
        name = new StringBuffer(name.reverse().toString().replaceFirst("[.]{2,}", "."));
        File file = new File(outputDirectory, name.reverse().toString());
        this.outputFile = file;
        phoneRecorder.setOutputFile(file);
        phoneRecorder.prepare();
    }

    public void reset() {
        if (phoneRecorder != null) {
            phoneRecorder.release();
        }
        instance = new RecorderManager();
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
        public abstract void setPhoneRecorder(PhoneRecorder recorder);
        public abstract void setOutputDirectory(File directory);
        public abstract void setFilenameBuilder(FilenameBuilder builder);
        public abstract void setTel(String tel);
        public abstract void setType(int type);
        public abstract void start() throws IOException;
        public abstract void stop();
    }

    private class InitialState extends State {
        private void transfer() {
            if (!"".equals(tel)
                    && type != TYPE_NULL
                    && phoneRecorder != null
                    && fileNameBuilder != null
                    && outputDirectory != null) {
                state = new ReadyState();
            }
        }
        @Override
        public void setPhoneRecorder(PhoneRecorder recorder) {
            phoneRecorder = recorder;
            transfer();
        }
        @Override
        public void setOutputDirectory(File directory) {
            outputDirectory = directory;
            transfer();
        }
        @Override
        public void setFilenameBuilder(FilenameBuilder builder) {
            fileNameBuilder = builder;
            transfer();
        }
        @Override
        public void setTel(String tel) {
            RecorderManager.this.tel = tel;
            transfer();
        }
        @Override
        public void setType(int type) {
            RecorderManager.this.type = type;
            transfer();
        }
        @Override
        public void start() throws IOException {}
        @Override
        public void stop() {}
    }

    private class ReadyState extends State {
        @Override
        public void setPhoneRecorder(PhoneRecorder recorder) {
            phoneRecorder = recorder;
            if (recorder == null) {
                state = new InitialState();
            }
        }
        @Override
        public void setOutputDirectory(File directory) {
            outputDirectory = directory;
            if (directory == null) {
                state = new InitialState();
            }
        }
        @Override
        public void setFilenameBuilder(FilenameBuilder builder) {
            fileNameBuilder = builder;
            if (builder == null) {
                state = new InitialState();
            }
        }
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
        @Override
        public void start() throws IOException {
            prepare();
            phoneRecorder.start();
            state = new WorkingState();
        }
        @Override
        public void stop() {}
    }

    private class WorkingState extends State {
        @Override
        public void setPhoneRecorder(PhoneRecorder recorder) {}
        @Override
        public void setOutputDirectory(File directory) {}
        @Override
        public void setFilenameBuilder(FilenameBuilder builder) {}
        @Override
        public void setTel(String tel) {}
        @Override
        public void setType(int type) {}
        @Override
        public void start() throws IOException {}
        @Override
        public void stop() {
            phoneRecorder.stop();
            state = new ReadyState();
        }
    }

    public interface FilenameBuilder {
        String build(String tel, int type);
    }

}
