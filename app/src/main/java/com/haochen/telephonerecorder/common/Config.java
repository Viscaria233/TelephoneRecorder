package com.haochen.telephonerecorder.common;

import android.content.Intent;

/**
 * Created by Haochen on 2016/7/1.
 */
public class Config {
    public static class Storage {
        public static String ROOT_PATH = "";
        public static String RECORD_PATH = "";
        public static String LOG_FILE = "";
    }
    public static class Request {
        public static final int ADD = 1;
        public static final int SETTING = 2;
        public static final int EDIT = 3;
        public static final int PATH = 4;
        public static final int CONTACT = 5;
    }
    public static class EditType {
        public static final int PHONE = 1;
        public static final int RECORD = 2;
    }
    public static class Changed {
        public static boolean PHONE;
        public static boolean RECORD;
        public static boolean HISTORY;
    }
    public static class Service {
        public static Intent SERVICE;
        public static boolean STARTED;
    }
    public static class Recorder {
        public static final int FORMAT_3GP = 1;
        public static final int FORMAT_WAV = 2;
        public static int FORMAT;
        public static boolean COMPRESS;
    }
}
