package com.haochen.telephonerecorder.common;

import java.io.Serializable;

/**
 * Created by Haochen on 2016/9/11.
 */
public class Phone implements Serializable {
    private String id;
    private String name;
    private String tel;
    private boolean enabled;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
