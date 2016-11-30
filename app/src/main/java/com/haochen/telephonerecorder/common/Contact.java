package com.haochen.telephonerecorder.common;

import java.io.Serializable;

/**
 * Created by Haochen on 2016/9/11.
 */
public class Contact implements Serializable {
    private String name;
    private String tel;

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
}
