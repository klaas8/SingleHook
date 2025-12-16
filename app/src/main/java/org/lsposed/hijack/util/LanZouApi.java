package org.lsposed.hijack.util;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;

public class LanZouApi extends HttpUtils {

    private static String UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36";

    public static interface onUpdateListener {
        @Nullable
        void onSuccess(boolean isUpdate);
    }
    
    public static void onUpdate(onUpdateListener updateListener) {
    	new Handler(Looper.getMainLooper()).post(() -> {
            new Thread(() -> {
                boolean isUpdate = isUpdate();
                new Handler(Looper.getMainLooper()).post(() -> {
                    updateListener.onSuccess(isUpdate);
                });
            }).start();
        });
    }
    
    public static boolean isUpdate() {
        try {
            String Url = AppUtil.NetworkDiskLink;
            String host = String.join("/", Arrays.copyOfRange(Url.split("/"), 0, 3));
            RequestResult RequestResult = get(Url)
                .header("User-Agent", UserAgent)
                .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                .setConnectTimeout(6000)
                .setReadTimeout(10000)
                .setCharset("UTF-8")
                .autoDisconnect(true)
                .EnableBufferedReader()
                .session()
                .endDelayed(50)
                .openConnection();
            AppUtil.w(1);
            if (RequestResult.isCanOk()) {
                String data = RequestResult.getData();
                AppUtil.w(data);
                String t = kt("t", data),
                    k = kt("k", data),
                    lx = find_int("lx", data),
                    fid = find_int("fid", data),
                    up = find_int("up", data),
                    ls = find_int("ls", data),
                    uid = find_str("uid", data),
                    rep = find_str("rep", data);
                String Url2 = host + "/filemoreajax.php?file=" + fid;
                RequestResult RequestResult2 = post(Url2)
                    .header("User-Agent", UserAgent)
                    .header("Referer", Url)
                    .header("Origin", host)
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                    .formData("lx", lx)
                    .formData("fid", fid)
                    .formData("uid", uid)
                    .formData("pg", "1")
                    .formData("rep", rep)
                    .formData("t", t)
                    .formData("k", k)
                    .formData("up", up)
                    .formData("ls", ls)
                    .formData("pwd", AppUtil.NetworkDiskPassword)
                    .setConnectTimeout(6000)
                    .setReadTimeout(10000)
                    .setCharset("UTF-8")
                    .autoDisconnect(true)
                    .EnableBufferedReader()
                    .session()
                    .endDelayed(50)
                    .openConnection();
                if (RequestResult2.isCanOk()) {
                    JSONObject Json = RequestResult2.toJsonObject();
                    if (Json.getInt("zt") == 1) {
                        String fileName = Json.getJSONArray("text").getJSONObject(0).getString("name_all").trim().replaceAll("\\s", "");
                        return !fileName.equalsIgnoreCase(AppUtil.LocalVersion.trim().replaceAll("\\s", ""));
                    }
                }
            }
        } catch (Throwable e) {}
        return false;
    }

    private static String find_str(String key, String data) {
        Matcher matcher = Pattern.compile("['\"]" + key + "['\"]\\s*[:=]\\s*['\"](.*?)['\"]", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(data);
        if (matcher.find()) {
            if (matcher.group(1).trim() != null) {
                return matcher.group(1).trim();
            }
        }
        return "";
    }

    private static String find_str_v(String key, String data) {
        Matcher matcher = Pattern.compile("var\\s*" + key + "\\s*[:=]\\s*['\"](.*?)['\"]", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(data);
        if (matcher.find()) {
            if (matcher.group(1).trim() != null) {
                return matcher.group(1).trim();
            }
        }
        return "";
    }

    private static String find_int(String key, String data) {
        Matcher matcher = Pattern.compile("['\"]" + key + "['\"]\\s*[:=]\\s*(.*?),", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(data);
        if (matcher.find()) {
            if (matcher.group(1).trim() != null) {
                return matcher.group(1).trim();
            }
        }
        return "";
    }

    private static String find_int_v(String key, String data) {
        Matcher matcher = Pattern.compile("var\\s*" + key + "\\s*[:=]\\s*(.*?),", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(data);
        if (matcher.find()) {
            if (matcher.group(1).trim() != null) {
                return matcher.group(1).trim();
            }
        }
        return "";
    }

    private static String kt(String key, String data) {
        Matcher matcher = Pattern.compile("['\"]" + key + "['\"]\\s*[:=]\\s*(.*?),", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(data);
        if (matcher.find()) {
            String v = find_str_v(matcher.group(1).trim(), data);
            if (!TextUtils.isEmpty(v)) return v;
        }
        return "";
    }
}