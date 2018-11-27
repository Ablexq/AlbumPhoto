package com.example.lenovo.myalbumtest.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.example.lenovo.myalbumtest.MyApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    public static final String ROOT_DIR = "Android/data/" + MyApplication.getInstance().getPackageName();
    public static final String DOWNLOAD_DIR = "download";
    public static final String CACHE_DIR = "cache";
    public static final String ICON_DIR = "icon";

    public static final String APP_STORAGE_ROOT = "AndroidNAdaption";

    /**
     * 判断SD卡是否挂载
     */
    public static boolean isSDCardAvailable() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取下载目录
     */
    public static String getDownloadDir() {
        return getDir(DOWNLOAD_DIR);
    }

    /**
     * 获取缓存目录
     */
    public static String getCacheDir() {
        return getDir(CACHE_DIR);
    }

    /**
     * 获取icon目录
     */
    public static String getIconDir() {
        return getDir(ICON_DIR);
    }

    /**
     * 获取应用目录，当SD卡存在时，获取SD卡上的目录，当SD卡不存在时，获取应用的cache目录
     */
    public static String getDir(String name) {
        StringBuilder sb = new StringBuilder();
        if (isSDCardAvailable()) {
            sb.append(getAppExternalStoragePath());
        } else {
            sb.append(getCachePath());
        }
        sb.append(name);
        sb.append(File.separator);
        String path = sb.toString();
        if (createDirs(path)) {
            return path;
        } else {
            return null;
        }
    }

    /**
     * 获取SD下的应用目录
     */
    public static String getExternalStoragePath() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        sb.append(ROOT_DIR);
        sb.append(File.separator);
        return sb.toString();
    }

    /**
     * 获取SD下当前APP的目录
     */
    public static String getAppExternalStoragePath() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        sb.append(APP_STORAGE_ROOT);
        sb.append(File.separator);
        return sb.toString();
    }

    /**
     * 获取应用的cache目录
     */
    public static String getCachePath() {
        File f = MyApplication.getInstance().getCacheDir();
        if (null == f) {
            return null;
        } else {
            return f.getAbsolutePath() + "/";
        }
    }

    /**
     * 创建文件夹
     */
    public static boolean createDirs(String dirPath) {
        File file = new File(dirPath);
        if (!file.exists() || !file.isDirectory()) {
            return file.mkdirs();
        }
        return true;
    }

    /**
     * 产生图片的路径，这里是在缓存目录下
     */
    public static String generateImgePathInStoragePath() {
        return getDir(ICON_DIR) + String.valueOf(System.currentTimeMillis()) + ".jpg";
    }

    /**
     * 发起剪裁图片的请求
     *
     * @param activity    上下文
     * @param srcFile     原文件的File
     * @param output      输出文件的File
     * @param requestCode 请求码
     */
    public static void startPhotoZoom(Activity activity, File srcFile, File output, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(getImageContentUri(activity, srcFile), "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", 480);
        intent.putExtra("outputY", 480);

        // true:返回uri，false：不返回uri
        intent.putExtra("return-data", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(output));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        activity.startActivityForResult(intent, requestCode);
    }

    public static File setCacheFile(Context context) {//注意：存在则删除
        if (!FileUtils.isSDCardAvailable()) {
            ToastUtil.showToast(context, "很抱歉，你的手机没内存卡！");
            return null;
        }

        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            System.out.println("============getExternalStorageDirectory==============");
            cacheDir = Environment.getExternalStorageDirectory();
        }
        File file = new File(cacheDir.getAbsolutePath() + "/DAI_CAI_HANG");
        if (file.exists()) {
            deleteFile(file);
        }
        file.mkdirs();
        String file_cache = file + "/tempImg.jpg";
        System.out.println("file_cache====================="+file_cache);
        return new File(file_cache);
    }

    public static File getTempFile(Context context) {
        if (!FileUtils.isSDCardAvailable()) {
            ToastUtil.showToast(context, "很抱歉，你的手机没内存卡！");
            return null;
        }

        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = Environment.getExternalStorageDirectory();
        }
        File file = new File(cacheDir.getAbsolutePath() + "/DAI_CAI_HANG");
        //String file_tmp = file + "/" + System.currentTimeMillis() + ".jpg";
        String file_tmp = file + "/tempImg.jpg";
        System.out.println("file_tmp====================="+file_tmp);
        return new File(file_tmp);
    }

    //flie：要删除的文件夹的所在位置
    private static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
            file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 返回bitmap
     */
    public static void cropPhoto(Activity activity, Uri uri, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 返回URI
     */
    public static void cropPhoto2(Activity activity, Uri uri, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 安卓7.0相机 裁剪根据文件路径获取uri
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * 复制bm
     *
     * @param bm
     * @return
     */
    public static String saveBitmap(Bitmap bm) {
        String croppath = "";
        try {
            File f = new File(FileUtils.generateImgePathInStoragePath());
            //得到相机图片存到本地的图片
            croppath = f.getPath();
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return croppath;
    }

    /**
     * 按质量压缩bm
     *
     * @param bm
     * @param quality 压缩率
     * @return
     */
    public static String saveBitmapByQuality(Bitmap bm, int quality) {
        String croppath = "";
        try {
            File f = new File(FileUtils.generateImgePathInStoragePath());
            //得到相机图片存到本地的图片
            croppath = f.getPath();
            if (f.exists()) {
                f.delete();
            }
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return croppath;
    }

    public static Bitmap compressQuality(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        System.out.println("t============"+(baos.toByteArray().length / 1024));
        while (baos.toByteArray().length / 1024 > 100) { //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        return BitmapFactory.decodeStream(isBm, null, null);
    }
}
