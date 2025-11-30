package org.lsposed.hijack.ui.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.lsposed.hijack.databinding.ActivityModuleNotActivatedBinding;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import org.lsposed.hijack.R;

public class ModuleNotActivatedActivity extends AppCompatActivity {

    private ActivityModuleNotActivatedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityModuleNotActivatedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setBar();
        binding.exit.setOnClickListener(v -> exit());
    }
    
    private void setBar() {
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .keyboardEnable(true)
                .autoDarkModeEnable(true)
                .navigationBarColor(R.color.bar)
                .init();
    }

    private void exit() {
        try {
            finishAffinity();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAndRemoveTask();
            } else {
                ActivityCompat.finishAffinity(this);
            }
        } catch (Throwable e) {
        }
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                am.getAppTasks().forEach(task -> task.finishAndRemoveTask());
            }
        } catch (Throwable e) {
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}