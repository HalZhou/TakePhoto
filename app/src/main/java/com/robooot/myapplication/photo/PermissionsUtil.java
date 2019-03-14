package com.robooot.myapplication.photo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PermissionsUtil {

    /**
     * 存储权限
     */
    public static final String[] STORAGE_PERMISSION = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 相机权限
     */
    public static final String[] CAMERA_PERMISSION = {
          Manifest.permission.CAMERA
    };

    /**
     * 定位权限组
     */
    public static final String[] LOC_PERMISSIONS = {
       Manifest.permission.ACCESS_COARSE_LOCATION,
       Manifest.permission.ACCESS_FINE_LOCATION,
       Manifest.permission.WRITE_EXTERNAL_STORAGE,
       Manifest.permission.READ_EXTERNAL_STORAGE,
       Manifest.permission.READ_PHONE_STATE
    };

    /**
     * 拨打电话权限
     */
    public static final String[] CALL_PHONE_PERMISSION = {
      Manifest.permission.CALL_PHONE
    };

    /**
     * 获取电话权限
     */
    public static final String[] READ_PHONE_STATE_PERMISSION = {
      Manifest.permission.READ_PHONE_STATE
    };

    /**
     * 发送短信权限
     */
    public static final String[] SEND_SMS_PERMISSION = {
      Manifest.permission.SEND_SMS
    };

    /**
     * 获取网络相关功能
     */
    public static final String[] NETWORK_PERMISSION = {
      Manifest.permission.ACCESS_NETWORK_STATE,
      Manifest.permission.ACCESS_WIFI_STATE
    };


    public static final int PERMISSION_CAMER_REQUEST_CODE = 1;
    public static final int PERMISSION_STORAGE_REQUEST_CODE = 2;
    public static final int PERMISSION_LOC_REQUEST_CODE = 3;
    public static final int PERMISSION_CALL_PHONE_REQUEST_CODE = 4;
    public static final int PERMISSION_SEND_SMS_REQUEST_CODE = 5;
    public static final int PERMISSION_READ_PHONE_REQUEST_CODE = 6;

    private static int mRequestCode = -1;
    private static WeakReference<OnPermissionListener> mOnPermissionListener = null;

    public interface OnPermissionListener{
        /**
         * 权限允许
         */
        void onPermissionGranted();

        /**
         * 权限拒绝
         */
        void onPermissionDenied(String[] deniedPermissions, boolean alwayDenied);
    }

    public static void requestPermissions(Context context,int requestCode,
                                          String[] permissions,
                                          OnPermissionListener listener){
        mRequestCode = requestCode;
        mOnPermissionListener = new WeakReference<>(listener);

        String[] deniedPermissions = getDeniedPermissions(context,permissions);

        if (deniedPermissions.length>0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissionAgain(context,permissions,requestCode);
        }else {
            if (mOnPermissionListener.get() != null){
                mOnPermissionListener.get().onPermissionGranted();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void requestPermissionAgain(Context context,String[] permissions,int requestCode){
        if (context instanceof Activity){
            ((Activity)context).requestPermissions(permissions,requestCode);
        }else {
            throw new IllegalArgumentException("Context must be an Activity");
        }
    }

    private static String[] getDeniedPermissions(Context context, String[] permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions){
            if (ContextCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED){
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions.toArray(new String[deniedPermissions.size()]);
    }

    public static void onRequestPermissionsResult(Activity context,int requestCode,
                                                  String[] permissions,int[] grantResult){
        if (mRequestCode != -1 && requestCode == mRequestCode){
            if (mOnPermissionListener.get()!=null){
                String[] deniedPermissions = getDeniedPermissions(context,permissions);
                if (deniedPermissions.length>0){
                    boolean alwayDenied = hasAlwaysDeniedPermissions(context,permissions);
                    mOnPermissionListener.get().onPermissionDenied(permissions,alwayDenied);
                }else {
                    mOnPermissionListener.get().onPermissionGranted();
                }
            }
        }
    }

    /**
     * 是否彻底拒绝了某项权限
     * @param context
     * @param permissions
     * @return
     */
    private static boolean hasAlwaysDeniedPermissions(Context context,String[] permissions){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return false;
        }

        boolean rationale;

        for (String permission : permissions){
            rationale = ActivityCompat.shouldShowRequestPermissionRationale((Activity) context,permission);

            if (!rationale){
                return true;
            }
        }

        return false;
    }

    /**
     * 检查是否有某项权限
     */
    public static boolean checkSelfPermission(Context context,String permission){
        if (ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    /**
     * 判断某个权限组是否开启
     */
    public static boolean checkSelfPermissions(Context context,String... permissions){
        for (String permission : permissions){
            if (!checkSelfPermission(context,permission)){
                return false;
            }
        }
        return true;
    }

    /**
     * 6.0以下 判断相机权限
     */
    public static boolean isCameraEnable(){
        boolean isCanUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            //针对魅族手机
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        }catch (Exception e){
            e.printStackTrace();
            isCanUse = false;
        }

        if (mCamera != null){
            try {
                mCamera.release();
            }catch (Exception e){
                e.printStackTrace();
                return isCanUse;
            }
        }

        return isCanUse;
    }

    /**
     * 获取应用设置界面
     */
    public static void skipAppSettingPage(Context context){
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package",context.getPackageName(),null));
        }else {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }

        context.startActivity(intent);
    }

}
