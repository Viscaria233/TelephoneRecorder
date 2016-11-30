package com.haochen.telephonerecorder.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;


/**
 * Created by Haochen on 2016/7/12.
 */
public abstract class EditFragment extends Fragment {

    protected Bundle data;

    public void setData(Bundle data) {
        this.data = data;
    }

    public abstract boolean checkAvailable(String origin);
    public abstract Bundle resultBundle();
}
