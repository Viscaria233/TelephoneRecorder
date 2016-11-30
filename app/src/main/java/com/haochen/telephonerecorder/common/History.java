package com.haochen.telephonerecorder.common;

import java.io.Serializable;

/**
 * Created by Haochen on 2016/9/11.
 */
public class History implements Serializable {
    private String id;
    private String time;
    private String event;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}