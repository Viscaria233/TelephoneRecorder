package com.haochen.telephonerecorder.util;

import android.media.MediaPlayer;

import java.io.File;

/**
 * Created by Haochen on 2016/7/7.
 */
public class MediaManager {
    private MediaPlayer player;
    private File file;

    public MediaManager() {
        player = new MediaPlayer();
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public void start() {
    }
}
