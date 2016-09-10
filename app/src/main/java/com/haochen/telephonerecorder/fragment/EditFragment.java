package com.haochen.telephonerecorder.fragment;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by Haochen on 2016/7/12.
 */
public abstract class EditFragment extends Fragment {

    protected Bundle data;

    public EditFragment(Bundle data) {
        this.data = data;
    }

    public abstract boolean checkAvailable(String origin);
    public abstract Bundle resultBundle();
}
