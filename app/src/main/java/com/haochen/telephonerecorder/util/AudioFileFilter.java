package com.haochen.telephonerecorder.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.Locale;

/**
 * Created by Haochen on 2016/7/1.
 */
public class AudioFileFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String filename) {
        String str = filename.toLowerCase(Locale.US);
        return str.endsWith(".3gp")
                || str.endsWith(".wav");
    }
}
