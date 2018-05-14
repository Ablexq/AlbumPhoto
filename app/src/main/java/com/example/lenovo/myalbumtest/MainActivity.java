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

    private String[] requestPermissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final int REQ_TAKE_PHOTO = 100;
    public static final int REQ_ALBUM = 101;
    public static final int REQ_ZOOM = 102;
    private Uri outputUri;

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
                    startActivityForResult(cutForPhoto(data.getData()), REQ_ZOOM);
                }
                break;

            case REQ_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    startActivityForResult(cutForCamera(), REQ_ZOOM);
                }
                break;

            case REQ_ZOOM:
                if (data != null) {
                    try {
                        //获取裁剪后的图片，并显示出来
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(outputUri));

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

    private Intent cutForCamera() {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            getOutputUri("cut_camera.png");
            setIntent(intent, getInputUri(intent), outputUri);
            return intent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Intent cutForPhoto(Uri uri) {
        try {
            Intent intent = new Intent("com.android.camera.action.CROP");
            getOutputUri("cut_photo.png");
            setIntent(intent, uri, outputUri);
            return intent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 拍照才需要
     */
    private Uri getInputUri(Intent intent) {
        //tempFile需要与拍照openCamera传入的文件一致
        File tempFile = FileUtils.getTempFile(this);
        if (Build.VERSION.SDK_INT >= 24) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        return FileProvider7.getUriForFile(this, tempFile);
    }

    private void getOutputUri(String imgName) throws IOException {
        //设置裁剪之后的图片路径文件
        File cutfile = new File(Environment.getExternalStorageDirectory().getPath(), imgName);
        if (cutfile.exists()) {
            cutfile.delete();
        }
        cutfile.createNewFile();

        outputUri = Uri.fromFile(cutfile);
    }

    private void setIntent(Intent intent, Uri inputUri, Uri outputUri) {
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", "150");
        intent.putExtra("outputY", "150");
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);//true返回bitmap，false返回URI
        if (inputUri != null) {
            intent.setDataAndType(inputUri, "image/*");
        }
        if (outputUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        }
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
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
        Uri imgUri = FileProvider7.getUriForFile(this, file);

        //跳转相机
        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent2.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);//拍照才需要传入URI
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
