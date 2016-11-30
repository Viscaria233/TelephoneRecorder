package com.haochen.telephonerecorder.apibugfixes;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by Haochen on 2016/9/12.
 */
public class FixedAppCompatActivity extends AppCompatActivity {
    final String TAG = "FixedAppCompatActivity";
    public static final String KEY = "----trace----";

    @Override
    public void startActivityFromFragment(Fragment fragment, Intent intent, int requestCode) {
        if (requestCode == -1) {
            super.startActivityForResult(intent, -1);
            return;
        }

        try {
            if ((requestCode&0xffff0000) != 0) {
                throw new IllegalArgumentException("Can only use lower 16 bits for requestCode");
            }

            FragmentActivity activity = fragment.getActivity();
            List<Integer> index = new FragmentTracer(activity, fragment).trace();
            int[] trace = new int[index.size()];
            for (int i = 0; i < index.size(); ++i) {
                trace[i] = index.get(i);
            }

            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                bundle = new Bundle();
            }
            bundle.putSerializable(KEY, trace);
            intent.putExtras(bundle);

            super.startActivityFromFragment(fragment, intent, requestCode);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            super.startActivityFromFragment(fragment, intent, requestCode);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            super.startActivityFromFragment(fragment, intent, requestCode);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        int index = requestCode >> 16;
        if (index != 0) {
            index--;
            if (fm.getFragments() == null || index < 0
                    || index >= fm.getFragments().size()) {
                Log.w(TAG, "Activity result fragment index out of range: 0x"
                        + Integer.toHexString(requestCode));
                return;
            }

            Bundle bundle = data.getExtras();
            if (bundle == null) {
                bundle = new Bundle();
            }
            int[] trace = (int[]) bundle.getSerializable(KEY);

            if (trace == null || trace.length <= 0) {
                Log.w(TAG, "Activity result no fragment exists for index: 0x"
                        + Integer.toHexString(requestCode));
            } else {
                Fragment frag = fm.getFragments().get(trace[0]);
                for (int i = 1; i < trace.length; ++i) {
                    frag = frag.getChildFragmentManager().getFragments().get(trace[i]);
                }
                frag.onActivityResult(requestCode & 0xffff, resultCode, data);
            }
        }
    }

    private class FragmentTracer {
        private FragmentActivity activity;
        private Fragment fragment;
        private Stack<Integer> stack;

        public FragmentTracer(FragmentActivity activity, Fragment fragment) throws NoSuchFieldException {
            this.activity = activity;
            this.fragment = fragment;
        }

        public List<Integer> trace() throws NoSuchFieldException, IllegalAccessException {
            stack = new Stack<>();
            FragmentManager fm = activity.getSupportFragmentManager();
            List<Fragment> frags = fm.getFragments();
            for (Fragment f : frags) {
                stack.push(getIndex(f));
                if (recurse(f)) {
                    break;
                }
                stack.pop();
            }

            Stack<Integer> temp = new Stack<>();
            while (!stack.isEmpty()) {
                temp.push(stack.pop());
            }
            List<Integer> index = new ArrayList<>();
            while (!temp.isEmpty()) {
                index.add(temp.pop());
            }
            return index;
        }

        private boolean recurse(Fragment frag) throws NoSuchFieldException, IllegalAccessException {
            if (frag == fragment) {
                return true;
            }

            List<Fragment> frags = frag.getChildFragmentManager().getFragments();
            for (Fragment f : frags) {
                stack.push(getIndex(frag));
                if (recurse(f)) {
                    return true;
                }
                stack.pop();
            }

            return false;
        }

        private int getIndex(Fragment frag)
                throws NoSuchFieldException, IllegalAccessException {
            return IndexUtil.getInstance().getIndex(frag);
        }
    }

    private static class IndexUtil {
        private static IndexUtil instance;

        private Class c;
        private Field field;

        public static IndexUtil getInstance() {
            if (instance == null) {
                try {
                    instance = new IndexUtil();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
            return instance;
        }

        private IndexUtil() throws NoSuchFieldException {
            c = Fragment.class;
            field = c.getDeclaredField("mIndex");
            field.setAccessible(true);
        }

        public int getIndex(Fragment frag) throws IllegalAccessException {
            return field.getInt(frag);
        }
    }
}

