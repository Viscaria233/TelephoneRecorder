package com.haochen.telephonerecorder.recorder;

import com.haochen.telephonerecorder.common.Config;

/**
 * Created by Haochen on 2016/9/13.
 */
public class MyFilenameBuilder implements RecorderManager.FilenameBuilder {
    @Override
    public String build(String tel, int type) {
        if (type == RecorderManager.TYPE_NULL) {
            return "";
        }
        if ("".equals(tel)) {
            return "";
        }
        String name = "";
        switch (type) {
            case RecorderManager.TYPE_IN:
                name = "From_";
                break;
            case RecorderManager.TYPE_OUT:
                name = "To_";
                break;
        }
        name += tel + "_" + System.currentTimeMillis();
        return name;
    }
}
