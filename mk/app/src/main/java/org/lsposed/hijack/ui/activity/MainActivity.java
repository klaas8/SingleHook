package org.lsposed.hijack.ui.activity;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import java.util.Timer;
import java.util.TimerTask;
import org.lsposed.hijack.databinding.ActivityMainBinding;
import org.lsposed.hijack.ui.fragment.MainBase;
import org.lsposed.hijack.ui.fragment.ApksFragment;
import org.lsposed.hijack.ui.fragment.HomeFragment;
import org.lsposed.hijack.ui.fragment.SettingFragment;
import org.lsposed.hijack.util.ApksAdapter;
import org.lsposed.hijack.util.AppUtil;
import org.lsposed.hijack.util.Event;
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import java.util.ArrayList;
import java.util.List;
import org.lsposed.hijack.R;

public class MainActivity extends AppCompatActivity {

    private int currentProject;
    private ActivityMainBinding binding;
    private ViewPager2 viewPager2;
    private BottomNavigationView bottomNavigationView;
    private HideBottomViewOnScrollBehavior<BottomNavigationView> hideBottomViewOnScrollBehavior;
    private final List<Class<? extends MainBase>> fragmentClasses = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        // 初始化
        AppUtil.mContext = getApplicationContext();
        if (AppUtil.prefs == null) AppUtil.prefs = getSharedPreferences("HookConfig", Context.MODE_PRIVATE);
        // 设置状态栏
        setBar();
        // p2加碎片活动
        fragmentClasses.add(ApksFragment.class);
        fragmentClasses.add(HomeFragment.class);
        fragmentClasses.add(SettingFragment.class);
        // 设置碎片绑定p2
        setupViewPagerAndBottomNavigation();
        setContentView(binding.getRoot());
        // 注册通信
        AppUtil.event = new ViewModelProvider(this).get(Event.class);
        AppUtil.event
                .observeCallShowNavigation()
                .observe(
                        this,
                        trigger -> {
                            if (trigger) {
                                showNavigation();
                            } else {
                                hideNavigation();
                            }
                        });
        AppUtil.prefs.getAll().forEach((k, v) -> {
            if (k.startsWith("extra_")) {
                String p = k.replaceFirst("^extra_", "");
                boolean isOk = true;
                for(ApksAdapter.AppInfo info : ApksAdapter.getAppInfos()) {
                    if (p.equals(info.packageName)) {
                        isOk = false;
                        break;
                    }
                }
                if (isOk) AppUtil.prefs.edit().remove(k).apply();
            }
        });
    }

    public void showNavigation() {
        if (hideBottomViewOnScrollBehavior.isScrolledDown())
            hideBottomViewOnScrollBehavior.slideUp(bottomNavigationView);
    }

    public void hideNavigation() {
        if (hideBottomViewOnScrollBehavior.isScrolledUp())
            hideBottomViewOnScrollBehavior.slideDown(bottomNavigationView);
    }
    
    public int getH() {
    	return binding.navView.getHeight();
    }
    
    public void setShowIcon(boolean isShow) {
        PackageManager packageManager = getPackageManager();
        int show = isShow ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        packageManager.setComponentEnabledSetting(getAliseComponentName(), show, PackageManager.DONT_KILL_APP);
        if (!isShow) {
            new Timer().scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    if (!isShowIcon()) exit();
                }
            }, 2000, 500);
        }
    }

    public boolean isShowIcon() {
        ComponentName alias = getAliseComponentName();
        if (alias == null || getPackageManager() == null) return false;
        return !getPackageManager().queryIntentActivities(new Intent().setComponent(getAliseComponentName()), 0).isEmpty();
    }

    private ComponentName getAliseComponentName() {
        return new ComponentName(this, SplashActivity.class.getName() + "Alias");
    }

    private void setupViewPagerAndBottomNavigation() {
        viewPager2 = binding.viewPager2;
        bottomNavigationView = binding.navView;
        hideBottomViewOnScrollBehavior = new HideBottomViewOnScrollBehavior<BottomNavigationView>();
        BottomFragmentStateAdapter adapter = new BottomFragmentStateAdapter(this, fragmentClasses);
        viewPager2.setAdapter(adapter);
        viewPager2.setUserInputEnabled(false);
        bottomNavigationView.setOnItemSelectedListener(
                item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.navigation_apks) {
                        viewPager2.setCurrentItem(0, true);
                    } else if (itemId == R.id.navigation_home) {
                        viewPager2.setCurrentItem(1, true);
                    } else if (itemId == R.id.navigation_setting) {
                        viewPager2.setCurrentItem(2, true);
                    }
                    return true;
                });
        viewPager2.setOffscreenPageLimit(1);
        viewPager2.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        bottomNavigationView.getMenu().getItem(position).setChecked(true);
                        currentProject = position;
                    }
                });
        viewPager2.setCurrentItem(1, false);
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) bottomNavigationView.getLayoutParams();
        if (params != null) {
            params.setBehavior(hideBottomViewOnScrollBehavior);
            binding.navView.setLayoutParams(params);
        }
    }

    private static class BottomFragmentStateAdapter extends FragmentStateAdapter {
        private final List<Class<? extends MainBase>> fragmentClasses;

        public BottomFragmentStateAdapter(
                @NonNull FragmentActivity fragmentActivity,
                List<Class<? extends MainBase>> fragmentClasses) {
            super(fragmentActivity);
            this.fragmentClasses = fragmentClasses;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            try {
                return fragmentClasses.get(position).newInstance();
            } catch (Throwable e) {
                throw new RuntimeException("Failed to create fragment instance", e);
            }
        }

        @Override
        public int getItemCount() {
            return fragmentClasses.size();
        }

        @Override
        public long getItemId(int position) {
            return fragmentClasses.get(position).hashCode();
        }

        @Override
        public boolean containsItem(long itemId) {
            for (Class<? extends Fragment> fragmentClass : fragmentClasses) {
                if (fragmentClass.hashCode() == itemId) {
                    return true;
                }
            }
            return false;
        }
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
    
    private void setBar() {
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
                .keyboardEnable(true)
                .autoDarkModeEnable(true)
                .navigationBarColor(R.color.bar)
                .init();
    }
    
    @Override
    public void onBackPressed() {
        showNavigation();
        if (currentProject == 1) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.HOME");
            startActivity(intent);
        } else {
            viewPager2.setCurrentItem(1, true);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}