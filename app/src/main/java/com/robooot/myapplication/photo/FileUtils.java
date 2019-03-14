package com.robooot.myapplication.photo;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.widget.Button;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();
    public static final String TEMP_FILE_SUFFIX = ".tmp";
    /**
     *
     * 目前只检查极限值 容量不为0的
     *
     * */
    private static final long MIN_SPACE = 0;

    private static String mRootPath;

    /**
     * 通过字符串获取唯一KEY
     * @param url
     * */
    /*public static String genKeyForUrl(String url) {
        if (url == null) {
            return null;
        }

        String cacheKey = MD5Utils.getStringMD5(url.trim());
        if (CommonUtils.isStringInvalid(cacheKey)) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }*/

    /**
     * 通过文件获取唯一KEY
     *
     * */
    /*public static String genKeyForUrl(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        String cacheKey = MD5Utils.getFileMD5(file);
        if (CommonUtils.isStringInvalid(cacheKey)) {
            cacheKey = String.valueOf(file.hashCode());
        }
        return cacheKey;
    }*/

    private static String getRootPathWithSdcard(Context context) {
        StringBuilder builder = new StringBuilder();
        File dirFile = Environment.getExternalStorageDirectory();
        if (dirFile == null || !dirFile.canWrite()) {
            return null;
        }

        builder.append(dirFile.getAbsolutePath()).append(File.separator).append("Download").append(File.separator)
                .append(context.getPackageName());
        File file = new File(builder.toString());
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file.canWrite()) {
            return null;
        }
        return file.getAbsolutePath();
    }

    private static String getRootPathWithoutSdcard(Context context) {
        String rootPath = context.getCacheDir().getAbsolutePath() + File.separator + "apps";
        String[] args1 = { "chmod", "705", rootPath };
        CommonUtils.exec(args1);
        return rootPath;
    }

    public static void resetRootPath(Context context) {
        mRootPath = null;
        getRootPath(context);
    }

    public static synchronized String getRootPath(Context context) {
        if (mRootPath == null) {
            String rootPath = null;
            try {
                if (rootPath == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    rootPath = getRootPathForApi19(context);
                    if (!canCreateFile(rootPath)) {
                        rootPath = null;
                    }
                }

                if (rootPath == null) {
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                            && hasEnoughSpaceOnSDCard()) {
                        rootPath = getRootPathWithSdcard(context);
                        if (!canCreateFile(rootPath)) {
                            rootPath = null;
                        }
                    }
                }

                if (rootPath == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    rootPath = getRootPathForApi19(context);
                    if (!canCreateFile(rootPath)) {
                        rootPath = null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (rootPath == null) {
                rootPath = getRootPathWithoutSdcard(context);
            }
            mRootPath = rootPath;
        } else if (mRootPath.startsWith(context.getCacheDir().getAbsolutePath())){
            String[] args1 = { "chmod", "705", mRootPath };
            CommonUtils.exec(args1);
        }
        return mRootPath;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getRootPathForApi19(Context context) {
        String rootPath = null;
        File[] fs = context.getExternalFilesDirs(null);

        if (fs == null || fs.length == 0) {
            return null;
        }

        ArrayList<File> fileList = new ArrayList<>(fs.length);
        for (File f : fs) {
            if (f != null) {
                if (!f.exists()) {
                    f.mkdirs();
                }
                if (f.isDirectory() && f.canWrite() && f.canRead()) {
                    fileList.add(f);
                }
            }
        }

        if (fileList.size() == 0) {
            return null;
        }

        if (fileList.size() == 1) {
            fileList.get(0).mkdirs();
            rootPath = fileList.get(0).getAbsolutePath();
        } else {
            long max = 0;
            for (int i = 0; i < fileList.size(); i++) {
                long[] l = getStorageSpacesLong(context, fileList.get(i).getAbsolutePath());
                if (l[1] > max) {
                    max = l[1];
                    fileList.get(i).mkdirs();
                    rootPath = fileList.get(i).getAbsolutePath();
                }
            }
        }

        return rootPath;
    }

    private static boolean canCreateFile(String path) {
        if (path == null) {
            return false;
        }

        File file = new File(path, "file.test");
        if (file == null) {
            return false;
        }
        if (file.exists()) {
            file.delete();
            return true;
        } else {
            try {
                boolean b = file.createNewFile();
                file.delete();
                return b;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    /**
     * 获取存储卡的可用空间和大小
     *
     * @return l[0] 可用空间, l[1] 总空间
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long[] getStorageSpacesLong(Context context, String path) {
        String rootPath = getUsbRootPath(context, path);
        File file = new File(rootPath);
        if (!file.exists()) {
            return null;
        }
        long[] l = new long[2];
        StatFs stat = new StatFs(rootPath);
        long blockSize = 0, blockCount = 0, availableBlocks = 0;
        // android 4.3以下支持的API
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSize();
            blockCount = stat.getBlockCount();
            availableBlocks = stat.getAvailableBlocks();
        } else { // android 4.3API
            blockSize = stat.getBlockSizeLong();
            blockCount = stat.getBlockCountLong();
            availableBlocks = stat.getAvailableBlocksLong();
        }
        l[0] = blockSize * availableBlocks;
        l[1] = blockSize * blockCount;
        return l;
    }

    public static String getUsbRootPath(Context context, String path) {
        String usbRootPath;
        String packageName;
        if (path.contains("Android/data")) {
            usbRootPath = path.substring(0, path.indexOf("Android/data"));
        } else if (path.contains(packageName = context.getPackageName())) {
            usbRootPath = path.substring(0, path.indexOf(packageName));
        } else {
            usbRootPath = path;
        }
        return usbRootPath;
    }

    private static boolean hasEnoughSpaceOnSDCard() {
        File file = Environment.getExternalStorageDirectory();
        if (!file.exists() || !file.canWrite() || !file.canRead()) {
            Log.i(TAG, "file is not exists");
            return false;
        }
        StatFs stat = new StatFs(file.getAbsolutePath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        Log.i(TAG, "space : " + blockSize * availableBlocks);
        return blockSize * availableBlocks > MIN_SPACE;
    }

    /**
     * use key as file name
     *
     * @param context
     * @param key
     * @return
     * @author: herry
     * @date: 2014年8月29日 上午11:29:56
     */

    public static File createTempFile(Context context, String key) {
        String rootPath = getRootPath(context);
        File newFile = new File(rootPath, key + TEMP_FILE_SUFFIX);
        if (!newFile.getParentFile().exists()) {
            newFile.getParentFile().mkdirs();
        }
        return newFile;
    }

    public static void renameFile(File tempFile, File newFile) {
        tempFile.renameTo(newFile);
    }

    private static final class FileSearchComp implements FilenameFilter {
        private String key;

        public FileSearchComp(String key) {
            this.key = key;
        }

        @Override
        public boolean accept(File dir, String filename) {
            if (null == key) {
                return false;
            }
            if (null == filename) {
                return false;
            }
            if (filename.startsWith(key)) {
                return true;
            }
            return false;
        }
    }

    public static boolean isCompleteFile(String path) {
        if (CommonUtils.isStringInvalid(path)) {
            return false;
        }
        return !path.endsWith(TEMP_FILE_SUFFIX);
    }

    /**
     * 得到SD卡根目录，SD卡不可用则获取内部存储的根目录
     */
    public static File getRootPath() {
        File path = null;
        if (sdCardIsAvailable()) {
            //SD卡根目录    /storage/emulated/0
            path = Environment.getExternalStorageDirectory();
        } else {
            //内部存储的根目录    /data
            path = Environment.getDataDirectory();
        }
        return path;
    }

    /**
     * SD卡是否可用
     */
    public static boolean sdCardIsAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sd = new File(Environment.getExternalStorageDirectory().getPath());
            return sd.canWrite();
        }
        return false;
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param dirPath 文件路径
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsDir(String dirPath) {
        return createOrExistsDir(getFileByPath(dirPath));
    }

    /**
     * 判断目录是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsDir(File file) {
        // 如果存在，是目录则返回true，是文件则返回false，不存在则返回是否创建成功
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * 判断文件是否存在，不存在则判断是否创建成功
     *
     * @param filePath 文件路径
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsFile(String filePath) {
        return createOrExistsFile(getFileByPath(filePath));
    }

    /**
     * 判断文件是否存在，不存在则判断是否创建成功
     *
     * @param file 文件
     * @return {@code true}: 存在或创建成功<br>{@code false}: 不存在或创建失败
     */
    public static boolean createOrExistsFile(File file) {
        if (file == null)
            return false;
        // 如果存在，是文件则返回true，是目录则返回false
        if (file.exists())
            return file.isFile();
        if (!createOrExistsDir(file.getParentFile()))
            return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据文件路径获取文件
     *
     * @param filePath 文件路径
     * @return 文件
     */
    public static File getFileByPath(String filePath) {
        return isSpace(filePath) ? null : new File(filePath);
    }

    /**
     * 判断字符串是否为 null 或全为空白字符
     *
     * @param s
     * @return
     */
    private static boolean isSpace(final String s) {
        if (s == null)
            return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 关闭IO
     *
     * @param closeables closeable
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null)
            return;
        try {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    closeable.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
