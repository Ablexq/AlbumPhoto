package com.example.lenovo.myalbumtest;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.example.lenovo.myalbumtest.utils.AlbunPhotoHelper;
import com.example.lenovo.myalbumtest.utils.BitmapUtils;
import com.example.lenovo.myalbumtest.utils.FileUtils;
import com.example.lenovo.myalbumtest.utils.ImageUtils;
import com.example.lenovo.myalbumtest.utils.PermissionUtils;
import com.example.lenovo.myalbumtest.views.CircleImageView;
import com.example.lenovo.myalbumtest.views.MyBottomSheetDialog;
import com.xq.myandroid7.FileProvider7;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


//参考:
// Android 7.0 行为变更 通过FileProvider在应用间共享文件吧(鸿洋)
// https://blog.csdn.net/lmj623565791/article/details/72859156
//  Android圆形头像，拍照后“无法加载此图片”的问题解决（适配Android7.0）
//  http://www.cnblogs.com/liushengchieh/p/7627271.html
//
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends AppCompatActivity implements View.OnClickListener, MyBottomSheetDialog.OnClickDialogListener {

    private CircleImageView circleImage;
    private Button btnAlbum;
    private Button btnCamera;
    private AlbunPhotoHelper albunPhotoHelper;
    private Button btnSet;
    private MyBottomSheetDialog myBottomSheetDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleImage = ((CircleImageView) this.findViewById(R.id.circleImage));
        btnAlbum = ((Button) this.findViewById(R.id.btn_album));
        btnCamera = ((Button) this.findViewById(R.id.btn_camera));
        btnSet = ((Button) this.findViewById(R.id.btn_set));
        btnAlbum.setOnClickListener(this);
        btnCamera.setOnClickListener(this);
        btnSet.setOnClickListener(this);

        albunPhotoHelper = new AlbunPhotoHelper(this);
        myBottomSheetDialog = new MyBottomSheetDialog(this);
        myBottomSheetDialog.initDialog(MainActivity.this, this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_album:
                if (PermissionUtils.setPermission(this, PermissionUtils.PERMISSION_READ_EXTERNAL_STORAGE, PermissionUtils.REQUESTCODE_READ_EXTERNAL_STORAGE)) {
                    albunPhotoHelper.openAlbum();
                }
                break;

            case R.id.btn_camera:
                if (PermissionUtils.setPermission(this, PermissionUtils.PERMISSION_CAMERA, PermissionUtils.REQUESTCODE_CAMERA)) {
                    albunPhotoHelper.openCamera();
                }
                break;

            case R.id.btn_set:
                myBottomSheetDialog.show();
                break;
        }
    }


    @SuppressWarnings("deprecation")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case AlbunPhotoHelper.REQ_ALBUM:
                if (resultCode == RESULT_OK) {
                    startActivityForResult(albunPhotoHelper.cutForPhoto(data.getData()), AlbunPhotoHelper.REQ_ZOOM);
                }
                break;

            case AlbunPhotoHelper.REQ_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    startActivityForResult(albunPhotoHelper.cutForCamera(), AlbunPhotoHelper.REQ_ZOOM);
                }
                break;

            case AlbunPhotoHelper.REQ_ZOOM:
                if (data != null) {
                    try {
                        //获取裁剪后的图片，并显示出来
                        final Bitmap bitmap = albunPhotoHelper.getBitmap();

                        if (bitmap != null) {
                            circleImage.setImageBitmap(bitmap);//用ImageView显示出来

                            new Thread(new Runnable() {//流操作放在子线程
                                @Override
                                public void run() {
                                    ImageUtils.saveImageToLocal(MainActivity.this, bitmap);//保存在SD卡中
                                }
                            }).start();

                            if (bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
                break;
            default:
                break;
        }

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
                    albunPhotoHelper.openAlbum();
                } else if (requestCode == PermissionUtils.REQUESTCODE_CAMERA) {
                    albunPhotoHelper.openCamera();
                }
            }

            @Override
            public void onPermissionFailure(int requestCode) {
                Log.e("PermissionUtils", "授权回调结果111：失败");
                if (requestCode == PermissionUtils.REQUESTCODE_READ_EXTERNAL_STORAGE) {
                    PermissionUtils.showRequest(MainActivity.this, PermissionUtils.PERMISSION_READ_EXTERNAL_STORAGE);
                } else if (requestCode == PermissionUtils.REQUESTCODE_CAMERA) {
                    PermissionUtils.showRequest(MainActivity.this, PermissionUtils.PERMISSION_CAMERA);
                }
            }
        });
    }

    @Override
    public void onClickDialog(int pos) {
        switch (pos) {
            case MyBottomSheetDialog.CAMERA:
                if (PermissionUtils.setPermission(MainActivity.this, PermissionUtils.PERMISSION_CAMERA, PermissionUtils.REQUESTCODE_CAMERA)) {
                    albunPhotoHelper.openCamera();
                }
                myBottomSheetDialog.dismiss();
                break;
            case MyBottomSheetDialog.ALBUM:
                if (PermissionUtils.setPermission(MainActivity.this, PermissionUtils.PERMISSION_READ_EXTERNAL_STORAGE, PermissionUtils.REQUESTCODE_READ_EXTERNAL_STORAGE)) {
                    albunPhotoHelper.openAlbum();
                }
                myBottomSheetDialog.dismiss();
                break;
            case MyBottomSheetDialog.CANCLE:
                myBottomSheetDialog.dismiss();
                break;
        }
    }
}
