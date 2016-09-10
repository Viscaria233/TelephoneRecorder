package com.haochen.telephonerecorder.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.haochen.telephonerecorder.MainActivity;
import com.haochen.telephonerecorder.R;
import com.haochen.telephonerecorder.callback.OnAllCancelClickListener;
import com.haochen.telephonerecorder.callback.OnAllCheckClickListener;
import com.haochen.telephonerecorder.callback.OnCheckedNumberChangeListener;
import com.haochen.telephonerecorder.callback.OnDeleteClickListener;
import com.haochen.telephonerecorder.callback.OnDeleteCompleteListener;
import com.haochen.telephonerecorder.callback.OnEditClickListener;

/**
 * Created by Haochen on 2016/7/4.
 */
public abstract class BaseBatchFragment extends Fragment implements OnCheckedNumberChangeListener {

    protected OnAllCheckClickListener onAllCheckClickListener;
    protected OnAllCancelClickListener onAllCancelClickListener;
    protected OnEditClickListener onEditClickListener;
    protected OnDeleteClickListener onDeleteClickListener;
    protected OnDeleteCompleteListener onDeleteCompleteListener;

    protected View view;
    protected FloatingActionButton allCheck;
    protected FloatingActionButton allCancel;
    protected FloatingActionButton edit;
    protected FloatingActionButton delete;

    public BaseBatchFragment(Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.layout_batch_button, null);
        allCheck = (FloatingActionButton) view.findViewById(R.id.fab_all_check);
        allCancel = (FloatingActionButton) view.findViewById(R.id.fab_all_cancel);
        edit = (FloatingActionButton) view.findViewById(R.id.fab_edit);
        delete = (FloatingActionButton) view.findViewById(R.id.fab_delete);
    }

    public void setOnAllCheckClickListener(OnAllCheckClickListener onAllCheckClickListener) {
        this.onAllCheckClickListener = onAllCheckClickListener;
    }

    public void setOnAllCancelClickListener(OnAllCancelClickListener onAllCancelClickListener) {
        this.onAllCancelClickListener = onAllCancelClickListener;
    }

    public void setOnEditClickListener(OnEditClickListener onEditClickListener) {
        this.onEditClickListener = onEditClickListener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener) {
        this.onDeleteClickListener = onDeleteClickListener;
    }

    public void setOnDeleteCompleteListener(OnDeleteCompleteListener onDeleteCompleteListener) {
        this.onDeleteCompleteListener = onDeleteCompleteListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        allCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onAllCheckClickListener != null) {
                    onAllCheckClickListener.onAllCheckClick();
                }
            }
        });
        allCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onAllCancelClickListener != null) {
                    onAllCancelClickListener.onAllCancelClick();
                }
            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onEditClickListener != null) {
                    onEditClickListener.onEditClick();
                }
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDeleteClickListener != null) {
                    new AlertDialog.Builder(getActivity()).setTitle("Confirm")
                            .setMessage("Please make sure to delete these item(s).")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onDeleteClickListener.onDeleteClick();
                                    if (onDeleteCompleteListener != null) {
                                        onDeleteCompleteListener.onDeleteComplete();
                                    }
                                }
                            }).setNegativeButton("No", null).create().show();
                }
            }
        });

        return view;
    }

}
