package com.example.lenovo.myalbumtest;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.lenovo.myalbumtest.utils.AlbunPhotoHelper;
import com.example.lenovo.myalbumtest.utils.BitmapUtils;
import com.example.lenovo.myalbumtest.utils.FileUtils;
import com.example.lenovo.myalbumtest.utils.ImageUtils;
import com.example.lenovo.myalbumtest.utils.PermissionUtils;
import com.example.lenovo.myalbumtest.views.CircleImageView;
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
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView circleImage;
    private Button btnAlbum;
    private Button btnCamera;
    private AlbunPhotoHelper albunPhotoHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleImage = ((CircleImageView) this.findViewById(R.id.circleImage));
        btnAlbum = ((Button) this.findViewById(R.id.btn_album));
        btnCamera = ((Button) this.findViewById(R.id.btn_camera));

        albunPhotoHelper = new AlbunPhotoHelper(this);
        if (PermissionUtils.setPermission(this, AlbunPhotoHelper.REQUEST_PERMISSIONS, PermissionUtils.REQUESTCODE_MULTI)) {
            btnAlbum.setOnClickListener(this);
            btnCamera.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_album:
                albunPhotoHelper.openAlbum();
                break;

            case R.id.btn_camera:
                albunPhotoHelper.openCamera();
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
                        Bitmap bitmap = albunPhotoHelper.getBitmap();

                        if (bitmap != null) {
                            ImageUtils.saveImageToLocal(this, bitmap);//保存在SD卡中
                            circleImage.setImageBitmap(bitmap);//用ImageView显示出来
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
                btnAlbum.setOnClickListener(MainActivity.this);
                btnCamera.setOnClickListener(MainActivity.this);
            }

            @Override
            public void onPermissionFailure(int requestCode) {
                Log.e("PermissionUtils", "授权回调结果111：失败");
                if (requestCode == PermissionUtils.REQUESTCODE_SINGLE) {
                    PermissionUtils.showRequest(MainActivity.this, AlbunPhotoHelper.REQUEST_PERMISSIONS);
                }
            }
        });
    }


}
