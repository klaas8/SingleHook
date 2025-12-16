package org.lsposed.hijack.hook;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.text.TextUtils;
import android.view.ViewGroup;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XCallback;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import org.lsposed.hijack.BuildConfig;
import org.lsposed.hijack.util.ApksAdapter;

public class Blocker extends Hook {
    
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
        bypass();
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
    }
    
    private static void bypass() {
    	tryFindAndHookMethod("android.os.SystemProperties", mClassLoader, "get", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (String.valueOf(param.args[0]).contains("xposed")) {  
                    param.setResult("");
                }
            }
        });
        //绕过jar Class检测
        tryFindAndHookMethod(ClassLoader.class, "loadClass", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String log = param.args[0].toString();
                if (TextUtils.isEmpty(log)) return;
                if (log.startsWith("de.robv.android.xposed")) {
                    param.setThrowable(new ClassNotFoundException("not found"));
                }
                super.beforeHookedMethod(param);
            }
        });
        //绕过堆栈检测
        tryFindAndHookMethod(StackTraceElement.class, "getClassName", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String result = (String) param.getResult();
                if (!TextUtils.isEmpty(result)) {
                    if (result.contains("de.robv.android.xposed.")) {
                        param.setResult("");
                    } else if (result.contains("com.android.internal.os.ZygoteInit")) {
                        param.setResult("");
                    }
                }
                super.afterHookedMethod(param);
            }
        });
        //绕过关闭xposed
        tryFindAndHookMethod(Class.class, "getDeclaredField", String.class, new XC_MethodHook(){
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String log = param.args[0].toString();
                if (TextUtils.isEmpty(log)) return;
                if (log.equals("disableHooks")) {
                    param.args[0] = "TMD";
                }
                super.beforeHookedMethod(param);
            }
        });
        //绕过检测Xposed相关文件
        tryFindAndHookMethod(BufferedReader.class, "readLine", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                String result = (String) param.getResult();
                if (!TextUtils.isEmpty(result)) {
                    if (result.contains("XposedBridge.jar")) {
                        param.setResult("");
                    }
                }
                super.afterHookedMethod(param);
            }
        });
        //绕过jar文件检测
        tryFindAndHookConstructor(java.io.File.class, String.class, new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String log = (String) param.args[0];
                if (TextUtils.isEmpty(log)) return;
                if (log.contains("XposedBridge")) {
                    param.args[0] = log.replace("XposedBridge", "jdjd");
                }
            }
        });
        //绕过maps检测
        tryFindAndHookConstructor("java.io.FileReader", mClassLoader, String.class , new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String arg0 = (String) param.args[0];
                if (TextUtils.isEmpty(arg0)) return;
                if (arg0.toLowerCase().contains("/proc/")) {
                    //param.setResult(null);
                }
            }
        });
        //绕过vxp检测
        tryFindAndHookMethod("java.lang.System", mClassLoader, "getProperty", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String arg0 = (String) param.args[0];
                if (TextUtils.isEmpty(arg0)) return;
                if (arg0.equals("vxp")) {
                    param.setResult(null);
                }
            }
        });
        //绕过ClassPath检测
        tryFindAndHookMethod("java.lang.System", mClassLoader, "getenv", String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                String arg = (String)param.args[0];
                if (TextUtils.isEmpty(arg)) return;
                if (arg.equals("CLASSPATH")) {
                    param.setResult("FAKE.CLASSPATH");
                }
            }
        });
        //绕过检测关键代码
        tryFindAndHookMethod("dalvik.system.DexPathList$Element", mClassLoader, "toString", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                String log = param.getResult().toString();
                if (TextUtils.isEmpty(log)) return;
                if (log.contains("XposedBridge.jar")) {
                    param.setResult(log.replace("XposedBridge.jar", ""));
                }
            }
        });
        //绕过包名检测
        tryFindAndHookMethod("android.app.ApplicationPackageManager", mClassLoader, "getInstalledApplications", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                List<ApplicationInfo> packages =  (List<ApplicationInfo>) param.getResult();
                ListIterator<ApplicationInfo> iter = packages.listIterator();
                ApplicationInfo tempAppInfo;
                String tempPackageName;
                while (iter.hasNext()) {
                    tempAppInfo = iter.next();
                    tempPackageName = tempAppInfo.packageName;
                    if (tempPackageName != null && tempPackageName.equals("de.robv.android.xposed.installer")) {
                        iter.remove();
                    }
                }
                param.setResult(packages);
            }
        });
    }
}