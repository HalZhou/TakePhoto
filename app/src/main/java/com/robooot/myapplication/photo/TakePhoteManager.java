package com.robooot.myapplication.photo;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.robooot.myapplication.BuildConfig;

import java.io.File;

public class TakePhoteManager {

    private static final String TAG = TakePhoteManager.class.getSimpleName();

    private static final String CAMERA_PHOTO_PATH = "temp_camera_image.jpg";
    private static final String CLIP_PHOTO_PATH = "temp_clip_image.jpg";

    private static final String PRIMARY = "primary";
    private static final String IMAGE = "image";
    private static final String VIDEO = "video";
    private static final String AUDIO = "audio";
    private static final String CONTENT = "content";
    private static final String FILE = "file";

    /**
     * 上下文
     */
    private Activity activity = null;

    /**
     * 图片的存储地址 - 父路径
     */
    private String mRootPath = null;

    /**
     * 原图的保存地址
     */
    private Uri imageUri = null;

    /**
     * 设置剪裁参数
     */
    private TakePhotoValue takePhotoValue = null;

    /**
     * 回调图片监听
     */
    private OnTakePhotoListener listener;

    public TakePhoteManager(Activity activity, OnTakePhotoListener listener) {
        this.activity = activity;
        this.mRootPath = FileUtils.getRootPath(activity) + File.separator + "photo";
        this.listener = listener;
    }

    /**
     * 设置是否剪裁
     *
     * @param width
     * @param height
     * @param cutting
     */
    public void setTakePhotoValue(int width, int height, int cutting) {
        this.takePhotoValue = new TakePhotoValue(width, height, cutting);
    }

