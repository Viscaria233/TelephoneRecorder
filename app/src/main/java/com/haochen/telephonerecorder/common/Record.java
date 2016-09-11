package com.haochen.telephonerecorder.common;

import java.io.File;
import java.io.Serializable;

/**
 * Created by Haochen on 2016/9/11.
 */
public class Record implements Serializable {
    private File file;
    private String length;
    private String size;
    private String modified;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }
}
