package com.example.lenovo.myalbumtest.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * 防止多次点击，Toast重复显示,
 */

public class ToastUtil {

    private static Toast toast;

    public static void showToast(Context context, String content) {
        if (toast == null) {
            toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast.setText(content);
        }

        toast.show();
    }
}
