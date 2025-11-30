package org.lsposed.hijack.hook;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.lsposed.hijack.BuildConfig;
import com.google.gson.Gson;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.lsposed.hijack.util.ApksAdapter;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindField;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.FieldMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

public class Hook extends HookUtils {

    public static void StartInject(String packages) {
        switch (packages) {
            case BuildConfig.APPLICATION_ID:
                find("isModuleActivated",
                    MethodMatcher.create()
                        .returnType(Types.BooleanType)
                        .paramCount(0)
                        .name("isModuleActivated"),
                    true
                );
                System.setProperty("isModuleActivated", "1");
                break;
            case "cn.trinea.android.developertools":
                Hook_开发助手();
                break;
            case "tv.danmaku.bili":
                Hook_哔哩哔哩();
                break;
            case "com.ycwlhz.tksp":
            case "com.jiandan.ji":
                Hook_大海视频系列();
                break;
            case "com.chaozh.iReader.dj":
                Hook_得间免费小说();
                break;
            case "com.github.eprendre.tingshu":
                Hook_我的听书();
                break;
            case "com.dragon.read":
                Hook_番茄免费小说();
                break;
            case "com.xs.fm":
                Hook_番茄畅听();
                break;
            case "com.kkdyC1V251122.T180136":
                Hook_可可影视();
                break;
            case "com.kmxs.reader":
                Hook_七猫免费小说();
                break;
            case "com.h4399.gamebox":
                Hook_4399在线玩();
                break;
            case "com.phoenix.read":
                Hook_红果免费短剧();
                break;
            case "com.gstarmc.android":
                Hook_CAD看图王();
                break;
            case "com.duapps.recorder":
                Hook_小熊录屏();
                break;
            case "com.cqy.ppttools":
                Hook_非凡PPT();
                break;
            case "com.cqy.wordtools":
                Hook_非凡文档();
                break;
            case "com.cqy.exceltools":
                Hook_非凡表格();
                break;
            case "com.ysg.ai.pptmaker":
                Hook_AiPPT制作师();
                break;
        };
    }
    
    private static void Hook_AiPPT制作师() {
    	find("isVip",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isVip"),
            true
        );
        find("isCanUseVip",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isCanUseVip"),
            true
        );
        find("isPermanentVip",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isPermanentVip"),
            true
        );
    }
    
    private static void Hook_非凡表格() {
    	find("getVip_expire_time",
            MethodMatcher.create()
                .returnType(Types.LongType)
                .paramCount(0)
                .name("getVip_expire_time"),
            4787107805000l
        );
        InjectActivity((activity, RootView, ActivityNane) -> {
            if (ActivityNane.endsWith("VipTestActivity") || ActivityNane.endsWith("VipTestActivity2") || ActivityNane.endsWith("VipActivity") || ActivityNane.endsWith("VipActivity2")) {
                activity.finish();
            } else if (ActivityNane.endsWith("SplashActivity")) {
                findInvokes("Splash_ad",
                    ClassMatcher.create()
                        .className(ActivityNane),
                    MethodMatcher.create()
                        .returnType(Types.VoidType)
                        .paramCount(0),
                    MethodMatcher.create()
                        .name("startActivity"),
                    (XC_Method) (triple) -> {
                        try {
                            if (triple.parent != null && triple.parent.isOk() && triple.parent.isMethod()) {
                                Method[] methods = activity.getClass().getDeclaredMethods();
                                boolean isOk = Arrays.stream(methods).anyMatch(method-> method.getName().equals("initPresenter"));
                                if (isOk) XposedHelpers.callMethod(activity, "initPresenter");
                                XposedHelpers.callMethod(activity, triple.parent.methodName);
                            }
                        } catch(Throwable err) {}
                    }
                );
            }
        });
    }
    
    private static void Hook_非凡文档() {
        find("getVip_expire_time",
            MethodMatcher.create()
                .returnType(Types.LongType)
                .paramCount(0)
                .name("getVip_expire_time"),
            4787107805000l
        );
        find("getAi_category_free_use_times",
            MethodMatcher.create()
                .returnType(Types.IntType)
                .paramCount(0)
                .name("getFree_talk_free_use_times"),
            Integer.MAX_VALUE
        );
        find("getFree_talk_free_use_times",
            MethodMatcher.create()
                .returnType(Types.IntType)
                .paramCount(0)
                .name("getFree_talk_free_use_times"),
            Integer.MAX_VALUE
        );
        find("getFree_use_times",
            MethodMatcher.create()
                .returnType(Types.IntType)
                .paramCount(0)
                .name("getFree_use_times"),
            Integer.MAX_VALUE
        );
        InjectActivity((activity, RootView, ActivityNane) -> {
            if (ActivityNane.endsWith("VipTestActivity") || ActivityNane.endsWith("VipTestActivity2") || ActivityNane.endsWith("VipActivity") || ActivityNane.endsWith("VipActivity2")) {
                activity.finish();
            }
        });
    }

