package org.lsposed.hijack.ui.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.app.ActivityManager;
import android.content.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.MessageDigest;
import org.apache.commons.io.FileUtils;
import org.lsposed.hijack.BuildConfig;
import org.lsposed.hijack.R;
import org.lsposed.hijack.util.ECB;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifySigning();
        startActivity(new Intent(this, isModuleActivated() ? MainActivity.class : ModuleNotActivatedActivity.class));
        finish();
    }

    private boolean isModuleActivated() {
        return System.getProperty("isModuleActivated" ,"0").equals("1") || isXPosed();
    }
    
    public void verifySigning() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : packageInfo.signatures) {
                byte[] signatureBytes = signature.toByteArray();
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signatureBytes);
                String sha1 = bytesToHex(md.digest());
                if (!sha1.equalsIgnoreCase(ECB.d(getString(R.string.sha_key), BuildConfig.SHA1))) {
                    exit();
                }
                md = MessageDigest.getInstance("SHA-256");
                md.update(signatureBytes);
                String sha256 = bytesToHex(md.digest());
                if (!sha256.equalsIgnoreCase(ECB.d(getString(R.string.sha_key), BuildConfig.SHA256))) {
                    exit();
                }
            }
        } catch (Throwable e) {}
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    private void exit() {
        try {
            finishAffinity();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                ActivityCompat.finishAffinity(this);
            }
        } catch (Throwable e) {}
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                am.getAppTasks().forEach(task -> task.finishAndRemoveTask());
            }
        } catch (Throwable e) {}
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private boolean isXPosed() {
        try {
            File file = new File("/proc/self/maps");
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            if (stringBuilder.toString().contains("/dev/zero (deleted)")
                    && (stringBuilder.toString().contains("/cache/SingleHook/HookConfig")
                            || stringBuilder.toString().contains("/cache/SingleHook/HookCache"))) {
                return true;
            }
        } catch (Throwable e) {}
        return false;
    }
}