    /**
     * 请求相机拍照
     */
    public void requestTakePhoto() {
        PermissionsUtil.requestPermissions(activity, PermissionsUtil.PERMISSION_CAMER_REQUEST_CODE,
                PermissionsUtil.CAMERA_PERMISSION,
                new PermissionsUtil.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        if (PermissionsUtil.isCameraEnable()) {
                            takePhoto();
                        }
                    }

                    @Override
                    public void onPermissionDenied(String[] deniedPermissions, boolean alwayDenied) {
                        System.out.println(deniedPermissions);
                    }
                });
    }

    private void takePhoto() {

        if (CommonUtils.isStringInvalid(mRootPath))
            return;

        File outDir = new File(mRootPath);

        if (!outDir.exists())
            outDir.mkdirs();

        File file = new File(mRootPath, CAMERA_PHOTO_PATH);
        Intent intentFormCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intentFormCapture.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            imageUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            imageUri = Uri.fromFile(file);
        }

        intentFormCapture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intentFormCapture, Constants.TAKE_PHOTO_REQUEST_CODE);

    }

    /**
     * 请求获取本地图片
     */
    public void requestPickPhoto() {
        PermissionsUtil.requestPermissions(activity, PermissionsUtil.PERMISSION_STORAGE_REQUEST_CODE,
                PermissionsUtil.STORAGE_PERMISSION,
                new PermissionsUtil.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        pickPhoto();
                    }

                    @Override
                    public void onPermissionDenied(String[] deniedPermissions, boolean alwayDenied) {

                    }
                });
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(intent, Constants.PICK_PHOTO_REQUEST_CODE);
    }

    private void requestClipPhoto(Context context, Uri uri) {
        if (uri == null || context == null) {
            return;
        }

        Intent intent = new Intent("com.android.camera.action.CROP");
        String imagePath = getImagePath(activity, uri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", new File(imagePath));
            intent.setDataAndType(contentUri, "image/*");
        } else {
            intent.setDataAndType(uri, "image/*");
        }

        int clipWidth = takePhotoValue.width;
        int clipHeight = takePhotoValue.height;
        if (clipWidth != 0 && clipHeight != 0) {
            intent.putExtra("aspectX", clipWidth);
            intent.putExtra("aspectY", clipHeight);
            //当图片大小< 剪裁大小 时，图片没有那么大系统会自动给我们添加黑色背景用于剪裁，以下两个参数不添加，可以避免出现这种情况；
//            intent.putExtra("outputX", clipWidth);
//            intent.putExtra("outputY", clipHeight);
        }

        File outDir = new File(mRootPath);
        if (!outDir.exists())
            outDir.mkdirs();

        File clipFile = new File(mRootPath, CLIP_PHOTO_PATH);
        if (clipFile.exists())
            clipFile.delete();

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(clipFile));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("return-data", false);
        activity.startActivityForResult(intent, Constants.CLIP_PHOTO_REQUEST_CODE);

    }

    /**
     * 获取图片地址
     * @param context
     * @param imageUri
     * @return
     */
    private String getImagePath(Context context, Uri imageUri) {

        if (context == null || imageUri == null)
            return null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context,imageUri)){

            if (isExternalStorageDocument(imageUri)){

                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if (PRIMARY.equalsIgnoreCase(type)){
                    return Environment.getExternalStorageDirectory()+"/"+split[1];
                }
            }else if (isDownloadsDocument(imageUri)){
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(id));
                return getDataColumn(context,contentUri,null,null);
            }else if (isMediaDocument(imageUri)){

                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;

                if (IMAGE.equals(type)){
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }else if (VIDEO.equals(type)){
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }else if (AUDIO.equals(type)){
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                String selection = MediaStore.Images.Media._ID+"=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context,contentUri,selection,selectionArgs);
            }
        }else if (CONTENT.equalsIgnoreCase(imageUri.getScheme())){
            if (isGooglePhotoUri(imageUri)){
                return imageUri.getLastPathSegment();
            }
            return getDataColumn(context,imageUri,null,null);
        }else if (FILE.equalsIgnoreCase(imageUri.getScheme())){
            return imageUri.getPath();
        }

        return null;
    }

    /**
     * 获取文件路径
     * @param context
     * @param contentUri
     * @param selection
     * @param selectionArgs
     * @return
     */
    private String getDataColumn(Context context, Uri contentUri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(contentUri,projection,selection,selectionArgs,null);

            if (cursor != null && cursor.moveToFirst()){
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor!=null)
                cursor.close();
        }

        return null;
    }

    private boolean isDownloadsDocument(Uri imageUri) {
        return "com.android.providers.downloads.documents".equals(imageUri.getAuthority());
    }

    private boolean isExternalStorageDocument(Uri imageUri) {
        return "com.android.externalstorage.documents".equals(imageUri.getAuthority());
    }

    private boolean isGooglePhotoUri(Uri imageUri) {
        return "com.google.android.apps.photos.content".equals(imageUri.getAuthority());
    }

    private boolean isMediaDocument(Uri imageUri) {
        return "com.android.providers.media.documents".equals(imageUri.getAuthority());
    }

    public Object onActivityResult(Context context, int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_CANCELED)
            return null;

        switch (requestCode) {
            case Constants.PICK_PHOTO_REQUEST_CODE:
                Uri imageUri = data.getData();
                if (takePhotoValue != null && takePhotoValue.cutting == 1) {
                    requestClipPhoto(context, imageUri);
                } else {
                    getPhotoPath(context, imageUri);
                }
                break;
            case Constants.TAKE_PHOTO_REQUEST_CODE:
                File tempFile = new File(mRootPath, CAMERA_PHOTO_PATH);
                if (takePhotoValue != null && takePhotoValue.cutting == 1) {
                    requestClipPhoto(context, Uri.fromFile(tempFile));
                } else {
                    getPhotoPath(context, tempFile.getAbsolutePath());
                }
                break;
            case Constants.CLIP_PHOTO_REQUEST_CODE:
                takePhotoValue = null;
                File clipFile = new File(mRootPath, CLIP_PHOTO_PATH);
                if (clipFile == null || !clipFile.exists())
                    return null;

                getPhotoPath(context, clipFile.getAbsolutePath());
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * 获取图片地址
     * @param context
     * @param filePath
     */
    private void getPhotoPath(Context context, String filePath) {

        if (listener != null && context != null){
            listener.onTakePath(filePath);
        }

    }

    private void getPhotoPath(Context context, Uri imageUri) {

        if (imageUri == null){
            return;
        }

        String imagePath = getImagePath(context,imageUri);
        if (CommonUtils.isStringInvalid(imagePath))
            return;

        getPhotoPath(context,imagePath);
    }


    public static class TakePhotoValue {
        /**
         * 剪裁图片宽度
         */
        public int width;

        /**
         * 剪裁图片高度
         */
        public int height;

        /**
         * 是否被剪裁 1 剪裁 0 不剪裁
         */
        public int cutting = 1;

        /**
         * 是否为本地图片 1拍照后图片 0 为本地图片
         */
        public int from;

        public TakePhotoValue(int width, int height, int cutting) {
            this.width = width;
            this.height = height;
            this.cutting = cutting;
        }
    }
}
