package org.lsposed.hijack.util;

import android.text.TextUtils;
import java.util.NavigableSet;
import java.util.Random;
import org.json.JSONObject;
import org.mapdb.BTreeKeySerializer;

public class yiyan {

    private static NavigableSet<String> YiYanData = AppUtil.db.treeSet("yiyan");

    public static String getYiYan() {
        addYiYan();
        if (YiYanData == null || YiYanData.isEmpty()) {
            return "努力让自己变得更好！";
        }
        Random random = new Random();
        return YiYanData.stream().skip(random.nextInt(YiYanData.size())).findFirst().orElse("努力让自己变得更好！");
    }
    
    private static void addYiYan() {
        HttpUtils.get("https://apis.jxcxin.cn/api/yiyan?type=json")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 12; 22021211RC Build/SKQ1.211006.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/99.0.4844.88 Mobile Safari/537.36")
            .setConnectTimeout(6000)
            .setReadTimeout(10000)
            .setCharset("UTF-8")
            .EnableBufferedReader()
            .openConnection((result) -> {
                if (!result.isCanOk()) return;
                JSONObject Json = result.toJsonObject();
                try {
                    if (Json.has("code") && Json.has("msg")) {
                        if (Json.getInt("code") == 200) {
                            String msg = Json.getString("msg");
                            if (!TextUtils.isEmpty(msg)) {
                                if (YiYanData.size() >= 100) YiYanData.pollLast();
                                YiYanData.add(msg);
                                AppUtil.db.commit();
                            }
                        }
                    }
                } catch (Throwable e) {}
            });
        HttpUtils.get("https://api.52vmy.cn/api/wl/yan/yiyan")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 12; 22021211RC Build/SKQ1.211006.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/99.0.4844.88 Mobile Safari/537.36")
            .setConnectTimeout(6000)
            .setReadTimeout(10000)
            .setCharset("UTF-8")
            .EnableBufferedReader()
            .openConnection((result) -> {
                if (!result.isCanOk()) return;
                JSONObject Json = result.toJsonObject();
                try {
                    if (Json.has("code") && Json.has("data")) {
                        if (Json.getInt("code") == 200) {
                            JSONObject data = Json.getJSONObject("data");
                            if (data.has("hitokoto")) {
                                String hitokoto = data.getString("hitokoto");
                                if (!TextUtils.isEmpty(hitokoto)) {
                                    if (YiYanData.size() >= 100) YiYanData.pollLast();
                                    YiYanData.add(hitokoto);
                                    AppUtil.db.commit();
                                }
                            }
                        }
                    }
                } catch (Throwable e) {}
            });
        HttpUtils.get("https://v.api.aa1.cn/api/yiyan/index.php?type=json")
            .header("User-Agent", "Mozilla/5.0 (Linux; Android 12; 22021211RC Build/SKQ1.211006.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/99.0.4844.88 Mobile Safari/537.36")
            .setConnectTimeout(6000)
            .setReadTimeout(10000)
            .setCharset("UTF-8")
            .EnableBufferedReader()
            .openConnection((result) -> {
                if (!result.isCanOk()) return;
                JSONObject Json = result.toJsonObject();
                try {
                    if (Json.has("from") && Json.has("yiyan")) {
                        String yiyan = Json.getString("yiyan");
                        if (!TextUtils.isEmpty(yiyan)) {
                            if (YiYanData.size() >= 100) YiYanData.pollLast();
                            YiYanData.add(yiyan);
                            AppUtil.db.commit();
                        }
                    }
                } catch (Throwable e) {}
            });
    }
}