    private static void Hook_非凡PPT() {
        find("getVip_expire_time",
            MethodMatcher.create()
                .returnType(Types.LongType)
                .paramCount(0)
                .name("getVip_expire_time"),
            4787107805000l
        );
	    find("getVip_state",
            MethodMatcher.create()
                .returnType(Types.IntType)
                .paramCount(0)
                .name("getVip_state"),
            1
        );
        find("isLifetime_vip",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isLifetime_vip"),
            true
        );
        InjectActivity((activity, RootView, ActivityNane) -> {
            if (ActivityNane.endsWith("VipTestActivity") || ActivityNane.endsWith("VipTestActivity2") || ActivityNane.endsWith("VipActivity") || ActivityNane.endsWith("VipActivity2")) {
                activity.finish();
            } else if (ActivityNane.endsWith("SplashActivity")) {
                findInvokes("Splash_ad",
                    ClassMatcher.create()
                        .className(ActivityNane),
                    MethodMatcher.create()
                        .returnType(Types.VoidType)
                        .paramCount(0),
                    MethodMatcher.create()
                        .name("startActivity"),
                    (XC_Method) (triple) -> {
                        try {
                            if (triple.parent != null && triple.parent.isOk() && triple.parent.isMethod()) {
                                Method[] methods = activity.getClass().getDeclaredMethods();
                                boolean isOk = Arrays.stream(methods).anyMatch(method-> method.getName().equals("initPresenter"));
                                if (isOk) XposedHelpers.callMethod(activity, "initPresenter");
                                XposedHelpers.callMethod(activity, triple.parent.methodName);
                            }
                        } catch(Throwable err) {}
                    }
                );
            }
        });
    }

    private static void Hook_小熊录屏() {
        String resName = "durec_vip_level_desc";
        int resId = mContext.getResources().getIdentifier(resName, "string", packageName);
        if (resId > 0) {
            findInvokes(resName,
                MethodMatcher.create()
                    .addUsingNumber(resId),
                MethodMatcher.create()
                    .returnType(Types.BooleanType)
                    .paramTypes(Types.ContextClass),
                true
            );
        }
    }

    private static void Hook_CAD看图王() {
        findAndHookMethod("com.stone.app.sharedpreferences.AppSharedPreferences", mClassLoader, "checkFunctionPointUseable", String.class, XC_MethodReplacement.returnConstant(true));
        findAndHookMethod("com.stone.app.ui.base.BaseActivity", mClassLoader, "checkFunctionPointShowFree", String.class, XC_MethodReplacement.returnConstant(true));
        findAndHookMethod("com.stone.app.ui.base.BaseActivity", mClassLoader, "checkUserVIP_CanReceive", XC_MethodReplacement.returnConstant(true));
    }
    
    private static void Hook_红果免费短剧() {
        Fields.create()
            .add("canWorn", true)
            .add("isAdFreeVip", true)
            .add("isStoryVip", true)
            .add("isPubVip", true)
            .add("adVipAvailable", true)
            .add("autoRenew", true)
            .add("continueMonth", true)
            .add("continueMonthBuy", true)
            .add("isUnionVip", true)
            .add("isAutoCharge", true)
            .add("isAdVip", true)
            .add("isVip", true)
            .add("isVip", "1")
            .add("expireTime", "218330035200")
            .Build();
    }
    
