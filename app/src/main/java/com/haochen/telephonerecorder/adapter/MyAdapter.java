package com.haochen.telephonerecorder.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.haochen.telephonerecorder.MainActivity;
import com.haochen.telephonerecorder.callback.OnAllCancelClickListener;
import com.haochen.telephonerecorder.callback.OnAllCheckClickListener;
import com.haochen.telephonerecorder.callback.OnCheckedNumberChangeListener;
import com.haochen.telephonerecorder.callback.OnDeleteClickListener;
import com.haochen.telephonerecorder.callback.OnEditClickListener;
import com.haochen.telephonerecorder.fragment.BaseBatchFragment;
import com.haochen.telephonerecorder.struct.CheckableItem;

import java.util.List;

/**
 * Created by Haochen on 2016/7/6.
 */
public abstract class MyAdapter<T> extends BaseAdapter {

    protected Context context;
    protected List<CheckableItem<T>> list;
    protected int checkedNumber;
    protected OnCheckedNumberChangeListener onCheckedNumberChangeListener;

    public MyAdapter(Context context, List<CheckableItem<T>> list) {
        this.context = context;
        this.list = list;
        this.checkedNumber = 0;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnCheckedNumberChangeListener(
            OnCheckedNumberChangeListener onCheckedNumberChangeListener) {
        this.onCheckedNumberChangeListener = onCheckedNumberChangeListener;
    }

    protected final void onCheckedNumberChange() {
        if (onCheckedNumberChangeListener != null) {
            onCheckedNumberChangeListener.onCheckedNumberChange(checkedNumber);
        }
    }

    public abstract BaseBatchFragment createBatchFragment();

    public void allCheck() {
        for (CheckableItem checkableItem : list) {
            checkableItem.setChecked(true);
        }
        checkedNumber = list.size();
        onCheckedNumberChange();
    }

    public void allCancel() {
        for (CheckableItem checkableItem : list) {
            checkableItem.setChecked(false);
        }
        checkedNumber = 0;
        onCheckedNumberChange();
    }

    public void clear() {
        allCheck();
        deleteChecked();
    }

    public int getCheckedNumber() {
        return checkedNumber;
    }

    public int[] getCheckedIndexArray() {
        int[] index = new int[checkedNumber];
        int n = 0;
        CheckableItem item;
        for (int i = 0; n < checkedNumber; ++i) {
            item = list.get(i);
            if (item.isChecked()) {
                index[n++] = i;
            }
        }
        return index;
    }

    public void setChecked(int position, boolean checked) {
        if (checked != list.get(position).isChecked()) {
            if (checked) {
                ++checkedNumber;
            } else {
                --checkedNumber;
            }
            list.get(position).setChecked(checked);
            onCheckedNumberChange();
        }
    }

    public final void deleteChecked() {
        CheckableItem checkableItem = null;
        beginDelete();
        for (int i = 0; checkedNumber > 0; ++i) {
            checkableItem = list.get(i);
            if (checkableItem.isChecked()) {
                physicalDelete(i);
                list.remove(checkableItem);
                --i;
                --checkedNumber;
            }
        }
        endDelete();
        onCheckedNumberChange();
    }
    protected void beginDelete() {}
    protected abstract void physicalDelete(int position);
    protected void endDelete() {}


    public final void checkAndUpdate() {
        if (isDataChanged()) {
            getDataSet();
            notifyDataSetChanged();
            resetChangeFlag();
        }
    }
    protected abstract boolean isDataChanged();
    protected abstract void getDataSet();
    protected abstract void resetChangeFlag();

    public abstract Bundle getDataBundle();

    public abstract class MyBatchFragment extends BaseBatchFragment {
        public MyBatchFragment(Context context) {
            super(context);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            this.onAllCheckClickListener = new OnAllCheckClickListener() {
                @Override
                public void onAllCheckClick() {
                    allCheck();
                    notifyDataSetChanged();
                }
            };
            this.onAllCancelClickListener = new OnAllCancelClickListener() {
                @Override
                public void onAllCancelClick() {
                    allCancel();
                    notifyDataSetChanged();
                }
            };
            this.onDeleteClickListener = new OnDeleteClickListener() {
                @Override
                public void onDeleteClick() {
                    deleteChecked();
                    checkAndUpdate();
                }
            };
            return view;
        }
    }

}
