package org.lsposed.hijack.util;

public class ApksAdapter {
    
    public static int isAutoAdapter = 0;

    public static AppInfo[] getAppInfos() {
        AppInfo[] appInfos = {
            new AppInfo(isAutoAdapter, "开发助手", "cn.trinea.android.developertools", "去广告，解锁专业版", "https://viphook.lanzoui.com/b009hamx7a"),
            new AppInfo(isAutoAdapter, "得间免费小说", "com.chaozh.iReader.dj", "去广告，解锁会员", "https://viphook.lanzoui.com/b009hamx5i"),
            new AppInfo(isAutoAdapter, "哔哩哔哩", "tv.danmaku.bili", "去广告，解锁画质", "market://search?q=哔哩哔哩"),
            new AppInfo(isAutoAdapter, "我的听书", "com.github.eprendre.tingshu", "去广告，优化启动", "https://viphook.lanzoui.com/b009hanlna"),
            new AppInfo(isAutoAdapter, "番茄免费小说", "com.dragon.read", "去广告，解锁会员", "https://viphook.lanzoui.com/b009haxvhe"),
            new AppInfo(isAutoAdapter, "番茄畅听", "com.xs.fm", "去广告，解锁会员", "https://viphook.lanzoui.com/b009hbqkdi"),
            new AppInfo(isAutoAdapter, "天空视频", "com.ycwlhz.tksp", "去广告，精简布局", "https://viphook.lanzoui.com/b009hbr9bg"),
            new AppInfo(isAutoAdapter, "可可影视", "com.kkdyC1V251122.T180136", "去广告，精简布局", "https://viphook.lanzoui.com/b009hbsvdg"),
            new AppInfo(isAutoAdapter, "七猫免费小说", "com.kmxs.reader", "去广告，解锁会员", "https://viphook.lanzoui.com/b009hbt3ub"),
            new AppInfo(isAutoAdapter, "红果免费短剧", "com.phoenix.read", "去广告，解锁会员", "https://viphook.lanzoui.com/b009hnnb7c"),
            new AppInfo(isAutoAdapter, "CAD看图王", "com.gstarmc.android", "去广告，解锁功能", "https://viphook.lanzoui.com/b009hnzdgf"),
            new AppInfo(isAutoAdapter, "小熊录屏", "com.duapps.recorder", "去广告，解锁会员", "https://viphook.lanzoui.com/b009ho0lcf"),
            new AppInfo(isAutoAdapter, "非凡PPT", "com.cqy.ppttools", "去广告，解锁会员", "https://viphook.lanzoui.com/b009ho2jzi"),
            new AppInfo(isAutoAdapter, "非凡文档", "com.cqy.wordtools", "去广告，解锁会员", "https://viphook.lanzoui.com/b009ho2xob"),
            new AppInfo(isAutoAdapter, "非凡表格", "com.cqy.exceltools", "去广告，解锁会员", "https://viphook.lanzoui.com/b009ho33eh"),
            new AppInfo(isAutoAdapter, "AiPPT制作师", "com.ysg.ai.pptmaker", "去广告，解锁会员", "https://viphook.lanzoui.com/b009ho3abg"),
        };
        return appInfos;
    }

    public static class AppInfo {
        public int versionCode;
        public String apkName, packageName, url, description;
        public AppInfo(int versionCode, String apkName, String packageName, String description, String url) {
            this.versionCode = versionCode;
            this.apkName = apkName;
            this.packageName = packageName;
            this.url = url;
            this.description = description;
        }
    }
}