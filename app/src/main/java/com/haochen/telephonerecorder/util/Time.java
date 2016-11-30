package com.haochen.telephonerecorder.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Haochen on 2016/7/7.
 */
public class Time {
    public static String now() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }
}
