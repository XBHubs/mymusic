package com.example.mymusic.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
//import android.support.v4.app.ActivityCompat;
import androidx.core.app.ActivityCompat;

public class PermissionUtils {
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
    public static boolean isGranted(Context context, String permission) {
        return !isMarshmallow() || isGrantedPermission(context, permission);
    }

//    public static void reqPermission(Activity context, String permission, int reqCode) {
//        if (!isGranted(context, permission)) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
//
//            } else {
//                ActivityCompat.requestPermissions(context, new String[]{permission}, reqCode);
//            }
//        }
//    }

    public static boolean isGrantedPermission(Context context, String permission) {
        int checkSelfPermission = ActivityCompat.checkSelfPermission(context, permission);
        return checkSelfPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
