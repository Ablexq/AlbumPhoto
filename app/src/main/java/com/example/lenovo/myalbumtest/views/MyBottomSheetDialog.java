package com.example.lenovo.myalbumtest.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;

import com.example.lenovo.myalbumtest.R;


public class MyBottomSheetDialog extends BottomSheetDialog {

    public static final int CAMERA = 1;
    public static final int ALBUM = 2;
    public static final int CANCLE = 3;

    public MyBottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    public MyBottomSheetDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    protected MyBottomSheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void initDialog(Context context, final OnClickDialogListener onClickDialogListener) {
        View view = View.inflate(context, R.layout.dialog_layout, null);
        View view1 = view.findViewById(R.id.tv_pick_phone);
        View view2 = view.findViewById(R.id.tv_pick_zone);
        View view3 = view.findViewById(R.id.tv_cancel);
        view1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDialogListener.onClickDialog(CAMERA);
            }
        });
        view2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDialogListener.onClickDialog(ALBUM);
            }
        });
        view3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDialogListener.onClickDialog(CANCLE);

            }
        });
        setContentView(view);
    }


    public interface OnClickDialogListener {
        void onClickDialog(int pos);
    }
}
