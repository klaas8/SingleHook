package org.lsposed.hijack.hook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.util.concurrent.atomic.AtomicBoolean;
import org.lsposed.hijack.BuildConfig;
import org.lsposed.hijack.util.ApksAdapter;

public class Init extends Hook implements IXposedHookLoadPackage , IXposedHookZygoteInit {
    
    private static AtomicBoolean InjectLock = new AtomicBoolean();
    
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam XP) throws Throwable {
        if (XP == null || !XP.isFirstApplication || InjectLock.getAndSet(true)) return;
        try {
            ApkPath = XP.appInfo.sourceDir;
            System.loadLibrary("dexkit");
            Inject(() -> {
                StartInject(packageName);
                Blocker.blockAds();
                Xprefs.getAll().forEach((k, v) -> {
                    if (k.startsWith("extra_")) {
                        String p = k.replaceFirst("^extra_", "");
                        for(ApksAdapter.AppInfo info : ApksAdapter.getAppInfos()) {
                            if (p.equals(info.packageName) && v.equals(packageName)) {
                                StartInject(info.packageName);
                                break;
                            }
                        }
                    }
                });
                if (Xprefs.getBoolean("dumpDex", false) && !packageName.equals(BuildConfig.APPLICATION_ID)) {
                    exportDex();
                }
            });
        } catch (Throwable e) {}
    }
    
    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        ModulePath = startupParam.modulePath;
        LoadXsp();
    }
}