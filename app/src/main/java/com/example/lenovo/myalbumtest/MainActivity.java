package com.example.lenovo.myalbumtest;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.lenovo.myalbumtest.utils.FileUtils;
import com.example.lenovo.myalbumtest.utils.ImageUtils;
import com.example.lenovo.myalbumtest.utils.PermissionUtils;
import com.example.lenovo.myalbumtest.views.CircleImageView;

import java.io.File;


@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView circleImage;
    private Button btnAlbum;
    private Button btnCamera;

    private String[] requestPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final int REQ_TAKE_PHOTO = 100;
    public static final int REQ_ALBUM = 101;
    public static final int REQ_ZOOM = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleImage = ((CircleImageView) this.findViewById(R.id.circleImage));
        btnAlbum = ((Button) this.findViewById(R.id.btn_album));
        btnCamera = ((Button) this.findViewById(R.id.btn_camera));

        if (PermissionUtils.setPermission(this, requestPermissions, PermissionUtils.REQUESTCODE_MULTI)) {
            btnAlbum.setOnClickListener(this);
            btnCamera.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_album:
                openAlbum();
                break;

            case R.id.btn_camera:
                openCamera();
                break;
        }
    }


    @SuppressWarnings("deprecation")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_ALBUM:
                if (resultCode == RESULT_OK) {
                    FileUtils.cropPhoto(this, data.getData(), REQ_ZOOM);//裁剪图片
                }
                break;

            case REQ_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    File temp = FileUtils.getTempFile(this);
                    FileUtils.cropPhoto(this, FileUtils.getImageContentUri(this, temp), REQ_ZOOM);//裁剪图片
                }
                break;

            case REQ_ZOOM:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    Bitmap bitmap = extras.getParcelable("data");
                    if (bitmap != null) {
                        /**
                         * 上传服务器代码
                         */
                        ImageUtils.saveImageToLocal(this, bitmap);//保存在SD卡中
                        circleImage.setImageBitmap(bitmap);//用ImageView显示出来
                        if (bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                    }
                }
                break;
            default:
                break;
        }

    }


    /**
     * 打开相册：
     */
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQ_ALBUM);
    }

    /**
     * 打开相机：适配 Android 7.0
     */
    private void openCamera() {
        // 指定调用相机拍照后照片的储存路径
        File file = FileUtils.getTempFile(this);

        //获取Uri:适配7.0
        Uri imgUri;
        if (Build.VERSION.SDK_INT >= 24) {
            //如果是7.0或以上
            imgUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", file);
        } else {
            imgUri = Uri.fromFile(file);
        }

        //跳转相机
        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent2.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(intent2, REQ_TAKE_PHOTO);
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
                    PermissionUtils.showRequest(MainActivity.this, requestPermissions);
                }
            }
        });
    }


}
