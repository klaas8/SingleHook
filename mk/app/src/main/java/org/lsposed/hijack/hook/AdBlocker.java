package org.lsposed.hijack.hook;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.lsposed.hijack.BuildConfig;
import org.lsposed.hijack.util.ApksAdapter;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;

public class AdBlocker extends Hook {
    private static List<String> excludeList = new ArrayList<>();

    static {
        excludeList.add(BuildConfig.APPLICATION_ID);
        excludeList.add("com.h4399.gamebox");
        for (ApksAdapter.AppInfo appInfo : ApksAdapter.getAppInfos()) {
            excludeList.add(appInfo.packageName);
        }
        excludeList.remove("com.cqy.exceltools");
    }
    
    public static void blockAds() {
        if (excludeList.contains(packageName) || !Xprefs.getBoolean("needInterceptAds", true)) {
            return;
        }
        //穿山甲广告
        tryFindAndHookMethod("com.bytedance.sdk.openadsdk.TTAdConfig", mClassLoader, "getAppId", XC_MethodReplacement.returnConstant("没有广告"));
        tryFindAndHookMethod("com.bytedance.sdk.openadsdk.TTAdConfig", mClassLoader, "getSdkInfo", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.bytedance.sdk.openadsdk.stub.activity.Stub_Activity", mClassLoader, "getPluginPkgName", XC_MethodReplacement.returnConstant("没有广告"));
        tryFindAndHookMethod("com.bytedance.sdk.openadsdk.AdSlot", mClassLoader, "getBidAdm", XC_MethodReplacement.returnConstant("没有广告"));
        tryFindAndHookMethod("com.bytedance.sdk.openadsdk.AdSlot.Builder", mClassLoader, "build", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.bytedance.sdk.openadsdk.AdSlot$Builder", mClassLoader, "build", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.bytedance.sdk.openadsdk.TTAdSdk", mClassLoader, "isInitSuccess", XC_MethodReplacement.returnConstant(false));
        tryFindAndHookMethod("com.bytedance.sdk.openadsdk.TTAdSdk", mClassLoader, "isSdkReady", XC_MethodReplacement.returnConstant(false));
        //腾讯广告
        tryFindAndHookMethod("com.qq.e.comm.managers.status.SDKStatus", mClassLoader, "getSDKVersion", XC_MethodReplacement.returnConstant(String.valueOf(System.currentTimeMillis())));
        tryFindAndHookMethod("com.qq.e.comm.managers.GDTADManager", mClassLoader, "isInitialized", XC_MethodReplacement.returnConstant(false));
        tryFindAndHookMethod("com.qq.e.comm.managers.plugin.PM$a", mClassLoader, "call", XC_MethodReplacement.returnConstant("没有广告"));
        tryFindAndHookMethod("com.qq.e.ads.rewardvideo.RewardVideoAD", mClassLoader, "loadAD", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.qq.e.ads.rewardvideo.RewardVideoAD", mClassLoader, "showAD", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.qq.e.ads.rewardvideo.RewardVideoAD", mClassLoader, "showAD", Activity.class, XC_MethodReplacement.returnConstant(null));
        //米盟广告
        tryFindAndHookMethod("com.miui.zeus.mimo.sdk.MimoSdk", mClassLoader, "init", Context.class, "com.miui.zeus.mimo.sdk.MimoSdk$InitCallback", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.miui.zeus.mimo.sdk.MimoSdk", mClassLoader, "setDebugOn", boolean.class, XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.miui.zeus.mimo.sdk.MimoSdk", mClassLoader, "setStagingOn", boolean.class, XC_MethodReplacement.returnConstant(null));
        //快手联盟广告
        tryFindAndHookMethod("com.kwad.sdk.api.loader.v", mClassLoader, "sf", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.kwad.components.offline.api.core.network.model.BaseOfflineCompoResultData", mClassLoader, "isResultOk", XC_MethodReplacement.returnConstant(false));
        tryFindAndHookMethod("com.kwad.components.offline.api.core.network.adapter.ResultDataAdapter", mClassLoader, "isResultOk", XC_MethodReplacement.returnConstant(false));
        tryFindAndHookMethod("com.kwad.sdk.core.network.BaseResultData", mClassLoader, "isResultOk", XC_MethodReplacement.returnConstant(false));
        tryFindAndHookMethod("com.kwad.components.offline.api.core.network.model.CommonOfflineCompoResultData", mClassLoader, "isResultOk", XC_MethodReplacement.returnConstant(false));
        tryFindAndHookMethod("com.kwad.sdk.core.response.model.BaseResultData", mClassLoader, "isResultOk", XC_MethodReplacement.returnConstant(false));
        tryFindAndHookMethod("com.dydroid.ads.base.http.response.InternalResponse", mClassLoader, "isResultOk", XC_MethodReplacement.returnConstant(false));
        //Unity3D广告
        tryFindAndHookMethod("com.unity3d.services.ads.api.AdUnit.getAdUnitActivity", mClassLoader, "isInitialized", XC_MethodReplacement.returnConstant(""));
        //华为广告
        tryFindAndHookMethod("com.huawei.hms.hatool.HmsHiAnalyticsUtils", mClassLoader, "init", Context.class, boolean.class, boolean.class, boolean.class, String.class, String.class, XC_MethodReplacement.returnConstant(null));
        //mbridge广告
        tryFindAndHookMethod("com.mbridge.msdk.foundation.entity.CampaignEx", mClassLoader, "getAdSpaceT", XC_MethodReplacement.returnConstant(0));
        tryFindAndHookMethod("com.mbridge.msdk.foundation.entity.CampaignEx", mClassLoader, "getAdHtml",  XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.mbridge.msdk.foundation.entity.CampaignUnit", mClassLoader, "getAdHtml", XC_MethodReplacement.returnConstant(null));
        //sigmob广告
        tryFindAndHookMethod("com.sigmob.sdk.base.models.BaseAdUnit", mClassLoader, "getAd_source_channel", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.sigmob.sdk.base.mta.PointEntitySigmob", mClassLoader, "getAd_source_channel", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.sigmob.sdk.base.models.BaseAdUnit", mClassLoader, "getAd_type", XC_MethodReplacement.returnConstant(0));
        tryFindAndHookMethod("com.sigmob.sdk.base.models.BaseAdUnit", mClassLoader, "getAd", XC_MethodReplacement.returnConstant(null));
        //百度广告
        tryFindAndHookMethod("com.baidu.mobads.sdk.internal.ba", mClassLoader, "onSuccess", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.baidu.mobads.sdk.internal.au", mClassLoader, "onSuccess", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.baidu.mobads.sdk.internal.as", mClassLoader, "onSuccess", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.baidu.mobads.sdk.internal.bg", mClassLoader, "onSuccess", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.baidu.mobads.sdk.internal.bf", mClassLoader, "onSuccess", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.baidu.mobads.sdk.internal.g", mClassLoader, "b", String.class, XC_MethodReplacement.returnConstant(""));
        tryFindAndHookMethod("com.baidu.mobads.sdk.internal.o", mClassLoader, "a", int.class, XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.baidu.mobads.sdk.api.SplashAd", mClassLoader, "load", XC_MethodReplacement.returnConstant(null));
        tryFindAndHookMethod("com.baidu.mobads.sdk.api.SplashAd", mClassLoader, "loadAndShow", ViewGroup.class, XC_MethodReplacement.returnConstant(null));
        //谷歌广告
        tryFindAndHookMethod("com.google.android.gms.ads.AdView", mClassLoader, "loadAd", XC_MethodReplacement.returnConstant(null));
        //头条广告
        tryFindAndHookMethod("com.bytedance.sdk.openadsdk.api.plugin.d", mClassLoader, "c", List.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                param.args[0] = null;
            }
        });
        // 关闭广告页
        InjectActivity((activity, RootView, ActivityNane) -> {
            String[] AdClass = {
                "com.qq.e.ads.PortraitADActivity",// 腾讯
                "com.google.android.gms.ads.AdActivity",// 谷歌
                "com.vivask.sdk.base.common.AdActivity",//迷你世界广告
                "com.bytedance.sdk.openadsdk.stub.activity.Stub_Standard_Portrait_Activity",// 穿山甲
                "com.mbridge.msdk.reward.player.MBRewardVideoActivity", 
            };
            for (String AdPage : AdClass) {
                if (ActivityNane.equals(AdPage)) {
                    activity.finish();
                }
            }
        });
//        find("blockAds_1000",
//            FindMethod.create().searchPackages(adPackages).matcher(MethodMatcher.create().modifiers(Modifier.PUBLIC).returnType(Types.VoidType)),
//            (XC_Method) (triple) -> {
//                if (isValidAdMethod(triple)) triple.hook(null);
//            }
//        );
//        find("blockAds_2000",
//            MethodMatcher.create()
//                .returnType(Types.BooleanType)
//                .paramCount(0)
//                .usingStrings("SDK 尚未初始化，请在 Application 中调用 GDTAdSdk.initWithoutStart() 初始化"),
//            false
//        );
//        find("blockAds_3000",
//            ClassMatcher.create().className("com.qq.e.comm.managers.GDTAdSdk"),
//            MethodMatcher.create().returnType(Types.VoidType),
//            null
//        );
//        find("blockAds_4000",
//            MethodMatcher.create()
//                .returnType(Types.BooleanType)
//                .usingStrings("Flags.initialize() was not called!"),
//            true
//        );
//        find("blockAds_5000",
//            FindMethod.create()
//                .searchPackages("com.kwad")
//                .matcher(MethodMatcher.create().returnType(Types.BooleanType).name("isResultOk")),
//            false
//        );
//        find("blockAds_6000",
//            FindMethod.create()
//                .searchPackages("com.kwad")
//                .matcher(MethodMatcher.create().returnType(Types.StringClass).usingStrings("https://open.e.kuaishou.com/rest/e/v3/open/sdk2")),
//            null
//        );
//        find("blockAds_7000",
//            FindMethod.create()
//                .searchPackages("com.bytedance.sdk.openadsdk.TTAdSdk", "com.anythink.core.api.ATSDK")
//                .matcher(MethodMatcher.create().returnType(Types.VoidType).name("init")),
//            null
//        );
    }
    
    private final static Set<String> validAdMethods = new HashSet<>(Arrays.asList(
        "loadAd",
        "loadAds",
        "showAd",
        "showAds",
        "ShowAd",
        "ShowAds",
        "fetchAd",
        "onBannerAdShow"
    ));

    private static boolean isValidAdMethod(MethodTriple triple) {
        return validAdMethods.contains(triple.methodName);
    }

    private static List<String> adPackages = Arrays.asList(
        "com.applovin",
        "com.anythink",
        "com.facebook.ads",
        "com.fyber.inneractive.sdk",
        "com.google.android.gms.ads",
        "com.mbridge.msdk",
        "com.inmobi.ads",
        "com.miniclip.ads",
        "com.smaato.sdk",
        "com.tp.adx",
        "com.tradplus.ads",
        "com.unity3d.services",
        "com.unity3d.ads",
        "com.vungle.warren",
        "com.bytedance.sdk"
    );
}