package org.lsposed.hijack.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.widget.Toast;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.lsposed.hijack.BuildConfig;
import java.util.concurrent.atomic.AtomicBoolean;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import org.mapdb.DB;

public class AppUtil {

    public static String qq = "1252760547";
    public static DB db;
    public static Event event;
    public static Toast toast;
    public static String domainName = "www.flexhook.eu.org";
    public static SharedPreferences prefs;
    public static Context mContext;
    public static String LocalVersion = String.format("%s_%s.apk", BuildConfig.APP_NAME, BuildConfig.VERSION_NAME), NetworkDiskLink = "https://viphook.lanzoui.com/b03p9x4ni", NetworkDiskPassword = "6666";

    public static void makeText(Object message) {
        makeText(mContext, message);
    }
    
    public static void makeText(Context mContext, final Object message) {
        if (mContext == null) return;
        new Handler(Looper.getMainLooper())
                .post(
                        () -> {
                            if (toast != null) toast.cancel();
                            toast =
                                    Toast.makeText(
                                            mContext, String.valueOf(message), Toast.LENGTH_SHORT);
                            toast.show();
                        });
    }
    
    public static void w(Object msg) {
    	try {
            FileUtils.writeStringToFile(new File(mContext.getFilesDir(), "log.txt"), String.valueOf(msg) + "\n", true);
        } catch(Throwable err) {}
    }
    
    public static boolean isAppInstalled(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) return false;
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (Throwable e) {}
        return false;
    }
    
    public static class image {
        public static String qqUrl = "https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=100&nopng=1";
        public static String appreciate = "https://cos-1309300400.cos.ap-beijing.myqcloud.com/files/img/appreciate.png";
        public static String avatarUrl = "https://avatars.githubusercontent.com/u/132535742?v=4";
    }

    // 异常处理器
    public static class CrashHandler {
        private static CrashHandler sInstance;
        private PartCrashHandler mPartCrashHandler;

        public static CrashHandler getInstance() {
            if (sInstance == null) {
                synchronized (CrashHandler.class) {
                    if (sInstance == null) {
                        sInstance = new CrashHandler();
                    }
                }
            }
            return sInstance;
        }

        public void registerPart(Context context) {
            unregisterPart();
            mPartCrashHandler = new PartCrashHandler(context);
            new Handler(Looper.getMainLooper()).post(mPartCrashHandler);
        }

        public void unregisterPart() {
            if (mPartCrashHandler != null) {
                mPartCrashHandler.stop();
                mPartCrashHandler = null;
            }
        }

        private static class PartCrashHandler implements Runnable {
            private final Context mContext;
            private AtomicBoolean isRunning = new AtomicBoolean(true);

            PartCrashHandler(Context context) {
                this.mContext = context;
            }

            void stop() {
                isRunning.set(false);
            }

            @Override
            public void run() {
                while (isRunning.get()) {
                    try {
                        Looper.loop();
                    } catch (final Throwable e) {
                        handleException(e);
                    }
                }
            }

            public void handleException(Throwable e) {
                if (isRunning.get()) {
                    Log.d("CrashHandler", Log.getStackTraceString(e));
                } else {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    } else {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
