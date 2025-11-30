package org.lsposed.hijack.util;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

public class LanZouApi {

    private static String UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36";

    public static interface FileName {
        @Nullable
        void onSuccess(String name);
    }

    public static void GetFiles(final String Url, final String Password, final FileName fileName) {
        new Handler(Looper.getMainLooper()).post(() -> {
            new Thread(() -> {
                try {
                    HttpUtils.RequestResult RequestResult = HttpUtils.get("https://" + AppUtil.domainName + "/lzy/index.php?url=" + Url + "&p=" + Password + "&type=json")
                            .header("User-Agent", UserAgent)
                            .header("Host", "flexhook.eu.org")
                            .header("Connection", "keep-alive")
                            .header("Cache-Control", "max-age=0")
                            .header("Upgrade-Insecure-Requests", "1")
                            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                            .header("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                            .setConnectTimeout(6000)
                            .setReadTimeout(10000)
                            .setCharset("UTF-8")
                            .EnableBufferedReader()
                            .openConnection();
                    if (RequestResult.isCanOk()) {
                        JSONObject json = RequestResult.toJsonObject();
                        JSONObject data = json.getJSONObject("data");
                        JSONArray list = data.getJSONArray("list");
                        if (list.length() > 0) {
                            String n = list.getJSONObject(0).getString("name");
                            new Handler(Looper.getMainLooper()).post(() -> {
                                        fileName.onSuccess(n);
                                    });
                        }
                    }
                } catch (Throwable e) {
                }
            }).start();
        });
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
        String[][] k = FindAll(" = '(.*?)'", data);
        return k.length > 1 ? k[1][0].trim() : "";
    }

    private static String[][] FindAll(String Regular, String data) {
        Regular = Regular.replaceAll("['\"]", "['\"]");
        Pattern p = Pattern.compile(Regular, Pattern.DOTALL);
        Matcher m = p.matcher(data);
        ArrayList<String[]> matches = new ArrayList<>();
        while (m.find()) {
            String[] matchArray = new String[m.groupCount()];
            for (int i = 1; i <= m.groupCount(); i++) {
                matchArray[i - 1] = m.group(i);
            }
            matches.add(matchArray);
        }
        return matches.toArray(new String[0][]);
    }
}