    private static void Hook_4399在线玩() {
        InjectActivity((activity, RootView, ActivityNane) -> {
            if (ActivityNane.startsWith("com.bytedance.sdk.openadsdk.stub.activity")) {
                AdActivity = activity;
            }
        });
        findInterfaceClass("com.h4399.mads.listener.OnVideoAdListener", (className) -> {
            find("onAdShow",
                ClassMatcher.create().className(className),
                MethodMatcher.create()
                    .returnType(Types.VoidType)
                    .paramCount(0)
                    .name("onAdShow"),
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        XposedHelpers.callMethod(param.thisObject, "onVideoCompleted");
                        if (AdActivity != null) AdActivity.finish();
                        main.postDelayed(() -> {
                            if (AdActivity != null) AdActivity.finish();
                        }, 500);
                    }
                }
            );
        });
    }
    
    private static void Hook_七猫免费小说() {
        findAndHookMethod("com.qimao.qmuser.model.entity.MineHeaderEntity", mClassLoader, "getAssets", XC_MethodReplacement.returnConstant(new ArrayList<>()));
        findAndHookMethod("com.qimao.qmuser.model.entity.mine_v2.VipInfo$VipOpenInfo", mClassLoader, "getText", XC_MethodReplacement.returnConstant("续费"));
        find("getYear_vip_show",
            MethodMatcher.create()
                .returnType(Types.StringClass)
                .paramCount(0)
                .name("getYear_vip_show"),
            "1"
        );
        find("getBanners_show_type",
            MethodMatcher.create()
                .returnType(Types.StringClass)
                .paramCount(0)
                .name("getBanners_show_type"),
            "1"
        );
        find("USER_IS_VIP",
            MethodMatcher.create()
                .returnType(Types.StringClass)
                .paramTypes(Types.ContextClass)
                .paramCount(1)
                .usingStrings("USER_IS_VIP", "0"),
            "1"
        );
        find("getIs_year_vip",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("getIs_year_vip"),
            true
        );
        find("isShowYearVip",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isShowYearVip"),
            true
        );
        find("isVipState",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isVipState"),
            true
        );
        find("isBookVip",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isBookVip"),
            true
        );
        find("isVipUser",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(1)
                .name("isVipUser"),
            true
        );
        find("isNewUser",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isNewUser"),
            true
        );
        find("getIs_vip",
            MethodMatcher.create()
                .returnType(Types.StringClass)
                .paramCount(0)
                .name("getIs_vip"),
            "1"
        );
    }
    
    private static void Hook_可可影视() {
        //飞溅广告 AdPlaceInfo(id=
        findAndHookMethod("com.c.marketing.base.AdPlaceInfo", mClassLoader, "getItems", XC_MethodReplacement.returnConstant(new ArrayList<>()));
        findAndHookMethod("com.c.data.AppRecommend", mClassLoader, "getItems", XC_MethodReplacement.returnConstant(new ArrayList<>()));
        //主页推荐菜单的横幅广告 VodBannerLink(data=
        findAndHookMethod("com.salmon.film.video.data.VodBannerLink", mClassLoader, "getData", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    List list = new ArrayList<>();
                    try {
                        list = (List) param.getResult();
                        Gson gson = new Gson();
                        //逆向遍历，防止删除元素导致索引错位
                        for (int i = list.size() - 1; i >= 0; i--) {
                            try {
                                String s = gson.toJson(list.get(i));
                                JSONObject json = new JSONObject(s);
                                if (json.has("title")) {
                                    if (TextUtils.isEmpty(json.get("title").toString())) {
                                        list.remove(i);
                                    }
                                }
                            } catch (Throwable e) {}
                        }
                    } catch (Throwable e) {}
                    param.setResult(list);
                }
            });
        //主页推荐菜单的网格菜单广告 VodSection3(cols=
        findAndHookMethod("com.salmon.film.video.data.VodSection3", mClassLoader, "getData", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    List list = new ArrayList<>();
                    try {
                        list = (List) param.getResult();
                        List<String> AdList = new ArrayList<>();
                        AdList.add("爱优腾芒");
                        AdList.add("Netflix");
                        AdList.add("豆瓣250");
                        AdList.add("排行榜");
                        AdList.add("上映表");
                        Gson gson = new Gson();
                        //逆向遍历，防止删除元素导致索引错位
                        for (int i = list.size() - 1; i >= 0; i--) {
                            try {
                                String s = gson.toJson(list.get(i));
                                JSONObject json = new JSONObject(s);
                                if (json.has("text")) {
                                    if (!AdList.contains(json.get("text").toString())) {
                                        list.remove(i);
                                    }
                                }
                            } catch (Throwable e) {}
                        }
                    } catch (Throwable e) {}
                    param.setResult(list);
                }
            });
    }
    
    private static void Hook_番茄畅听() {
        find("isVip",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isVip"),
            true
        );
        findInvokes("getIsEnableSDK",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .usingStrings("SplashAdManagerImpl", "开屏 SDK 未启用", "开屏数据未加载好，无法展示广告"),
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0),
            false
        );
    }
    
    private static void Hook_番茄免费小说() {
        Fields.create()
            .add("canWorn", true)
            .add("isAdFreeVip", true)
            .add("isStoryVip", true)
            .add("isPubVip", true)
            .add("adVipAvailable", true)
            .add("autoRenew", true)
            .add("continueMonth", true)
            .add("continueMonthBuy", true)
            .add("isUnionVip", true)
            .add("isAutoCharge", true)
            .add("isAdVip", true)
            .add("isVip", true)
            .add("isVip", "1")
            .add("expireTime", "218330035200")
            .Build();
        findAndHookMethod("com.dragon.read.component.NsAdDependImpl", mClassLoader, "readerIsAdFree", XC_MethodReplacement.returnConstant(true)); //[激励视频广告-反转] 命中实验，260484自动阅读不出激励广告入口
        findAndHookMethod("com.dragon.read.component.NsCommunityDependImpl", mClassLoader, "isHideFunctionInspireAd", XC_MethodReplacement.returnConstant(true)); //[激励视频广告-反转] 命中实验，260485不展示催更激励视频广告入口
        findAndHookMethod("com.dragon.read.component.biz.impl.NsVipImpl", mClassLoader, "isAnyVip", XC_MethodReplacement.returnConstant(true)); //com.dragon.read.component.biz.impl.mine.FanqieMineFragment
        findAndHookMethod("com.dragon.read.component.biz.impl.NsVipImpl", mClassLoader, "willShowLynxBanner", XC_MethodReplacement.returnConstant(false)); //注释：true显示网络加载会员横幅
        findAndHookMethod("com.dragon.read.component.biz.impl.NsVipImpl", mClassLoader, "canShowMulVip", XC_MethodReplacement.returnConstant(true)); //注释：true显示新会员横幅
        findAndHookMethod("com.dragon.read.component.biz.impl.NsVipImpl", mClassLoader, "isBuyPaidBook", String.class, XC_MethodReplacement.returnConstant(true)); //书籍已购买，可以直接下载
        findAndHookMethod("com.dragon.read.component.audio.biz.protocol.core.data.AudioPageBookInfo", mClassLoader, "isBookUnsignedAdFree", XC_MethodReplacement.returnConstant(true)); //unauthorized book
        //checkTtsPrivilege hasNewUserProtectPrivilege  或者 [激励视频广告-反转] checkTtsPrivilege命中实验，隐藏TTS听书激励入口
        find("hasTtsNewUserPrivilege",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("hasTtsNewUserPrivilege"),
            true
        );
        // 隐藏金币功能
        find("isPolarisEnable",
            FindMethod.create().searchPackages("com.dragon.read").matcher(MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isPolarisEnable")
            ),
            false
        );
        //[激励视频广告-反转] 命中实验，屏蔽书架/收藏入口赚金币弹窗上的激励入口
        find("isHideInspireAd",
            FindMethod.create().searchPackages("com.dragon.read").matcher(MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(1)
                .name("isHideInspireAd")
            ),
            true
        );
        find("ILiveFeedCard",
            MethodMatcher.create()
                .returnType("com.dragon.read.plugin.common.api.live.feed.ILiveFeedCard")
                .usingStrings("热门主播", "热门直播", "热门直播间"),
            null
        );
        findInvokes("getIsEnableSDK",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .usingStrings("SplashAdManagerImpl", "开屏 SDK 未启用", "开屏数据未加载好，无法展示广告"),
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0),
            false
        );
    }
    
    private static void Hook_我的听书() {
        find("isShowAds",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isShowAds")
                .usingStrings("is_show_ads"),
            false
        );
        find("is_skip_splash",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .usingStrings("is_skip_splash"),
            true
        );
        find("getOpenAdTime",
            MethodMatcher.create()
                .returnType(Types.LongType)
                .paramCount(0)
                .name("getOpenAdTime")
                .usingStrings("open_ad_time"),
            System.currentTimeMillis() + 7 * 60000 * 60 * 24
        );
        find("init",
            MethodMatcher.create()
                .returnType(Types.VoidType)
                .paramCount(3)
                .name("init")
                .usingStrings("请注意正在使用测试的id进行广告调用，请在发版时换成正式id"),
            null
        );
    }
    
    private static void Hook_得间免费小说() {
        //去除广告
        findAndHookMethod("com.zhangyue.iReader.module.proxy.AdProxy", mClassLoader, "getModuleId", XC_MethodReplacement.returnConstant(null));
        findAndHookMethod("com.zhangyue.iReader.module.proxy.AdProxy", mClassLoader, "isShowAd", Bundle.class, XC_MethodReplacement.returnConstant(false));
        findAndHookMethod("com.zhangyue.iReader.module.idriver.ad.AdUtil", mClassLoader, "isPreventAd", XC_MethodReplacement.returnConstant(true));
        findAndHookMethod("com.zhangyue.iReader.module.idriver.ad.AdUtil", mClassLoader, "needForbiddenAdInSevenDays", XC_MethodReplacement.returnConstant(true));
        findAndHookMethod("com.zhangyue.iReader.app.APP", mClassLoader, "loadAdStrategy", XC_MethodReplacement.returnConstant(null));
        findAndHookMethod("com.zhangyue.iReader.module.proxy.AdProxy", mClassLoader, "loadAdStrategy", String.class, String.class, XC_MethodReplacement.returnConstant(null));
        //隐藏金币入口
        findAndHookMethod("com.zhangyue.iReader.task.read.ReadTaskProgressManager", mClassLoader, "startReadTask", XC_MethodReplacement.returnConstant(null));
        long day31 = 2678580000l;
        long vip_time = System.currentTimeMillis() + day31;
        findAndHookMethod("com.zhangyue.iReader.DB.SPHelperTemp", mClassLoader, "getLong", String.class, long.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String key = (String) param.args[0];
                if (!TextUtils.isEmpty(key)) {
                    if (key.equals("video_vip_time")) {
                        param.setResult(vip_time);
                    }
                }
            }
        });
        find("vip_time",
            MethodMatcher.create()
                .returnType(Types.LongType)
                .paramCount(0)
                .usingEqStrings("video_vip_time"),
            vip_time
        );
        find("getIs_vip",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramTypes(Types.BooleanType, Types.IntType)
                .usingStrings("request_reyun_inlaybooks_scene"),
            false
        );
    }
    
    private static void Hook_大海视频系列() {
        findAndHookMethod("android.content.ClipboardManager", mClassLoader, "setPrimaryClip", ClipData.class, XC_MethodReplacement.returnConstant(null));//剪切板
        find("getIs_vip",
            MethodMatcher.create()
                .returnType(Types.IntType)
                .paramCount(0)
                .name("getIs_vip"),
            1
        );
        find("getLogin_type",
            MethodMatcher.create()
                .returnType(Types.IntType)
                .paramCount(0)
                .name("getLogin_type"),
            1
        );
        find("getAd_source_id",
            MethodMatcher.create()
                .returnType(Types.IntType)
                .paramCount(0)
                .name("getAd_source_id"),
            0
        );
        find("barrage",
            MethodMatcher.create()
                .returnType(Types.VoidType)
                .paramTypes(Types.IntType, Types.IntType)
                .usingStrings("collection", "vod_id", "start_time", "end_time"),
            null
        );
        InjectActivity((activity, RootView, ActivityNane) -> {
            onGlobalLayout(RootView, (mView, views) -> {
                views.forEach(v -> {
                    if (v instanceof TextView) {
                        TextView textView = (TextView) v;
                        String text = textView.getText().toString();
                        switch(text) {
                            case "官方客服":
                            case "分享可得终身免广告特权>":
                                ((View) textView.getParent().getParent()).setVisibility(8);
                                break;
                            case "催更新":
                            case "推广免广告":
                            case "我来说几句":
                            case "全部影评":
                                ((View) textView.getParent()).setVisibility(8);
                                break;
                            case "弹幕走一波...":
                            case "评论":
                                textView.setVisibility(8);
                                break;
                        }
                        String[] ids = {"iv_barrage_write_horizontal", "iv_barrage_horizontal", "iv_barrage"};
                        for (String s : ids) {
                            View root = mView.findViewById(mView.getResources().getIdentifier(s, "id", mContext.getPackageName()));
                            if (root != null) root.setVisibility(8);
                        }
                    }
                });
                return false;
            });
        });
    }
    
    private static void Hook_哔哩哔哩() {
        findAndHookMethod("tv.danmaku.bili.ui.splash.ad.model.Splash", mClassLoader, "isValid", XC_MethodReplacement.returnConstant(false));
        find("isEffectiveVip",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isEffectiveVip"),
            true
        );
        find("isSeniorUser",
            MethodMatcher.create()
                .returnType(Types.BooleanType)
                .paramCount(0)
                .name("isSeniorUser"),
            true
        );
        find("getVipType",
            MethodMatcher.create()
                .returnType(Types.IntType)
                .paramCount(0)
                .name("getVipType"),
            1
        );
    }

    private static void Hook_开发助手() {
        find("vip",
            MethodMatcher.create()
                .returnType(Types.IntType)
                .paramCount(0)
                .usingNumbers(1000, 1991, 0, 1101)
                .usingEqStrings("a", "a"),
            0
        );
    }
}