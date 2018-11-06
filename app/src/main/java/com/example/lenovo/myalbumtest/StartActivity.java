package com.example.lenovo.myalbumtest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.lenovo.myalbumtest.utils.PermissionUtils;


public class StartActivity extends AppCompatActivity {

    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        handler = new Handler();

        if (PermissionUtils.setPermission(this, PermissionUtils.PERMISSION_WRITE_EXTERNAL_STORAGE, PermissionUtils.REQUESTCODE_READ_EXTERNAL_STORAGE)) {
            next();
        }
    }

    private void next() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(StartActivity.this, MainActivity.class));
            }
        }, 3000);
    }

    /**
     * android 6.0 权限适配
     *
     * @param requestCode:请求码
     * @param permissions：权限
     * @param grantResults：授权结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, new PermissionUtils.PermissionGrant() {

            @Override
            public void onPermissionGranted(int requestCode) {
                Log.e("PermissionUtils", "授权回调结果111：成功");

                if (requestCode == PermissionUtils.REQUESTCODE_READ_EXTERNAL_STORAGE) {
                    next();
                }
            }

            @Override
            public void onPermissionFailure(int requestCode) {
                Log.e("PermissionUtils", "授权回调结果111：失败");
                if (requestCode == PermissionUtils.REQUESTCODE_READ_EXTERNAL_STORAGE) {
                    PermissionUtils.showRequest(StartActivity.this, PermissionUtils.PERMISSION_READ_EXTERNAL_STORAGE);
                }
            }
        });
    }

}
