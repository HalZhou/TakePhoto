package com.robooot.myapplication.photo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtils {

    private static final String TAG = "CommonUtils";
    private static final Pattern PATTERN = Pattern.compile("\t|\r|\n|\\s*");
    private static final String SCHEMA_HTTP = "http://";
    private static final String SCHEMA_HTTPS = "https://";
    private static final String PROTOCOL_HTTP = "http";
    private static final String PROTOCOL_HTTPS = "https";
    private static final String SLASH = "/";
    private static final String POINT = ".";

    public static void silentClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                Log.e(TAG, "" + e.getMessage());
            }
        }
    }

    public static void closeConnection(HttpURLConnection connection) {
        if (connection != null) {
            connection.disconnect();
        }
    }

    public static String formatSdkLevel() {
        return String.valueOf(Build.VERSION.SDK_INT);
    }

    /*public static String getAppName(Context context) {
        return InternalUtils.obtainClientName(context);
    }

    public static String getAppVersionName(Context context) {
        return InternalUtils.obtainClientVersion(context);
    }

    public static int getAppVersionCode(Context context) {
        return InternalUtils.obtainClientCode(context);
    }

    public static String getChannel(Context context) {
        return InternalUtils.obtainChannel(context);
    }

    public static String getAppPackageName(Context context) {
        return InternalUtils.obtainClientPackage(context);
    }

    *//**
     * TODO
     * <p>
     * 当前只考虑到了wifi和有线网络的情况
     * <p>
     * 如果需要适配手机，则需要判断是否为移动制式
     *
     * @param context
     * @return
     * @author: danbin
     * @date: 2015年3月26日 上午9:01:38
     *//*
    @SuppressLint("MissingPermission")
    public static int getDevNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return InternalConstants.NETWORK_TYPE_NONE;
        }
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return InternalConstants.NETWORK_TYPE_NONE;
        }
        int type = ni.getType();
        if (type == ConnectivityManager.TYPE_WIFI) {
            type = InternalConstants.NETWORK_TYPE_WIFI;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            int subType = ni.getSubtype();
            if (subType == TelephonyManager.NETWORK_TYPE_CDMA
                    || subType == TelephonyManager.NETWORK_TYPE_GPRS
                    || subType == TelephonyManager.NETWORK_TYPE_EDGE) {
                type = InternalConstants.NETWORK_TYPE_2G;
            } else if (subType == TelephonyManager.NETWORK_TYPE_UMTS
                    || subType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_A
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
                type = InternalConstants.NETWORK_TYPE_3G;
            } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {
                type = InternalConstants.NETWORK_TYPE_4G;
            }
        }
        return type;
    }

    public static boolean isNetworkActive(int networkType) {
        return networkType != InternalConstants.NETWORK_TYPE_NONE;
    }*/

   /* public static boolean isNetworkActive(Context context) {
        return isNetworkActive(getDevNetworkType(context));
    }*/

    public static String getDevModel() {
        return Build.MODEL.toLowerCase();
    }

    public static String fixRequestUrl(String host, String action) {
        if (isStringInvalid(action)) {
            return null;
        } else {
            action = action.trim();
        }
        if (action.contains(PROTOCOL_HTTP)) {
            return action;
        }
        if (isStringInvalid(host)) {
            return null;
        }
        if (!action.startsWith(SLASH)) {
            return new StringBuilder().append(SCHEMA_HTTP).append(host).append(SLASH).append(action).toString();
        } else {
            return new StringBuilder().append(SCHEMA_HTTP).append(host).append(action).toString();
        }
    }

    public static String fixRequestHttpsUrl(String host, String action) {
        if (host.equals("apppaycloud-test.wechatpark.com")) {
            return fixRequestUrl(host, action);
        }
        if (isStringInvalid(action)) {
            return null;
        } else {
            action = action.trim();
        }

        if (action.contains(PROTOCOL_HTTPS)) {
            return action;
        }
        if (isStringInvalid(host)) {
            return null;
        }
        if (!action.startsWith(SLASH)) {
            return new StringBuilder().append(SCHEMA_HTTPS).append(host).append(SLASH).append(action).toString();
        } else {
            return new StringBuilder().append(SCHEMA_HTTPS).append(host).append(action).toString();
        }
    }

    public static String fixImageRequestUrl(String ignoreHost, String ip, String url) {
        if (isStringInvalid(ip)) {
            return url;
        }

        if (isStringInvalid(url)) {
            return null;
        } else {
            url = url.trim();
        }

        if (url.startsWith(PROTOCOL_HTTP)) {
            int start = url.indexOf("://") + "://".length();
            int sec = url.indexOf("/", start);
            String host = url.substring(start, sec);
            if (!host.endsWith(ignoreHost)) {
                return url;
            }
            return new StringBuilder().append(url.substring(0, start)).append(ip)
                    .append(url.substring(sec, url.length())).toString();
        } else if (!url.startsWith(SLASH)) {
            int start = url.indexOf("/");
            if (url.indexOf(POINT) < start) {
                String host = url.substring(0, start);
                if (!host.endsWith(ignoreHost)) {
                    return new StringBuilder().append(SCHEMA_HTTP).append(url).toString();
                }

                return new StringBuilder().append(SCHEMA_HTTP).append(ip).append(url.substring(start, url.length()))
                        .toString();
            } else {
                return new StringBuilder().append(SCHEMA_HTTP).append(ip).append("/").append(url).toString();
            }
        } else {
            int start = url.indexOf("/", 1);
            if (url.indexOf(POINT) < start) {
                String host = url.substring(1, start);
                if (!host.endsWith(ignoreHost)) {
                    return new StringBuilder().append(SCHEMA_HTTP).append(url.substring(1, url.length())).toString();
                }

                return new StringBuilder().append(SCHEMA_HTTP).append(ip).append(url.substring(start, url.length()))
                        .toString();
            } else {
                return new StringBuilder().append(SCHEMA_HTTP).append(ip).append(url).toString();
            }
        }
    }

    public static boolean isStringInvalid(String str) {
        return str == null || str.trim().length() <= 0;
    }

    /**
     * 将流转成对应字符串
     *
     * @param is
     */
    public static String readStream(InputStream is) {
        try {
            return readStream(is, false);
        } catch (Exception e) {
            return "";
        }
    }

    public static String readStream(InputStream is, boolean closeIt) throws Exception {
        BufferedReader br = null;
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append((char) 10);
            }
        } catch (Exception e) {
            sb.setLength(0);
            if (!closeIt) {
                throw e;
            }
        } finally {
            if (closeIt) {
                silentClose(is);
            }
            silentClose(br);
        }
        return sb.toString();
    }

    /**
     * 从InputStream流中读取所有的内容保存到string中并返回
     *
     * @param is
     */
    public static String parse2string(InputStream is) throws IOException {
        if (null == is) {
            return null;
        }
        InputStreamReader reader = new InputStreamReader(is);
        StringBuilder builder = new StringBuilder();
        int len = 0;
        char[] buf = new char[4096];
        while (-1 != (len = reader.read(buf))) {
            if (len > 0) {
                builder.append(buf, 0, len);
            }
            Arrays.fill(buf, '\0');
        }

        return builder.toString();
    }

    /*public static String encodeUrlSegment(String segment) {
        try {
            return URLEncoder.encode(segment, InternalUtils.UTF_8);
        } catch (UnsupportedEncodingException e) {
            return segment;
        }
    }*/

    public static int parseInt(String value) {
        if (CommonUtils.isStringInvalid(value)) {
            return -1;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static long parseLong(String value) {
        if (CommonUtils.isStringInvalid(value)) {
            return -1;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 将文字特殊颜色处理
     *
     * @param highLightColor
     */
    public static SpannableStringBuilder genHightlightText(String originalText, int startIndex, int length,
                                                           int highLightColor) {
        SpannableStringBuilder style = new SpannableStringBuilder(originalText);
        style.setSpan(new ForegroundColorSpan(highLightColor), startIndex, startIndex + length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return style;
    }

    /**
     * 格式化需要渲染的文字
     * <p>
     * 防止出现null字符串
     * <p>
     * 去掉多余的首尾空格
     * <p>
     *
     * @param text
     * @return
     */
    public static String formatText(String text) {
        if (isStringInvalid(text)) {
            return "";
        }
        return text.trim();
    }

    public static CharSequence formatText(CharSequence charSequence) {
        if (charSequence == null) {
            return "";
        }
        return charSequence;
    }

    /**
     * boolean值和int值的映射
     * <p>
     * 约定以1表示true
     * <p>
     * 以0表示false
     *
     * @param b
     * @return
     */
    public static int convertBoolean2Int(boolean b) {
        return b ? 1 : 0;
    }

    public static boolean convertInt2Boolean(int i) {
        return i == 1 ? true : false;
    }


    /**
     * 执行java 命令
     *
     * @param args
     * @return
     */
    public static String exec(String[] args) {
        String result = "";
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        InputStream errIs = null;
        InputStream inIs = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int read;
            process = processBuilder.start();
            errIs = process.getErrorStream();
            while ((read = errIs.read()) != -1) {
                baos.write(read);
            }
            baos.write('\n');
            inIs = process.getInputStream();
            while ((read = inIs.read()) != -1) {
                baos.write(read);
            }
            byte[] data = baos.toByteArray();
            result = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CommonUtils.silentClose(errIs);
            CommonUtils.silentClose(inIs);
            if (process != null) {
                process.destroy();
            }
        }
        return result;
    }

    public static String trim(String text) {
        if (null != text) {
            return text.trim();
        }
        return null;
    }

    public static String dumpInputStream(InputStream is) throws IOException {
        StringBuilder builder = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(is);
        char[] buf = new char[4096];
        int len = 0;
        while (-1 != (len = reader.read(buf))) {
            builder.append(buf, 0, len);
            Arrays.fill(buf, '\0');
        }
        return builder.toString();
    }

    /**
     * 用于调试打印object的内 容
     */
    public static <T> String dumpObject(T target) {
        if (null == target) {
            return "null";
        } else {
            return target.toString();
        }
    }

    /**
     * 用于调试打印list的内 容
     */
    public static <T> String dumpList(List<T> list) {
        StringBuilder builder = new StringBuilder();
        if (null == list) {
            builder.append("null");
        } else {
            Object obj = null;
            builder.append("{size: " + list.size());
            for (int i = 0, count = list.size(); i < count; i++) {
                obj = list.get(i);
                builder.append(", [" + i + "]: {");
                if (null == obj) {
                    builder.append("null");
                } else {
                    builder.append(obj.toString());
                }
                builder.append("}");
            }
            builder.append("}");
        }
        return builder.toString();
    }

    /**
     * 用于调试打印数组的内 容
     */
    public static <T> String dumpArray(T[] arr) {
        StringBuilder builder = new StringBuilder();
        if (null == arr) {
            builder.append("null");
        } else {
            Object obj = null;
            builder.append("{size: " + arr.length);
            for (int i = 0, count = arr.length; i < count; i++) {
                obj = arr[i];
                builder.append(", [" + i + "]: {");
                if (null == obj) {
                    builder.append("null");
                } else {
                    builder.append(obj.toString());
                }
                builder.append("}");
            }
            builder.append("}");
        }
        return builder.toString();
    }

    /**
     * 打印MeasureSpec的值内容
     */
    public static String dumpMeasureSpec(int measureSpec) {
        final int mode = View.MeasureSpec.getMode(measureSpec);
        int size = View.MeasureSpec.getSize(measureSpec);
        String modeName = null;
        if (mode == View.MeasureSpec.AT_MOST) {
            modeName = "AT_MOST";
        } else if (mode == View.MeasureSpec.EXACTLY) {
            modeName = "EXACTLY";
        } else {
            modeName = "UNSPECIFIED";
        }
        return String.format("{hex: 0x%08x, mode: %s, size: %d }", measureSpec, modeName, size);
    }

    public static <T> String toHexString(T value) {
        if (null == value) {
            return "null";
        }

        if (value instanceof Integer) {
            return String.format("0x%08x", value);
        }

        if (value instanceof Long) {
            return String.format("0x%08x", value);
        }

        return value.toString();
    }

    /**
     * 打印控件相关信息
     *
     * @param params
     */
    public static String dumpLayoutParams(ViewGroup.LayoutParams params) {
        if (null == params) {
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("{width: ");
        if (ViewGroup.LayoutParams.MATCH_PARENT == params.width) {
            builder.append("match_parent");
        } else if (ViewGroup.LayoutParams.WRAP_CONTENT == params.width) {
            builder.append("wrap_content");
        } else {
            builder.append(params.width);
        }

        builder.append(", height: ");
        if (ViewGroup.LayoutParams.MATCH_PARENT == params.height) {
            builder.append("match_parent");
        } else if (ViewGroup.LayoutParams.WRAP_CONTENT == params.height) {
            builder.append("wrap_content");
        } else {
            builder.append(params.height);
        }
        builder.append("}");
        return builder.toString();
    }

    /**
     * OSS图片修改时需要把url转换成BACKET
     *
     * @param url
     * @return
     */
    /*public static String parseImageUrl(String url) {
        if (isStringInvalid(url)) {
            return "";
        }
        if (url.indexOf(PROTOCOL_HTTPS) >= 0) {
            return FileUploadTask.getBacketName() + CommonRegex.getImgBacket(url);
        }
        return url;
    }*/

    /**
     * 安装APK
     *
     * @param context
     * @param apkFile
     */
    public static void installApk(Context context, File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        apkFile.setReadable(true, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.parse("file://" + apkFile.getAbsolutePath()),
                    "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (context.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
            context.startActivity(intent);
        }
    }

    /**
     * 根据手机的分辨率from dp 的单位 转成为 px(像素)
     *
     * @param context
     * @param dpValue
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * dp转px
     *
     * @param dp
     */
    public static int dp2px(float dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    /**
     * sp转px
     *
     * @param sp
     * @return
     */
    public static int sp2px(float sp) {
        return (int) (sp * Resources.getSystem().getDisplayMetrics().scaledDensity + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     *
     * @param context
     * @param pxValue
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 去掉特殊字符
     *
     * @param src
     */
    public static String replaceBlank(String src) {
        String dest = "";
        if (CommonUtils.isStringInvalid(src)) {
            return dest;
        }
        Matcher matcher = PATTERN.matcher(src);
        dest = matcher.replaceAll("");
        return dest;
    }

    /**
     * 处理保留两位小数
     *
     * @param format
     * @param value
     * @return
     */
    public static String formatFloat(String format, Double value) {
        if (TextUtils.isEmpty(format)) {
            format = "0.00";
        }
        DecimalFormat df = new DecimalFormat(format);
        df.setRoundingMode(RoundingMode.HALF_EVEN);
        return df.format(value);
    }

    /**
     * 将时间转换为时间戳
     *
     * @param s
     * @return
     * @throws ParseException
     */
    public static long dateToStamp(String format, String s) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.CHINA);
        Date date = simpleDateFormat.parse(s);
        return date.getTime();
    }

    /**
     * 将时间戳转换为时间
     * yyyy/MM/dd hh:mm:ss
     *
     * @param s
     * @return
     */
    public static String stampToDate(String format, long s) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.CHINA);
        Date date = new Date(s);
        res = simpleDateFormat.format(date);
        return res;
    }


}
