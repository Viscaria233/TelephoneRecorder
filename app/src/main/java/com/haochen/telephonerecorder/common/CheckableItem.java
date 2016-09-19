package com.haochen.telephonerecorder.common;

/**
 * Created by Haochen on 2016/7/6.
 */
public class CheckableItem<V> {

    private V value;
    private boolean checked;

    public CheckableItem(V value, boolean checked) {
        this.value = value;
        this.checked = checked;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
