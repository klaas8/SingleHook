package org.lsposed.hijack.hook;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import dalvik.system.DexFile;
import java.util.Arrays;
import java.util.Enumeration;
import org.apache.commons.io.FileUtils;
import org.lsposed.hijack.BuildConfig;
import com.tencent.mmkv.MMKV;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IdentityHashMap;
import org.lsposed.hijack.util.AppUtil;
import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.MethodData;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class HookUtils {

    public static Xprefs Xprefs;
    public static Context mContext;
    public static ClassLoader mClassLoader;
    public static String packageName;
    public static String ApkPath;
    public static String ModulePath;
    public static MMKV kv, kvStorage;
    public static Handler main;
    public static boolean DEBUG = true;
    public static boolean can = false;
    public static Activity AdActivity;
    public static XC_LoadPackage.LoadPackageParam mLoadPackageParam;
    public static ExecutorService executor;
    public static Runnable mCloseDexKitBridgeRunnable;
    public static DexKitBridge bridge1, bridge2;
    public static List<XC_MethodHook.Unhook> Unhook = new ArrayList<>();
    public static List<String> cachesKey = new ArrayList<>();
    public static List<String> fieldList = new ArrayList<>();
    public static AtomicBoolean XspLock = new AtomicBoolean();
    
    public static interface XC_InterfaceClass {
        void run(String className);
    }
    
    public static interface XC_Method {
        void run(MethodTriple triple);
    }
    
    public static void findInterfaceClass(String className, XC_InterfaceClass r) {
        try {
            if (kv.containsKey(className)) {
                try {
                    HookCache cache = kv.decodeParcelable(className, HookCache.class);
                    if (cache != null) {
                        List<MethodTriple> methods = cache.getData();
                        boolean isOk = methods.stream().allMatch(triple -> {
                            try {
                                XposedHelpers.findClass(triple.className, mClassLoader);
                                return true;
                            } catch(Throwable e) {}
                            return false;
                        });
                        if (isOk && methods.size() > 0) {
                            methods.forEach(triple -> r.run(triple.className));
                            if (DEBUG) l("缓存>接口类:", className, "接口实现类:", cache.toClass());
                            if (!can) return;
                        }
                    }
                } catch (Throwable e) {}
            }
            List<MethodTriple> methods = new ArrayList<>();
            List<String> keys = new ArrayList<>();
            dexKit((Bridge) -> {
                Bridge.get().findClass(
                    FindClass.create().matcher(
                        ClassMatcher.create()
                            .addInterface(className))
                ).forEach(clazz ->{
                    if (clazz != null) {
                        String claz = clazz.getName();
                        MethodTriple triple = new MethodTriple(claz, "", new ArrayList<>());
                        if (!keys.contains(triple.className)) {
                            methods.add(triple);
                            keys.add(triple.className);
                            r.run(triple.className);
                        }
                    }
                });
                Bridge.close();
                if (Bridge.isOk() && methods.size() > 0 && kv != null) {
                    HookCache cache = new HookCache(methods);
                    kv.removeValueForKey(className);
                    kv.encode(className, cache);
                    if (DEBUG) l("kit>接口类:", className, "接口实现类:", cache.toClass());
                    cachesKey.add(className);
                }
            });
        } catch (Throwable e) {}
    }
    
    public static void find(String key, ClassMatcher classMatcher, MethodMatcher methodMatcher, Object mCallback) {
        find(key, FindClass.create().matcher(classMatcher), FindMethod.create().matcher(methodMatcher), mCallback);
    }
    
    public static void find(String key, ClassMatcher classMatcher, FindMethod findMethod, Object mCallback) {
        find(key, FindClass.create().matcher(classMatcher), findMethod, mCallback);
    }
    
    public static void find(String key, MethodMatcher methodMatcher, ClassMatcher classMatcher, Object mCallback) {
        find(key, FindClass.create().matcher(classMatcher), FindMethod.create().matcher(methodMatcher), mCallback);
    }
    
    public static void find(String key, MethodMatcher methodMatcher, FindClass findClass, Object mCallback) {
        find(key, findClass, FindMethod.create().matcher(methodMatcher), mCallback);
    }
    
    public static void find(String key, FindMethod findMethod, FindClass findClass, Object mCallback) {
        find(key, findClass, findMethod, mCallback);
    }
    
    public static void find(String key, FindMethod findMethod, ClassMatcher classMatcher, Object mCallback) {
        find(key, FindClass.create().matcher(classMatcher), findMethod, mCallback);
    }
    
    public static void find(String key, FindClass findMethod, MethodMatcher classMatcher, Object mCallback) {
        find(key, findMethod, FindMethod.create().matcher(classMatcher), mCallback);
    }
    
    public static void find(String key, FindClass findClass, FindMethod findMethod, Object mCallback) {
        try {
            if (mCallback == null) mCallback = XC_MethodReplacement.returnConstant(null);
            boolean isCanCallback = (mCallback instanceof XC_Method);
            XC_Method xc_method = isCanCallback ? (XC_Method) mCallback : null;
            XC_MethodHook callback = (mCallback instanceof XC_MethodHook) ? (XC_MethodHook) mCallback : (isCanCallback ? null : XC_MethodReplacement.returnConstant(mCallback));
            if (kv.containsKey(key)) {
                try {
                    HookCache cache = kv.decodeParcelable(key, HookCache.class);
                    if (cache != null) {
                        List<MethodTriple> triples = cache.getData();
                        boolean isOk = triples.stream().allMatch(triple-> triple.isOk());
                        if (isOk && triples.size() > 0) {
                            triples.forEach(triple -> {
                                if (isCanCallback) xc_method.run(triple); else triple.hook(callback);
                            });
                            if (DEBUG) l(key, "缓存>Class>Method:", cache.toString());
                            if (!can) return;
                        }
                    }
                } catch (Throwable e) {}
            }
            List<MethodTriple> methods = new ArrayList<>();
            List<String> keys = new ArrayList<>();
            dexKit((Bridge) -> {
                Bridge.get().findClass(findClass).findMethod(findMethod).forEach(methodData ->{
                    if (methodData != null && (methodData.isMethod() || methodData.isConstructor())) {
                        int modifiers = methodData.getModifiers();
                        int modifiers2 = methodData.getDeclaredClass().getModifiers();
                        if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers) && !Modifier.isNative(modifiers) && !Modifier.isAbstract(modifiers2) && !Modifier.isInterface(modifiers2) && !Modifier.isNative(modifiers2)) {
                            MethodTriple triple = new MethodTriple(methodData.getDeclaredClassName(), methodData.getMethodName(), methodData.getParamTypeNames());
                            if (!keys.contains(triple.toString())) {
                                methods.add(triple);
                                keys.add(triple.toString());
                                if (!isCanCallback) triple.hook(callback);
                            }
                        }
                    }
                });
                Bridge.close();
                if (Bridge.isOk() && methods.size() > 0 && kv != null) {
                    HookCache cache = new HookCache(methods);
                    kv.removeValueForKey(key);
                    kv.encode(key, cache);
                    if (isCanCallback) {
                        List<MethodTriple> triples = cache.getData();
                        triples.forEach(triple -> {
                            xc_method.run(triple);
                        });
                    }
                    if (DEBUG) l(key, "kit>Class>Method:", cache.toString());
                    cachesKey.add(key);
                }
            });
        } catch (Throwable e) {}
    }
    
    public static void find(String key, MethodMatcher methodMatcher, Object mCallback) {
        find(key, FindMethod.create().matcher(methodMatcher), mCallback);
    }
    
    public static void find(String key, FindMethod findMethod, Object mCallback) {
        try {
            if (mCallback == null) mCallback = XC_MethodReplacement.returnConstant(null);
            boolean isCanCallback = (mCallback instanceof XC_Method);
            XC_Method xc_method = isCanCallback ? (XC_Method) mCallback : null;
            XC_MethodHook callback = (mCallback instanceof XC_MethodHook) ? (XC_MethodHook) mCallback : (isCanCallback ? null : XC_MethodReplacement.returnConstant(mCallback));
            if (kv.containsKey(key)) {
                try {
                    HookCache cache = kv.decodeParcelable(key, HookCache.class);
                    if (cache != null) {
                        List<MethodTriple> triples = cache.getData();
                        boolean isOk = triples.stream().allMatch(triple-> triple.isOk());
                        if (isOk && triples.size() > 0) {
                            triples.forEach(triple -> {
                                if (isCanCallback) xc_method.run(triple); else triple.hook(callback);
                            });
                            if (DEBUG) l(key, "缓存>Method:", cache.toString());
                            if (!can) return;
                        }
                    }
                } catch (Throwable e) {}
            }
            List<MethodTriple> methods = new ArrayList<>();
            List<String> keys = new ArrayList<>();
            dexKit((Bridge) -> {
                Bridge.get().findMethod(findMethod).forEach(methodData ->{
                    if (methodData != null && (methodData.isMethod() || methodData.isConstructor())) {
                        int modifiers = methodData.getModifiers();
                        int modifiers2 = methodData.getDeclaredClass().getModifiers();
                        if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers) && !Modifier.isNative(modifiers) && !Modifier.isAbstract(modifiers2) && !Modifier.isInterface(modifiers2) && !Modifier.isNative(modifiers2)) {
                            MethodTriple triple = new MethodTriple(methodData.getDeclaredClassName(), methodData.getMethodName(), methodData.getParamTypeNames());
                            if (!keys.contains(triple.toString())) {
                                methods.add(triple);
                                keys.add(triple.toString());
                                if (!isCanCallback) triple.hook(callback);
                            }
                        }
                    }
                });
                Bridge.close();
                if (Bridge.isOk() && methods.size() > 0 && kv != null) {
                    HookCache cache = new HookCache(methods);
                    kv.encode(key, cache);
                    if (isCanCallback) {
                        List<MethodTriple> triples = cache.getData();
                        triples.forEach(triple -> {
                            xc_method.run(triple);
                        });
                    }
                    if (DEBUG) l(key, "kit>Method:", cache.toString());
                    cachesKey.add(key);
                }
            });
        } catch (Throwable e) {}
    }
    
    public static void findInvokes(String key, MethodMatcher methodMatcher, FindMethod findMethod, Object mCallback) {
        findInvokes(key, FindMethod.create().matcher(methodMatcher), findMethod, mCallback);
    }
    
    public static void findInvokes(String key, FindMethod findMethod, MethodMatcher methodMatcher, Object mCallback) {
        findInvokes(key, findMethod, FindMethod.create().matcher(methodMatcher), mCallback);
    }
    
    public static void findInvokes(String key, MethodMatcher methodMatcher1, MethodMatcher methodMatcher2, Object mCallback) {
        findInvokes(key, FindMethod.create().matcher(methodMatcher1), FindMethod.create().matcher(methodMatcher2), mCallback);
    }
    
    public static void findInvokes(String key, FindMethod findMethod, FindMethod findMethod2, Object mCallback) {
        try {
            if (mCallback == null) mCallback = XC_MethodReplacement.returnConstant(null);
            boolean isCanCallback = (mCallback instanceof XC_Method);
            XC_Method xc_method = isCanCallback ? (XC_Method) mCallback : null;
            XC_MethodHook callback = (mCallback instanceof XC_MethodHook) ? (XC_MethodHook) mCallback : (isCanCallback ? null : XC_MethodReplacement.returnConstant(mCallback));
            if (kv.containsKey(key)) {
                try {
                    HookCache cache = kv.decodeParcelable(key, HookCache.class);
                    if (cache != null) {
                        List<MethodTriple> triples = cache.getData();
                        boolean isOk = triples.stream().allMatch(triple-> triple.isOk());
                        if (isOk && triples.size() > 0) {
                            triples.forEach(triple -> {
                                if (isCanCallback) xc_method.run(triple); else triple.hook(callback);
                            });
                            if (DEBUG) l(key, "缓存>Invokes>Method:", cache.toString());
                            if (!can) return;
                        }
                    }
                } catch (Throwable e) {}
            }
            List<MethodTriple> methods = new ArrayList<>();
            List<String> keys = new ArrayList<>();
            dexKit((Bridge) -> {
                Bridge.get().findMethod(findMethod).forEach(methodData1 ->{
                    if (methodData1 != null && (methodData1.isMethod() || methodData1.isConstructor())) {
                        methodData1.getInvokes().findMethod(
                            findMethod2
                        ).forEach(methodData2 -> {
                            if (methodData2 != null && (methodData2.isMethod() || methodData2.isConstructor())) {
                                int modifiers = methodData2.getModifiers();
                                int modifiers2 = methodData1.getModifiers();
                                if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers) && !Modifier.isNative(modifiers) && !Modifier.isAbstract(modifiers2) && !Modifier.isInterface(modifiers2) && !Modifier.isNative(modifiers2)) {
                                    MethodTriple triple = new MethodTriple(methodData2.getDeclaredClassName(), methodData2.getMethodName(), methodData2.getParamTypeNames());
                                    triple.setParent(methodData1.getDeclaredClassName(), methodData1.getMethodName(), methodData1.getParamTypeNames());
                                    if (!keys.contains(triple.toString())) {
                                        methods.add(triple);
                                        keys.add(triple.toString());
                                        if (!isCanCallback) triple.hook(callback);
                                    }
                                }
                            }
                        });
                    }
                });
                Bridge.close();
                if (Bridge.isOk() && methods.size() > 0 && kv != null) {
                    HookCache cache = new HookCache(methods);
                    kv.removeValueForKey(key);
                    kv.encode(key, cache);
                    if (isCanCallback) {
                        List<MethodTriple> triples = cache.getData();
                        triples.forEach(triple -> {
                            xc_method.run(triple);
                        });
                    }
                    if (DEBUG) l(key, "kit>Invokes>Method:", cache.toString());
                    cachesKey.add(key);
                }
            });
        } catch (Throwable e) {}
    }
    
    public static void findInvokes(String key, FindClass findClass, FindMethod findMethod, MethodMatcher methodMatcher, Object mCallback) {
        findInvokes(key, findClass, findMethod, FindMethod.create().matcher(methodMatcher), mCallback);
    }
    
    public static void findInvokes(String key, FindClass findClass, MethodMatcher methodMatcher, FindMethod findMethod, Object mCallback) {
        findInvokes(key, findClass, FindMethod.create().matcher(methodMatcher), findMethod, mCallback);
    }
    
    public static void findInvokes(String key, FindClass findClass, MethodMatcher methodMatcher, MethodMatcher methodMatcher2, Object mCallback) {
        findInvokes(key, findClass, FindMethod.create().matcher(methodMatcher), FindMethod.create().matcher(methodMatcher2), mCallback);
    }
    
    public static void findInvokes(String key, ClassMatcher classMatcher, MethodMatcher methodMatcher, MethodMatcher methodMatcher2, Object mCallback) {
        findInvokes(key, FindClass.create().matcher(classMatcher), FindMethod.create().matcher(methodMatcher), FindMethod.create().matcher(methodMatcher2), mCallback);
    }
    
    public static void findInvokes(String key, ClassMatcher classMatcher, MethodMatcher methodMatcher, FindMethod findMethod, Object mCallback) {
        findInvokes(key, FindClass.create().matcher(classMatcher), FindMethod.create().matcher(methodMatcher), findMethod, mCallback);
    }
    
    public static void findInvokes(String key, ClassMatcher classMatcher, FindMethod findMethod, MethodMatcher methodMatcher, Object mCallback) {
        findInvokes(key, FindClass.create().matcher(classMatcher), findMethod, FindMethod.create().matcher(methodMatcher), mCallback);
    }
    
    public static void findInvokes(String key, ClassMatcher classMatcher, FindMethod findMethod, FindMethod findMethod2, Object mCallback) {
        findInvokes(key, FindClass.create().matcher(classMatcher), findMethod, findMethod2, mCallback);
    }
    
    public static void findInvokes(String key, FindClass findClass, FindMethod findMethod, FindMethod findMethod2, Object mCallback) {
        try {
            if (mCallback == null) mCallback = XC_MethodReplacement.returnConstant(null);
            boolean isCanCallback = (mCallback instanceof XC_Method);
            XC_Method xc_method = isCanCallback ? (XC_Method) mCallback : null;
            XC_MethodHook callback = (mCallback instanceof XC_MethodHook) ? (XC_MethodHook) mCallback : (isCanCallback ? null : XC_MethodReplacement.returnConstant(mCallback));
            if (kv.containsKey(key)) {
                try {
                    HookCache cache = kv.decodeParcelable(key, HookCache.class);
                    if (cache != null) {
                        List<MethodTriple> triples = cache.getData();
                        boolean isOk = triples.stream().allMatch(triple-> triple.isOk());
                        if (isOk && triples.size() > 0) {
                            triples.forEach(triple -> {
                                if (isCanCallback) xc_method.run(triple); else triple.hook(callback);
                            });
                            if (DEBUG) l(key, "缓存>Invokes>Method:", cache.toString());
                            if (!can) return;
                        }
                    }
                } catch (Throwable e) {}
            }
            List<MethodTriple> methods = new ArrayList<>();
            List<String> keys = new ArrayList<>();
            dexKit((Bridge) -> {
                Bridge.get().findClass(findClass).findMethod(findMethod).forEach(methodData1 ->{
                    if (methodData1 != null && (methodData1.isMethod() || methodData1.isConstructor())) {
                        methodData1.getInvokes().findMethod(
                            findMethod2
                        ).forEach(methodData2 -> {
                            if (methodData2 != null && (methodData2.isMethod() || methodData2.isConstructor())) {
                                int modifiers = methodData2.getModifiers();
                                int modifiers2 = methodData1.getModifiers();
                                if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers) && !Modifier.isNative(modifiers) && !Modifier.isAbstract(modifiers2) && !Modifier.isInterface(modifiers2) && !Modifier.isNative(modifiers2)) {
                                    MethodTriple triple = new MethodTriple(methodData2.getDeclaredClassName(), methodData2.getMethodName(), methodData2.getParamTypeNames());
                                    triple.setParent(methodData1.getDeclaredClassName(), methodData1.getMethodName(), methodData1.getParamTypeNames());
                                    if (!keys.contains(triple.toString())) {
                                        methods.add(triple);
                                        keys.add(triple.toString());
                                        if (!isCanCallback) triple.hook(callback);
                                    }
                                }
                            }
                        });
                    }
                });
                Bridge.close();
                if (Bridge.isOk() && methods.size() > 0 && kv != null) {
                    HookCache cache = new HookCache(methods);
                    kv.removeValueForKey(key);
                    kv.encode(key, cache);
                    if (isCanCallback) {
                        List<MethodTriple> triples = cache.getData();
                        triples.forEach(triple -> {
                            xc_method.run(triple);
                        });
                    }
                    if (DEBUG) l(key, "kit>Invokes>Method:", cache.toString());
                    cachesKey.add(key);
                }
            });
        } catch (Throwable e) {}
    }
    
    public static class MethodTriple {
        public Parent parent;
        public String className;
        public String methodName;
        public List<String> parameterTypes;

        public MethodTriple(String className, String methodName, List<String> parameterTypes) {
            this.className = className;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
        }
        
        public void setParent(String className, String methodName, List<String> parameterTypes) {
            this.parent = new Parent(className, methodName, parameterTypes);
        }
        
        public void p() {
        	l(this.toString());
        }
        
        public boolean isOk() {
            try {
            	if (this.methodName.equalsIgnoreCase("<init>")) {
                    return XposedHelpers.findConstructorExactIfExists(className, mClassLoader, parameterTypes.toArray(new Object[0])) != null;
                } else {
                    return XposedHelpers.findMethodExactIfExists(className, mClassLoader, methodName, parameterTypes.toArray(new Object[0])) != null;
                }
            } catch(Throwable err) {}
        	return false;
        }
        
        public boolean isMethod() {
            return !TextUtils.isEmpty(this.methodName) && !this.methodName.equalsIgnoreCase("<init>");
        }
        
        public boolean isConstructor() {
            return !TextUtils.isEmpty(this.methodName) && this.methodName.equalsIgnoreCase("<init>");
        }
        
        public Method getMethod() {
            try {
                return XposedHelpers.findMethodExact(className, mClassLoader, methodName, parameterTypes.toArray(new Object[0]));
            } catch(Throwable e) {
                if (DEBUG) l(toString());
            }
            return null;
        }
        
        public void hook(Object param) {
            try {
                if (param == null) param = XC_MethodReplacement.returnConstant(null);
                XC_MethodHook mCallback = (param instanceof XC_MethodHook) ? (XC_MethodHook) param : XC_MethodReplacement.returnConstant(param);
                if (this.methodName.equalsIgnoreCase("<init>")) {
                    tryFindAndHookConstructor(this.methodName, mClassLoader, parameterTypes.toArray(new Object[0]), mCallback);
                } else {
                    tryHookMethod(getMethod(), mCallback);
                }
            } catch (Throwable e){}
        }
        
        @Override
        public String toString() {
            return "{className=" + className +
            ", methodName=" + methodName +
            ", parameterTypes=[" + String.join(", ", parameterTypes) +
            "], Parent= " + (parent == null ? "null" : parent.toString()) +"}";
        }
        
        public class Parent {
            public String className;
            public String methodName;
            public List<String> parameterTypes;
    
            public Parent(String className, String methodName, List<String> parameterTypes) {
                this.className = className;
                this.methodName = methodName;
                this.parameterTypes = parameterTypes;
            }
            
            public void p() {
                l(this.toString());
            }
            
            public boolean isOk() {
                try {
                    if (this.methodName.equalsIgnoreCase("<init>")) {
                        return XposedHelpers.findConstructorExactIfExists(className, mClassLoader, parameterTypes.toArray(new Object[0])) != null;
                    } else {
                        return XposedHelpers.findMethodExactIfExists(className, mClassLoader, methodName, parameterTypes.toArray(new Object[0])) != null;
                    }
                } catch(Throwable err) {}
                return false;
            }

            public boolean isMethod() {
                return !TextUtils.isEmpty(this.methodName) && !this.methodName.equalsIgnoreCase("<init>");
            }
            
            public boolean isConstructor() {
                return !TextUtils.isEmpty(this.methodName) && this.methodName.equalsIgnoreCase("<init>");
            }
            
            public Method getMethod() {
                try {
                    return XposedHelpers.findMethodExact(className, mClassLoader, methodName, parameterTypes.toArray(new Object[0]));
                } catch(Throwable e) {
                    if (DEBUG) l(toString());
                }
                return null;
            }
            
            public void hook(Object param) {
                try {
                    if (param == null) param = XC_MethodReplacement.returnConstant(null);
                    XC_MethodHook mCallback = (param instanceof XC_MethodHook) ? (XC_MethodHook) param : XC_MethodReplacement.returnConstant(param);
                    if (this.methodName.equalsIgnoreCase("<init>")) {
                        tryFindAndHookConstructor(this.methodName, mClassLoader, parameterTypes.toArray(new Object[0]), mCallback);
                    } else {
                        tryHookMethod(getMethod(), mCallback);
                    }
                } catch (Throwable e){}
            }
            
            @Override
            public String toString() {
                return "{className=" + this.className +
                ", methodName=" + this.methodName +
                ", parameterTypes=[" + String.join(", ", this.parameterTypes) +
                "]}";
            }
        }
    }
    
    public static String getConfigKey() {
        ApkInfo apkInfo = getApkInfo(packageName);
        if (apkInfo != null) {
            return apkInfo.toString();
        }
        return "";
    }
    
    public static interface XC_Dex {
        void run(bDex bridge);
    }
    
    // 定时关闭dex
    public static void resetTimer(long time) {
        if (main == null) main = new Handler(Looper.getMainLooper());
        if (main != null) {
            if (mCloseDexKitBridgeRunnable != null) main.removeCallbacks(mCloseDexKitBridgeRunnable);
            mCloseDexKitBridgeRunnable = () -> closeDexKitBridge();
            main.postDelayed(mCloseDexKitBridgeRunnable, time);
        }
    }
    
    // 关闭dex
    private static void closeDexKitBridge() {
        if (bridge1 != null) {
            bridge1.close();
        }
        if (bridge2 != null) {
            bridge2.close();
        }
        if (can) {
            for (String key : kv.allKeys()) {
                if (!cachesKey.contains(key)) {
                    kv.removeValueForKey(key);
                }
            }
            kv.encode("_ConfigKey_", getConfigKey());
            if (DEBUG) l("成功更新配置", cachesKey.size());
        }
    }
    
    // 导出dex
    public static void exportDex() {
    	dexKit((Bridge) -> {
            File dir = mContext.getFilesDir().getAbsoluteFile();
            try {
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, "dex" + Bridge.get().getDexNum());
                if (!file.exists()) file.mkdirs();
                Bridge.get().exportDexFile(file.getAbsolutePath());
            } catch (Throwable e) {}
            Bridge.close();
            if (Bridge.isOk() && canExecuteNow("last_try_exportDex_execution_time", 1000)) {
                AppUtil.makeText(mContext, "导出dex: " + dir.getPath());
            }
        });
    }
    
    public static abstract class bDex {
        abstract DexKitBridge get();
        public void close() {
            resetTimer(2000);
        }
        public boolean isOk(){
            return false;
        }
    }
    
    // 创建dex实例
    public static void dexKit(final XC_Dex dex) {
        if (executor == null) return;
        executor.execute(() -> {
            if (bridge1 == null) bridge1 = DexKitBridge.create(mClassLoader, true);
            if (bridge2 == null) bridge2 = DexKitBridge.create(ApkPath);
            resetTimer(10000);
            class y extends bDex {
                @Override
                public DexKitBridge get() {
                    if (bridge1 == null) bridge1 = DexKitBridge.create(mClassLoader, true);
                    resetTimer(5000);
                    return bridge1;
                }
                @Override
                public boolean isOk() {
                    return bridge2 == null || !bridge2.isValid();
                }
            }
            class y2 extends bDex {
                @Override
                public DexKitBridge get() {
                    if (bridge2 == null) bridge2 = DexKitBridge.create(ApkPath);
                    resetTimer(5000);
                    return bridge2;
                }
                @Override
                public boolean isOk() {
                    return true;
                }
            }
            if (bridge1.isValid()) {
                dex.run(new y());
            } else {
                bridge1 = DexKitBridge.create(mClassLoader, true);
                if (bridge1.isValid()) dex.run(new y());
            }
            if (bridge2.isValid()) {
                dex.run(new y2());
            } else {
                bridge2 = DexKitBridge.create(ApkPath);
                if (bridge2.isValid()) dex.run(new y2());
            }
        });
    }
    
    public static interface XC_Init {
        void run();
    }
    
    // hook入口
    public static void Inject(final XC_Init Init) {
        findAndHookMethod(android.app.Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (mContext == null) {
                    mContext = (Context) param.thisObject;
                    packageName = mContext.getPackageName();
                }
                if (mClassLoader == null) mClassLoader = mContext.getClassLoader();
                if (executor == null) executor = Executors.newSingleThreadExecutor();
                if (main == null) main = new Handler(Looper.getMainLooper());
                if (Xprefs == null) {
					XspLock.set(false);
					LoadXsp();
				}
                try {
                    MMKV.initialize(mContext, mContext.getCacheDir().getAbsolutePath() + "/SingleHook");
                    kv = MMKV.mmkvWithID("HookCache", MMKV.MULTI_PROCESS_MODE);
                    kvStorage = MMKV.mmkvWithID("HookConfig", MMKV.MULTI_PROCESS_MODE);
                    String ConfigKey = getConfigKey();
                    String v = kv.decodeString("_ConfigKey_");
                    if (TextUtils.isEmpty(v) || !v.equals(ConfigKey)) {
                        can = true;
                    }
                } catch (Throwable e) {}
                if (canExecuteNow("last_try_XC_Init_Inject_execution_time", 100)) Init.run();
            }
        });
    }
    
    public static interface XC_Activity {
        void onCallback(android.app.Activity activity, android.view.View RootView, String ActivityNane);
    }
    
    public static void InjectActivity(final XC_Activity mActivity) {
        findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity activity = (Activity) param.thisObject;
                    View RootView = activity.getWindow().getDecorView();
                    String ActivityNane = activity.getClass().getName();
                    mActivity.onCallback(activity, RootView, ActivityNane);
                }
            });
    }
    
    public static interface OnGlobalLayoutListener {
        boolean onGlobalLayout(View RootView, List<View> views);
    }
    
    public static void onGlobalLayout(final View RootView, final OnGlobalLayoutListener GlobalLayout) {
        RootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (RootView != null) {
                        List<View> views = getAllView(RootView);
                        if (GlobalLayout.onGlobalLayout(RootView, views)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                RootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                RootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                        }
                    }
                }
            });
    }
    
    public static List<View> getAllView(View RootView) {
        ArrayList<View> allViews = new ArrayList<>();
        findAllView(RootView, allViews);
        return allViews;
    }

    private static void findAllView(View view, ArrayList<View> allControls) {
        if (view == null) return;
        allControls.add(view);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childView = viewGroup.getChildAt(i);
                findAllView(childView, allControls);
            }
        }
    }
    
    public static void hookMethod(MethodData methodData, XC_MethodHook mCallback) {
        try {
            if (methodData == null) return;
            int modifiers = methodData.getModifiers();
            if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers) || Modifier.isNative(modifiers)) return;
            tryHookMethod(methodData.getMethodInstance(mClassLoader), mCallback);
        } catch (Throwable e) {}
    }
    
    public static void hookMethod(Method method, XC_MethodHook mCallback) {
        if (method == null) return;
        int modifiers = method.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers) || Modifier.isNative(modifiers)) return;
        try {
            XC_MethodHook.Unhook hook = XposedBridge.hookMethod(method, mCallback);
            if (hook != null) Unhook.add(hook);
        } catch(Throwable e) {
            printMethod(method.getDeclaringClass().getName(), method.getName(), e, (Object[]) method.getParameters());
        }
    }
    
    public static boolean tryHookMethod(Method method, XC_MethodHook mCallback) {
        if (method == null) return false;
        int modifiers = method.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers) || Modifier.isNative(modifiers)) return false;
        try {
            XC_MethodHook.Unhook hook = XposedBridge.hookMethod(method, mCallback);
            if (hook != null) {
                Unhook.add(hook);
                return true;
            }
        } catch(Throwable e) {}
        return false;
    }

    public static void findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        try {
            XC_MethodHook.Unhook hook = XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
            if (hook != null) Unhook.add(hook);
        } catch (Throwable e) {
            printMethod(className, methodName, e, parameterTypesAndCallback);
        }
    }
    
    public static void findAndHookMethod(Class<?> className, String methodName, Object... parameterTypesAndCallback) {
        try {
            XC_MethodHook.Unhook hook = XposedHelpers.findAndHookMethod(className,  methodName, parameterTypesAndCallback);
            if (hook != null) Unhook.add(hook);
        } catch (Throwable e) {
            printMethod(className, methodName, e, parameterTypesAndCallback);
        }
    }
    
    public static void tryFindAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        try {
            XC_MethodHook.Unhook hook = XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
            if (hook != null) Unhook.add(hook);
        } catch (Throwable e) {}
    }
    
    public static void tryFindAndHookMethod(Class<?> className, String methodName, Object... parameterTypesAndCallback) {
        try {
            XC_MethodHook.Unhook hook = XposedHelpers.findAndHookMethod(className,  methodName, parameterTypesAndCallback);
            if (hook != null) Unhook.add(hook);
        } catch (Throwable e) {}
    }
    
    public static void findAndHookConstructor(String className, ClassLoader classLoader, Object... parameterTypesAndCallback) {
        try {
            XC_MethodHook.Unhook hook = XposedHelpers.findAndHookConstructor(className, classLoader, parameterTypesAndCallback);
            if (hook != null) Unhook.add(hook);
        } catch (Throwable e) {
            printMethod(className, "<init>", e, parameterTypesAndCallback);
        }
    }
    
    public static void findAndHookConstructor(Class<?> className, ClassLoader classLoader, Object... parameterTypesAndCallback) {
        try {
            XC_MethodHook.Unhook hook = XposedHelpers.findAndHookConstructor(className, classLoader, parameterTypesAndCallback);
            if (hook != null) Unhook.add(hook);
        } catch (Throwable e) {
            printMethod(className, "<init>", e, parameterTypesAndCallback);
        }
    }
    
    public static void tryFindAndHookConstructor(String className, ClassLoader classLoader, Object... parameterTypesAndCallback) {
        try {
            XC_MethodHook.Unhook hook = XposedHelpers.findAndHookConstructor(className, classLoader, parameterTypesAndCallback);
            if (hook != null) Unhook.add(hook);
        } catch (Throwable e) {}
    }
    
    public static void tryFindAndHookConstructor(Class<?> className, ClassLoader classLoader, Object... parameterTypesAndCallback) {
        try {
            XC_MethodHook.Unhook hook = XposedHelpers.findAndHookConstructor(className, classLoader, parameterTypesAndCallback);
            if (hook != null) Unhook.add(hook);
        } catch (Throwable e) {}
    }
    
    // 打印异常
    private static void printMethod(Object className, String methodName, Throwable e, Object... parameterTypesAndCallback) {
        StringBuilder sb = new StringBuilder(
            (mContext != null ? "方法异常: " + mContext.getPackageName() + "/" : "方法异常: ")
            + (className instanceof Class ? ((Class<?>) className).getCanonicalName() : String.valueOf(className))
            + "#"
            + methodName
            + "("
        );
        boolean first = true;
        for (int i = 0; i <= parameterTypesAndCallback.length - 1; i++) {
            Object type = parameterTypesAndCallback[i];
            if (type instanceof XC_MethodHook) {
                continue;
            }
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            if (type == null) {
                sb.append("null");
            } else if (type instanceof Class)
                sb.append(((Class<?>) type).getCanonicalName());
            else if (type instanceof String) {
                sb.append((String) type);
            } else {
                sb.append(type);
            }
        }
        sb.append(")")
            .append("\n")
            .append(e != null ? Log.getStackTraceString(e) : "")
            .append("\n");
        l(sb.toString());
    }
    
    public static void w(Object msg) {
    	try {
            FileUtils.writeStringToFile(new File(mContext.getFilesDir(), "log.txt"), String.valueOf(msg), true);
        } catch(Throwable err) {}
    }
    
    // 打印
    public static void l(Object ...logMessage) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < logMessage.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(String.valueOf(logMessage[i]));
        }
        String s = sb.toString().trim();
        XposedBridge.log(s);
        Log.d("LSPosed-Bridge", s);
    }
    
    public static String le(Throwable e) {
        return Log.getStackTraceString(e);
    }
    
    public static boolean canExecuteNow(String key, int time) {
        long lastTime = kvStorage.decodeLong(key, 0);
        long currentTime = System.currentTimeMillis();
        boolean canExecute = (currentTime - lastTime) >= time;
        if (canExecute) {
            kvStorage.encode(key, currentTime);
            return true;
        }
        return false;
    }
    
    public static String GetRandomData(int count) {
        String data = "";
        try {
            StringBuilder builder = new StringBuilder();
            SecureRandom random = new SecureRandom();
            String characters = "0123456789abcdef";
            for (int i = 0; i < count; i++) {
                int index = random.nextInt(characters.length());
                builder.append(characters.charAt(index));
            }
            data = builder.toString();
        } catch (Throwable e) {}
        return data;
    }
    
    public static void SetRandomID() {
        final String id = GetRandomData(16);
        Arrays.asList(
            "android.provider.Settings$System", 
            "android.provider.Settings$Secure").forEach(SystemClass -> {
            findAndHookMethod(SystemClass, mClassLoader, "getString", ContentResolver.class, String.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					super.afterHookedMethod(param);
					if (param.args[1] == Settings.Secure.ANDROID_ID) {
						param.setResult(id);
					}
				}
			});
        });
    }
    
    public static void SetConstantRandomID() {
        final String id = kvStorage.decodeString("settings.Constant.Random.ID", GetRandomData(16));
        kvStorage.encode("settings.Constant.Random.ID", id);
        Arrays.asList(
            "android.provider.Settings$System", 
            "android.provider.Settings$Secure").forEach(SystemClass -> {
            findAndHookMethod(SystemClass, mClassLoader, "getString", ContentResolver.class, String.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					super.afterHookedMethod(param);
					if (param.args[1] == Settings.Secure.ANDROID_ID) {
						param.setResult(id);
					}
				}
			});
        });
    }
    
    public static void clearConstantRandomID() {
        kvStorage.removeValueForKey("settings.Constant.Random.ID");
    }
    
    public static class Fields {
        private static Fields sInstance;
        private static List<FieldInfo> KeyMap = new ArrayList<>();
        
        public static Fields create() {
            if (sInstance == null) {
                synchronized (Fields.class) {
                    if (sInstance == null) {
                        sInstance = new Fields();
                    }
                }
            }
            KeyMap = new ArrayList<>();
            return sInstance;
        }
        
        public Fields add(String ClassName, String FieldName, Object Value) {
        	KeyMap.add(new FieldInfo(ClassName, FieldName, Value));
            return sInstance;
        }
        
        public Fields add(String FieldName, Object Value) {
        	KeyMap.add(new FieldInfo(null, FieldName, Value));
            return sInstance;
        }
        
        public void Build() {
        	setField(KeyMap);
        }
    }

    private static class FieldInfo {
        public final String ClassName, FieldName;
        public final Object Value;
        
        public FieldInfo(String ClassName, String FieldName, Object Value) {
            this.ClassName = ClassName;
            this.FieldName = FieldName;
            this.Value = Value;
        }
    }

    private static Class<?> getWrapperType(Class<?> primitiveType) {
        if (primitiveType == int.class) return Integer.class;
        if (primitiveType == boolean.class) return Boolean.class;
        if (primitiveType == long.class) return Long.class;
        if (primitiveType == double.class) return Double.class;
        if (primitiveType == float.class) return Float.class;
        if (primitiveType == byte.class) return Byte.class;
        if (primitiveType == short.class) return Short.class;
        if (primitiveType == char.class) return Character.class;
        return primitiveType;
    }
    
    private static boolean checkFieldTypeAndObject(Field field , Object value) {
        if (field == null) {
            return false;
        }
        if (value == null) return true;
        Class<?> fieldType = field.getType();
        Class<?> valueType = value.getClass();
        if (fieldType.isPrimitive()) {
            Class<?> wrapperType = getWrapperType(fieldType);
            return wrapperType.equals(valueType);
        } else {
            return fieldType.isAssignableFrom(valueType);
        }
    }
    
    private static void setField(final List<FieldInfo> KeyMap) {
        findAndHookMethod(Field.class, "set", Object.class, Object.class, new XC_MethodHook(){
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Field field = (Field) param.thisObject;
                    String FieldName = String.valueOf(field.getName());
                    for (FieldInfo entry : KeyMap) {
                        String ClassName = entry.ClassName;
                        String key = entry.FieldName;
                        Object value = entry.Value;
                        if (!TextUtils.isEmpty(ClassName)) {
                            if (ClassName.equalsIgnoreCase(field.getDeclaringClass().getName()) && FieldName.equalsIgnoreCase(key)) {
                                param.args[1] = value;
                                break;
                            }
                        } else if (checkFieldTypeAndObject(field, value)) {
                            if (FieldName.equalsIgnoreCase(key)) {
                                param.args[1] = value;
                                break;
                            }
                        }
                    }
                }
            });
    }
    
    public static void printField() {
        XposedHelpers.findAndHookMethod(Field.class, "set", Object.class, Object.class, new XC_MethodHook(){
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Field field = (Field) param.thisObject;
                    String FieldName = String.valueOf(field.getName());
                    String TypeName = String.valueOf(field.getType().getName());
                    String ClassName = String.valueOf(field.getDeclaringClass().getName());
                    String Value = ToString(param.args[1], new IdentityHashMap<Object,Void>());
                    String MethodName = "", FileName = "";
                    int LineNumber = 0;
                    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                    for (StackTraceElement stackTraceElement : stackTrace) {
                        MethodName = stackTraceElement.getMethodName();
                        FileName = stackTraceElement.getFileName();
                        LineNumber = stackTraceElement.getLineNumber();
                    }
                    String value = String.format("【%s】【%s】【%s】【%s】【%s】", ClassName, MethodName, FieldName, TypeName, Value);
                    if (!fieldList.contains(value)) {
                        String log = String.format("类【%s】  方法【%s】  字段【%s】  类型【%s】  值【%s】  文件【%s】  行【%s】", ClassName, MethodName, FieldName, TypeName, Value, FileName, LineNumber);
                        XposedBridge.log(log);
                        fieldList.add(value);
                    }
                }
            });
    }
    
    private static String ToString(Object obj, IdentityHashMap<Object, Void> seen) {
        if (obj == null) {
            return "null";
        }
        if (seen.containsKey(obj)) return "<Value为对象→" + obj.getClass().getSimpleName() + ">";
        seen.put(obj, null);
        if (isDirectlyPrintable(obj.getClass())) {
            return String.valueOf(obj);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(obj.getClass().getName()).append("<类>{");
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                Field field = fields[i];
                Object value = field.get(obj);
                field.setAccessible(true);
                sb.append(field.getName()).append("=").append(ToString(value, new IdentityHashMap<Object,Void>(seen)));
                if (i < fields.length - 1) {
                    sb.append(", ");
                }
            } catch (Throwable e) {}
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static boolean isDirectlyPrintable(Class<?> clazz) {
        return clazz.isPrimitive() ||
            clazz == String.class ||
            Number.class.isAssignableFrom(clazz) ||
            clazz == Boolean.class ||
            clazz == Character.class;
    }

    private static class ApkInfo {
        PackageInfo info;
        String apkName;
        String versionName;
        int versionCode;
        long lastUpdateTime;
        ApkInfo(String apkName, String versionName, int versionCode, long lastUpdateTime, PackageInfo info) {
            this.apkName=apkName;
            this.versionName=versionName;
            this.versionCode=versionCode;
            this.lastUpdateTime=lastUpdateTime;
            this.info=info;
        }
        @Override
        public String toString() {
            return "{ApkName = "
            + apkName + ", "
            + "versionName = " + versionName + ", "
            + "versionCode = " + versionCode + ", "
            + "lastUpdateTime = " + lastUpdateTime
            + "}";
        }
    }
        
    public static ApkInfo getApkInfo(String packageName) {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            String apkName = info.applicationInfo.loadLabel(pm).toString();
            String versionName = info.versionName;
            int versionCode = info.versionCode;
            long lastUpdateTime = info.lastUpdateTime;
            ApkInfo apkInfo = new ApkInfo(apkName, versionName, versionCode, lastUpdateTime, info);
            return apkInfo;
        } catch (Throwable e) {
            try {
                PackageManager pm = mContext.getPackageManager();
                PackageInfo info = pm.getPackageInfo(packageName, 0);
                String apkName = mLoadPackageParam.appInfo.loadLabel(pm).toString();
                String versionName = info.versionName;
                int versionCode = info.versionCode;
                long lastUpdateTime = info.lastUpdateTime;
                ApkInfo apkInfo = new ApkInfo(apkName, versionName, versionCode, lastUpdateTime, info);
                return apkInfo;
            } catch (Throwable err) {}
        }
        return null;
    }
    
    public static List<String> getClassNames() {
        List<String> cls = new ArrayList<>();
        try {
            DexFile df = new DexFile(mContext.getPackageCodePath());
            Enumeration<String> enumeration = df.entries();
            while (enumeration.hasMoreElements()) {
                String clzName = enumeration.nextElement();
                cls.add(clzName);
            }
        } catch (Throwable e) {}
        return cls;
    }
    
    public static void UnhookAll() {
        for (XC_MethodHook.Unhook Un : Unhook) {
            Un.unhook();
        }
    }
    
    public static void LoadXsp() {
		if (!XspLock.getAndSet(true)) {
			try {
				final XSharedPreferences SharedData = new XSharedPreferences(BuildConfig.APPLICATION_ID, "HookConfig");
				SharedData.makeWorldReadable();
				if (SharedData.hasFileChanged()) {
					SharedData.reload();
				}
				SharedData.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
						@Override
						public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
							SharedData.makeWorldReadable();
							if (SharedData.hasFileChanged()) {
								SharedData.reload();
							}
							Xprefs = new Xprefs(SharedData, SharedData.getFile());
						}
					});
				Xprefs = new Xprefs(SharedData, SharedData.getFile());
			} catch (Throwable e) {}
		}
	}
    
    public static class Xprefs {
		private XSharedPreferences SharedPreferences;
        private File myFile;
		public Xprefs(XSharedPreferences XSharedPreferences, File myFile) {
			this.SharedPreferences = XSharedPreferences;
            this.myFile = myFile;
		}
        public File getFile() {
            return this.myFile;
        }
		public Map<String, ?> getAll() {
			return this.SharedPreferences.getAll();
		}
		public String getString(String key, String defValue) {
			return this.SharedPreferences.getString(key, defValue);
		}
		public Set<String> getStringSet(String key, Set<String> defValues) {
			return this.SharedPreferences.getStringSet(key, defValues);
		}
		public int getInt(String key, int defValue) {
			return this.SharedPreferences.getInt(key, defValue);
		}
		public long getLong(String key, long defValue) {
			return this.SharedPreferences.getLong(key, defValue);
		}
		public float getFloat(String key, float defValue) {
			return this.SharedPreferences.getFloat(key, defValue);
		}
		public boolean getBoolean(String key, boolean defValue) {
			return this.SharedPreferences.getBoolean(key, defValue);
		}
		public boolean contains(String key) {
			return this.SharedPreferences.contains(key);
		}
		@Override
		public String toString() {
			return getAll().toString();
		}
	}

    public static class Types {
        public static String ContextClass = Context.class.getName();
        public static String ByteClass = java.lang.Byte.class.getName();
        public static String ShortClass = java.lang.Short.class.getName();
        public static String FloatClass = java.lang.Float.class.getName();
        public static String LongClass = java.lang.Long.class.getName();
        public static String BooleanClass = java.lang.Boolean.class.getName();
        public static String IntClass = java.lang.Integer.class.getName();
        public static String DoubleClass = java.lang.Double.class.getName();
        public static String CharacterClass = java.lang.Character.class.getName();
        public static String StringClass = java.lang.String.class.getName();
        public static String ObjectClass = java.lang.Object.class.getName();
        public static String ClassClass = java.lang.Class.class.getName();
        public static String VoidClass = java.lang.Void.class.getName();
        public static String ListClass = java.util.List.class.getName();
        public static String ArrayListClass = java.util.ArrayList.class.getName();
        public static String MapClass = java.util.Map.class.getName();
        public static String HashMapClass = java.util.HashMap.class.getName();
        public static String SetClass = java.util.Set.class.getName();
        public static String HashSetClass = java.util.HashSet.class.getName();
        public static String LinkedListClass = java.util.LinkedList.class.getName();
        public static String VectorClass = java.util.Vector.class.getName();
        public static String StackClass = java.util.Stack.class.getName();
        public static String QueueClass = java.util.Queue.class.getName();
        public static String DequeClass = java.util.Deque.class.getName();
        public static String ArrayDequeClass = java.util.ArrayDeque.class.getName();
        public static String PriorityQueueClass = java.util.PriorityQueue.class.getName();
        public static String TreeSetClass = java.util.TreeSet.class.getName();
        public static String LinkedHashSetClass = java.util.LinkedHashSet.class.getName();
        public static String TreeMapClass = java.util.TreeMap.class.getName();
        public static String LinkedHashMapClass = java.util.LinkedHashMap.class.getName();
        public static String HashtableClass = java.util.Hashtable.class.getName();
        public static String PropertiesClass = java.util.Properties.class.getName();
        public static String LocalDateClass = java.time.LocalDate.class.getName();
        public static String LocalDateTimeClass = java.time.LocalDateTime.class.getName();
        public static String InstantClass = java.time.Instant.class.getName();
        public static String FileClass = java.io.File.class.getName();
        public static String InputStreamClass = java.io.InputStream.class.getName();
        public static String OutputStreamClass = java.io.OutputStream.class.getName();
        public static String ExceptionClass = java.lang.Exception.class.getName();
        public static String RuntimeExceptionClass = java.lang.RuntimeException.class.getName();
        public static String IOExceptionClass = java.io.IOException.class.getName();
        public static String ThreadClass = java.lang.Thread.class.getName();
        public static String RunnableClass = java.lang.Runnable.class.getName();
        public static String ExecutorClass = java.util.concurrent.Executor.class.getName();
        public static String VoidType = Void.TYPE.getName();
        public static String BooleanType = Boolean.TYPE.getName();
        public static String ByteType = Byte.TYPE.getName();
        public static String ShortType = Short.TYPE.getName();
        public static String IntType = Integer.TYPE.getName();
        public static String LongType = Long.TYPE.getName();
        public static String FloatType = Float.TYPE.getName();
        public static String DoubleType = Double.TYPE.getName();
        public static String CharacterType = Character.TYPE.getName();
    }
}