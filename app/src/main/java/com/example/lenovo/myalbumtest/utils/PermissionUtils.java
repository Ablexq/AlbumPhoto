package com.example.lenovo.myalbumtest.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lenovo on 2017/8/22.
 */

public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();

    private static PermissionGrant mPermissionGrant;

    public interface PermissionGrant {
        void onPermissionGranted(int requestCode);

        void onPermissionFailure(int requestCode);
    }

    /**
     * 授权结果的处理
     */
    public static void requestPermissionsResult(Activity activity,
                                                int requestCode,
                                                @NonNull String[] permissions,
                                                @NonNull int[] grantResults,
                                                PermissionGrant permissionGrant) {

        if (activity == null) {
            Log.e(TAG, "activity == null");
            return;
        }

        if (grantResults.length == 0) {//无授权
            Log.e(TAG, "grantResults.length == 0");
            return;
        }

        if (grantResults.length == 1) {//单个权限
            Log.e(TAG, "grantResults.length == 1");
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGrant.onPermissionGranted(requestCode);
            } else {
                permissionGrant.onPermissionFailure(requestCode);
            }
        } else {//多个权限
            Log.e(TAG, "grantResults.length ==" + grantResults.length);
            //未授权集合
            List<String> notGranted = new ArrayList<>();

            //检查是否授权成功
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    notGranted.add(permissions[i]);
                    Log.e(TAG, "notGranted ==" + permissions[i]);
                }
            }

            if (notGranted.size() == 0) {
                permissionGrant.onPermissionGranted(requestCode);
            } else {
                permissionGrant.onPermissionFailure(requestCode);
            }

            notGranted.clear();
            notGranted = null;
        }
    }

    public static final int REQUESTCODE_MULTI = 1;
    public static final int REQUESTCODE_SINGLE = 2;

    public static final String[] PERMISSION_RECORD_AUDIO = {Manifest.permission.RECORD_AUDIO};
    public static final String[] PERMISSION_GET_ACCOUNTS = {Manifest.permission.GET_ACCOUNTS};
    public static final String[] PERMISSION_READ_PHONE_STATE = {Manifest.permission.READ_PHONE_STATE};
    public static final String[] PERMISSION_CALL_PHONE = {Manifest.permission.CALL_PHONE};
    public static final String[] PERMISSION_CAMERA = {Manifest.permission.CAMERA};
    public static final String[] PERMISSION_ACCESS_FINE_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};
    public static final String[] PERMISSION_ACCESS_COARSE_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
    public static final String[] PERMISSION_READ_EXTERNAL_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE};
    public static final String[] PERMISSION_WRITE_EXTERNAL_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final String[] requestPermissions = {
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * 设置权限
     *
     * @param activity
     * @param permissions
     * @param requestCode
     * @return 授权前是否已有权限
     */
    public static boolean setPermission(Activity activity, String[] permissions, int requestCode) {

        if (Build.VERSION.SDK_INT < 23) {
            Log.e(TAG, "Build.VERSION.SDK_INT < 23");
            return true;
        }

        if (checkPermissions(activity, permissions)) {
            Log.e(TAG, "已有权限");
            return true;
        }

        Log.e(TAG, "弹窗：是否同意授权");
        requestPermission(activity, permissions, requestCode);
        return false;
    }


    /**
     * 提示单个权限的授权
     *
     * @param activity
     * @param permissions
     * @return
     */
    public static void showRequest(Activity activity, String[] permissions) {
        if (shouldShowRequest(activity, permissions)) {
            Log.e(TAG, "弹窗：已拒绝过再次温馨提示");
            PermissionUtils.showMessage(activity);
        }
    }


    /**
     * 是否提示单个权限
     *
     * @param activity
     * @param permissions
     * @return
     */
    public static boolean shouldShowRequest(Activity activity, String[] permissions) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissions[0])) {
            return true;
        }
        return false;
    }


    /**
     * 检测权限是否都授权
     *
     * @param activity
     * @param permissions
     * @return
     */
    public static boolean checkPermissions(Activity activity, String[] permissions) {
        boolean hasAllPermission = true;

        if (permissions.length > 1) {

            List<String> unPermissions = new ArrayList<>();

            for (int i = 0; i < permissions.length; i++) {
                if (ActivityCompat.checkSelfPermission(activity, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    unPermissions.add(permissions[i]);
                }
            }

            if (unPermissions.size() > 0) {
                unPermissions.clear();
                unPermissions = null;

                hasAllPermission = false;
            }

        } else if (permissions.length == 1) {
            if (ActivityCompat.checkSelfPermission(activity, permissions[0]) != PackageManager.PERMISSION_GRANTED) {
                hasAllPermission = false;
            }
        }


        return hasAllPermission;
    }


    /**
     * 请求权限
     *
     * @param activity
     * @param permission
     * @param requestCode
     */
    public static void requestPermission(Activity activity, String[] permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, permission, requestCode);
    }


    /**
     * 提示开启权限
     *
     * @param activity
     */
    public static void showMessage(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setMessage("app需要开启权限才能使用此功能")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    //打开手机setting授权的页面
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri);
                        activity.startActivity(intent);
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }


}