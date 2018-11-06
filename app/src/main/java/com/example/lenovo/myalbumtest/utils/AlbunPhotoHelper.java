package com.example.lenovo.myalbumtest.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.xq.myandroid7.FileProvider7;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by lenovo on 2018/11/6.
 */

public class AlbunPhotoHelper {

    public static final int REQ_TAKE_PHOTO = 100;
    public static final int REQ_ALBUM = 101;
    public static final int REQ_ZOOM = 102;

    public static String[] REQUEST_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private Activity activity;
    private Uri outputUri;


    public AlbunPhotoHelper(Activity activity) {
        this.activity = activity;
    }

    /*---------------------------------------------------------------------------------------------*/

    /**
     * 打开相册：
     */
    public void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(intent, REQ_ALBUM);
    }

    /**
     * 打开相机：适配 Android 7.0
     */
    public void openCamera() {
        // 指定调用相机拍照后照片的储存路径
        File file = FileUtils.getTempFile(activity);
        //获取Uri:适配7.0
        Uri imgUri = FileProvider7.getUriForFile(activity, file);

        //跳转相机
        Intent intent2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent2.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);//拍照才需要传入URI
        activity.startActivityForResult(intent2, REQ_TAKE_PHOTO);
    }

    /*---------------------------------------------------------------------------------------------*/

    public Intent cutForCamera() {
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

    public Intent cutForPhoto(Uri uri) {
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
    public Uri getInputUri(Intent intent) {
        //tempFile需要与拍照openCamera传入的文件一致
        File tempFile = FileUtils.getTempFile(activity);
        if (Build.VERSION.SDK_INT >= 24) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }

        return FileProvider7.getUriForFile(activity, tempFile);
    }

    public void getOutputUri(String imgName) throws IOException {
        //设置裁剪之后的图片路径文件
        File cutfile = new File(Environment.getExternalStorageDirectory().getPath(), imgName);
        if (cutfile.exists()) {
            cutfile.delete();
        }
        cutfile.createNewFile();

        outputUri = Uri.fromFile(cutfile);
    }

    /*
    * aspectX aspectY 是裁剪图片宽高的【比例】
    * outputX outputY 是裁剪图片宽高的【像素】
    * */
    public void setIntent(Intent intent, Uri inputUri, Uri outputUri) {
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", "150");
        intent.putExtra("outputY", "150");
        intent.putExtra("scale", true);//是否保留比例
        intent.putExtra("return-data", false);//true返回bitmap，false返回URI
        if (inputUri != null) {
            intent.setDataAndType(inputUri, "image/*");
        }
        if (outputUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);//将URI指向相应的file
        }
        //是否取消人脸识别功能
        intent.putExtra("noFaceDetection", false);
        //输出格式，一般设为Bitmap格式：Bitmap.CompressFormat.JPEG.toString()
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
    }

    /*--------------------------------------------------------------------------------------------*/

    public Bitmap getBitmap() throws FileNotFoundException {
        return BitmapFactory.decodeStream(activity.getContentResolver().openInputStream(outputUri));
    }


}
