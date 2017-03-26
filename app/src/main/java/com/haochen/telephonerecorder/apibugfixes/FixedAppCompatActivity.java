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
            if ((requestCode & 0xffff0000) != 0) {
                throw new IllegalArgumentException("Can only use lower 16 bits for requestCode");
            }

            FragmentActivity activity = fragment.getActivity();
            int[] trace = FragmentTracer.trace(activity, fragment);

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

    private static class FragmentTracer {
        private static Stack<Integer> stack;

        public static int[] trace(FragmentActivity activity, Fragment fragment)
                throws NoSuchFieldException, IllegalAccessException {
            stack = new Stack<>();
            FragmentManager fm = activity.getSupportFragmentManager();
            List<Fragment> frags = fm.getFragments();
            for (Fragment f : frags) {
                stack.push(getIndex(f));
                if (tryTracing(f, fragment)) {
                    break;
                }
                stack.pop();
            }

            Stack<Integer> reversed = new Stack<>();
            while (!stack.isEmpty()) {
                reversed.push(stack.pop());
            }

            int[] trace = new int[reversed.size()];
            for (int i = 0; i < trace.length; ++i) {
                trace[i] = reversed.pop();
            }
            return trace;
        }

        private static boolean tryTracing(Fragment frag, Fragment target)
                throws NoSuchFieldException, IllegalAccessException {
            if (frag == target) {
                return true;
            }

            List<Fragment> frags = frag.getChildFragmentManager().getFragments();
            for (Fragment f : frags) {
                stack.push(getIndex(frag));
                if (tryTracing(f, target)) {
                    return true;
                }
                stack.pop();
            }

            return false;
        }

        private static int getIndex(Fragment frag)
                throws NoSuchFieldException, IllegalAccessException {
            return IndexUtil.getIndex(frag);
        }
    }

    private static class IndexUtil {
        private static Field field;

        static {
            try {
                field = Fragment.class.getDeclaredField("mIndex");
                field.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }

        public static int getIndex(Fragment frag) throws IllegalAccessException {
            return field.getInt(frag);
        }
    }
}

