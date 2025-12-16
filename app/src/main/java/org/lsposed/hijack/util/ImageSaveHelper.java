package org.lsposed.hijack.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageSaveHelper {


    private Context context;

    public static final int REQUEST_CODE_PERMISSIONS_ANDROID_13 = 1001;
    public static final int REQUEST_CODE_PERMISSIONS_LEGACY = 1002;

    public ImageSaveHelper(Context context) {
        this.context = context;
    }

    public interface SaveCallback {
        void onSuccess(String imagePath);
        void onError(String errorMessage);
    }

    /** 保存图片到相册 */
    public void saveImageToGallery(Bitmap bitmap, String fileName, SaveCallback callback) {
        try {
            if (!checkAndRequestPermissions(callback)) {
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ 使用 MediaStore
                saveImageWithMediaStore(bitmap, fileName, callback);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0 - 9 使用传统方式
                saveImageLegacy(bitmap, fileName, callback);
            } else {
                // Android 6.0以下直接保存
                saveImageLegacy(bitmap, fileName, callback);
            }
        } catch (Throwable e) {
            callback.onError("保存失败: " + e.getMessage());
        }
    }

    /** 检查并请求权限 */
    private boolean checkAndRequestPermissions(SaveCallback callback) {
        if (shouldRequestPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ 检查媒体权限
                if (!hasReadMediaImagesPermission(context)) {
                    requestPermissionsForAndroid13();
                    callback.onError("需要授予相册访问权限");
                    return false;
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0 - 12 检查存储权限
                if (!hasWriteExternalStoragePermission(context)) {
                    requestPermissionsLegacy();
                    callback.onError("需要授予存储权限");
                    return false;
                }
            }
        }
        return true;
    }

    // ==================== 权限相关方法 ====================

    /** 检查是否需要运行时权限 */
    public static boolean shouldRequestPermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /** 检查是否有写入外部存储权限（Android 9及以下） */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean hasWriteExternalStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 使用作用域存储，不需要WRITE_EXTERNAL_STORAGE
            return true;
        } else {
            return context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /** 检查是否有读取媒体权限（Android 13+） */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static boolean hasReadMediaImagesPermission(Context context) {
        return context.checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
    }

    /** 获取需要请求的权限列表 */
    public static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            return new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
            };
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 - 12
            return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        } else {
            // Android 6.0以下，不需要运行时权限
            return new String[]{};
        }
    }

    /** Android 13+ 权限请求 */
    private void requestPermissionsForAndroid13() {
        if (context instanceof Activity) {
            String[] permissions = getRequiredPermissions();
            ActivityCompat.requestPermissions((Activity) context, permissions, REQUEST_CODE_PERMISSIONS_ANDROID_13);
        }
    }

    /** Android 6.0-12 权限请求 */
    private void requestPermissionsLegacy() {
        if (context instanceof Activity) {
            String[] permissions = getRequiredPermissions();
            ActivityCompat.requestPermissions((Activity) context, permissions, REQUEST_CODE_PERMISSIONS_LEGACY);
        }
    }

    // ==================== 图片保存相关方法 ====================

    /** Android 10+ 使用 MediaStore 保存图片 */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void saveImageWithMediaStore(Bitmap bitmap, String fileName, SaveCallback callback) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);

        Uri imageUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (imageUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = context.getContentResolver().openOutputStream(imageUri);
                if (outputStream != null) {
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                        contentValues.clear();
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                        context.getContentResolver().update(imageUri, contentValues, null, null);
                        callback.onSuccess(imageUri.toString());
                    } else {
                        throw new IOException("压缩图片失败");
                    }
                }
            } catch (Throwable e) {
                context.getContentResolver().delete(imageUri, null, null);
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable e) {}
                }
            }
        }
    }

    /** Android 6.0-9 传统方式保存图片 */
    private void saveImageLegacy(Bitmap bitmap, String fileName, SaveCallback callback) {
        File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!picturesDir.exists()) {
            picturesDir.mkdirs();
        }

        File imageFile = new File(picturesDir, fileName);
        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(imageFile);
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                // 通知系统图库刷新
                MediaScannerConnection.scanFile(context,
                        new String[]{imageFile.getAbsolutePath()},
                        new String[]{"image/jpeg"},
                        (String path, Uri uri) -> callback.onSuccess(path != null ? path : ""));
            } else {
                return;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable e) {
                }
            }
        }
    }

    /** 检查所有必需权限是否已授予 */
    public boolean hasAllRequiredPermissions() {
        if (!shouldRequestPermission()) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return hasReadMediaImagesPermission(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return hasWriteExternalStoragePermission(context);
        }
        return true;
    }
}

