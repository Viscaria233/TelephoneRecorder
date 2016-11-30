package com.haochen.telephonerecorder.recorder;

import java.io.File;
import java.io.IOException;

/**
 * Created by Haochen on 2016/7/13.
 */
public abstract class PhoneRecorder {

    public PhoneRecorder(boolean compress) {
    }

    public abstract String getOutputFormat();

    public abstract void setOutputFile(File file);
    public abstract void prepare() throws IOException;
    public abstract void start();
    public abstract void stop();
    public abstract void release();
}
