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
            new AppInfo(isAutoAdapter, "红果免费短剧", "com.phoenix.read", "解锁会员", "https://viphook.lanzoui.com/b009hnnb7c"),
            new AppInfo(isAutoAdapter, "CAD看图王", "com.gstarmc.android", "去广告，解锁功能", "https://viphook.lanzoui.com/b009hnzdgf"),
            new AppInfo(isAutoAdapter, "小熊录屏", "com.duapps.recorder", "去广告，解锁会员", "https://viphook.lanzoui.com/b009ho0lcf"),
            new AppInfo(isAutoAdapter, "非凡PPT", "com.cqy.ppttools", "去广告，解锁会员", "https://viphook.lanzoui.com/b009ho2jzi"),
            new AppInfo(isAutoAdapter, "非凡文档", "com.cqy.wordtools", "去广告，解锁会员", "https://viphook.lanzoui.com/b009ho2xob"),
            new AppInfo(isAutoAdapter, "非凡表格", "com.cqy.exceltools", "去广告，解锁会员", "https://viphook.lanzoui.com/b009ho33eh"),
            new AppInfo(isAutoAdapter, "AiPPT制作师", "com.ysg.ai.pptmaker", "去广告，解锁会员", "https://viphook.lanzoui.com/b009ho3abg"),
            new AppInfo(isAutoAdapter, "红果免费漫剧", "com.kylin.read", "解锁会员", "https://viphook.lanzoui.com/b009hodnyh"),
            new AppInfo(isAutoAdapter, "4399在线玩", "com.h4399.gamebox", "免广告获得奖励", "https://viphook.lanzoui.com/b009hodonc"),
            new AppInfo(isAutoAdapter, "今日水印相机", "com.xhey.xcamera", "解锁会员", "https://www.123684.com/s/IfQ7Vv-pipE3"),
            new AppInfo(isAutoAdapter, "扫描全能王", "com.intsig.camscanner", "解锁至尊版", "https://www.123684.com/s/IfQ7Vv-fipE3"),
            new AppInfo(isAutoAdapter, "一叶日历", "me.mapleaf.calendar", "解锁高级版", "https://viphook.lanzoui.com/b009hokg8b"),
            new AppInfo(isAutoAdapter, "LinkUp", "com.youloft.watcher", "解锁会员", "https://viphook.lanzoui.com/b009hokjde"),
            new AppInfo(isAutoAdapter, "翻页时钟", "com.charm.clockdesktop", "解锁高级功能", "https://viphook.lanzoui.com/b009hokltc"),
            new AppInfo(isAutoAdapter, "ZArchiver解压缩工具", "com.ZArchiver.chengyuda", "解锁会员", "https://viphook.lanzoui.com/b009hoksbg"),
            new AppInfo(isAutoAdapter, "倒数日", "com.clover.daysmatter", "解锁高级版", "https://viphook.lanzoui.com/b009hol0ha"),
            new AppInfo(isAutoAdapter, "傲软抠图", "com.apowersoft.backgrounderaser", "解锁会员", "https://viphook.lanzoui.com/b009homi5c"),
            new AppInfo(isAutoAdapter, "全能翻译宝", "com.fanyiiap.wd", "解锁会员", "https://viphook.lanzoui.com/b009homl1g"),
            new AppInfo(isAutoAdapter, "元气壁纸", "com.wander.android.wallpaper", "解锁本地会员", "https://viphook.lanzoui.com/b009hoo40f"),
            new AppInfo(isAutoAdapter, "一木记物", "com.wangc.item", "解锁会员", "https://viphook.lanzoui.com/b009hoogxa"),
            new AppInfo(isAutoAdapter, "一木清单", "com.wangc.todolist", "解锁会员", "https://viphook.lanzoui.com/b009hoojje"),
            new AppInfo(isAutoAdapter, "万能小组件 Top Widgets", "com.growing.topwidgets", "解锁会员，Bug: 打开会员页面会闪退", "https://www.123684.com/s/IfQ7Vv-vLpE3"),
            new AppInfo(isAutoAdapter, "中华万年历", "cn.etouch.ecalendar", "解锁会员", "https://viphook.lanzoub.com/b009hopzxa"),
            new AppInfo(isAutoAdapter, "墨迹天气", "com.moji.mjweather", "解锁会员", "https://viphook.lanzoub.com/b009hor9yh"),
            new AppInfo(isAutoAdapter, "Mathfuns", "com.mathfuns.mathfuns", "解锁会员", "https://viphook.lanzoub.com/b009horf3c"),
            new AppInfo(isAutoAdapter, "元元记账", "com.gameley.yyjz", "解锁会员", "https://viphook.lanzoub.com/b009horgpa"),
            new AppInfo(isAutoAdapter, "logo设计大师", "com.ideack.logodesign", "解锁会员", "https://viphook.lanzoub.com/b009hori8f"),
            new AppInfo(isAutoAdapter, "照片去水印", "com.sywh.mediaeditor", "解锁会员", "https://viphook.lanzoub.com/b009hork6f"),
            new AppInfo(isAutoAdapter, "飞鸭AI记账", "com.feiya.accounting", "解锁会员", "https://viphook.lanzoub.com/b009horvoj"),
            new AppInfo(isAutoAdapter, "画世界", "net.huanci.hsj", "解锁会员", "https://viphook.lanzoub.com/b009hotg7e"),
            new AppInfo(isAutoAdapter, "可栗口语", "com.oralcraft.android", "解锁会员", "https://viphook.lanzoub.com/b009hotk9a"),
            new AppInfo(isAutoAdapter, "简讯", "com.tipsoon.android", "解锁会员", "https://viphook.lanzoub.com/b009hotmfi"),
            new AppInfo(isAutoAdapter, "喵钱记账", "com.juyingfun.mqjz", "解锁会员", "https://viphook.lanzoub.com/b009hotomh"),
            new AppInfo(isAutoAdapter, "修改水印相机", "com.hcn.mm", "解锁会员", "https://www.123684.com/s/IfQ7Vv-yLpE3"),
            new AppInfo(isAutoAdapter, "YoYo日常", "cn.nineton.dailycheck", "解锁会员", "https://www.123684.com/s/IfQ7Vv-v5pE3"),
            new AppInfo(isAutoAdapter, "造画艺术滤镜", "com.musketeer.drawart", "解锁会员", "https://viphook.lanzoub.com/b009hov2gb"),
            new AppInfo(isAutoAdapter, "PassStore", "app.jjyy.passstore", "解锁会员", "https://viphook.lanzoub.com/b009hovdyf"),
            new AppInfo(isAutoAdapter, "轻图", "com.photovision.camera", "解锁会员", "https://www.123684.com/s/IfQ7Vv-o5pE3"),
            new AppInfo(isAutoAdapter, "背书匠", "com.zzdbwku.zizbnea", "解锁会员", "https://viphook.lanzoub.com/b009hovrte"),
            new AppInfo(isAutoAdapter, "指尖时光", "com.zjzy.calendartime", "解锁会员", "https://www.123684.com/s/IfQ7Vv-yypE3"),
            new AppInfo(isAutoAdapter, "我要做计划", "com.nineton.todolist", "解锁会员", "https://www.123684.com/s/IfQ7Vv-dVpE3"),
            new AppInfo(isAutoAdapter, "Blurrr", "ai.blurrr.video", "解锁会员", "https://www.123684.com/s/IfQ7Vv-HVpE3"),
            new AppInfo(isAutoAdapter, "扫描Pro", "com.qxwl.scanimg.scanassist", "解锁会员", "https://viphook.lanzoub.com/b009hpjqjc"),
            new AppInfo(isAutoAdapter, "外语翻译官", "cn.rxxlong.translate", "解锁会员", "https://viphook.lanzoub.com/b009hpn8ed"),
            new AppInfo(isAutoAdapter, "元气打卡", "com.habits.todolist.plan.wish", "解锁会员", "https://viphook.lanzoub.com/b009hpnh5i"),
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