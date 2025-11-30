package org.lsposed.hijack.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ThemeUtils;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.NestedScrollView;

import org.lsposed.hijack.BuildConfig;
import org.lsposed.hijack.R;
import org.lsposed.hijack.databinding.FragmentHomeBinding;
import org.lsposed.hijack.util.AppUtil;
import org.lsposed.hijack.util.LanZouApi;
import com.google.android.material.appbar.AppBarLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

public class HomeFragment extends MainBase {

    private FragmentHomeBinding binding;
    private boolean isNeedWarn, isNeedUpdate, lastNeedWarn, lastNeedUpdate, isFirstCall = true, isActivityResumed = false;
    private final AtomicReference<Runnable> pending = new AtomicReference<>(), pending2 = new AtomicReference<>();
    private Runnable warningResetRunnable;
    
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        verify();
        setUpToolbar(binding.toolbar, getString(R.string.app_name));
        initInfo();
        isNeedUpdate = getBoolean("isNeedUpdate", false) && getBoolean("detectUpdates", true);
        updateStates(isNeedWarn, isNeedUpdate);
        CheckUpdate();
        AppUtil.event.observeCallUpdate().observe(getViewLifecycleOwner(), trigger -> {
            if (isActivityResumed) {
                CheckUpdate();
            } else {
                pending.set(() -> CheckUpdate());
            }
        });
        return binding.getRoot();
    }
    
    public void Update() {
        verify();
    	if (isActivityResumed) {
            updateStates(isNeedWarn, isNeedUpdate);
        } else {
            pending2.set(() -> updateStates(isNeedWarn, isNeedUpdate));
        }
    }
    
    public void verify() {
    	long currentTime = System.currentTimeMillis();
        long DaysInMillis = 24 * 60 * 60 * 1000L;
        if (getLong("first_call_time", 0) == 0) {
            putLong("first_call_time", currentTime + DaysInMillis);
        }
        long firstCallTime = getLong("first_call_time", currentTime + DaysInMillis);
        long remainingMillis = firstCallTime - currentTime;
        if (remainingMillis > 0) {
            isNeedWarn = true;
            if (warningResetRunnable != null) main.removeCallbacks(warningResetRunnable);
            warningResetRunnable = () -> {
                isNeedWarn = false;
                main.post(() -> {
                    Update();
                });
            };
            main.postDelayed(warningResetRunnable, remainingMillis);
        }
    }
    
    public void CheckUpdate() {
        if (getBoolean("detectUpdates", true)) {
            LanZouApi.GetFiles(AppUtil.NetworkDiskLink, AppUtil.NetworkDiskPassword, (name) ->{
                if (!TextUtils.isEmpty(name) && !name.equals(AppUtil.LocalVersion)) {
                    isNeedUpdate = true;
                } else {
                    isNeedUpdate = false;
                }
                putBoolean("isNeedUpdate", isNeedUpdate);
                Update();
            });
        } else {
            isNeedUpdate = false;
            Update();
        }
    }

    public void setUpToolbar(Toolbar toolbar, String title) {
        toolbar.setNavigationIcon(R.drawable.ic_launcher_round);
        toolbar.setTitle(title);
        toolbar.setTooltipText(title);
        TooltipCompat.setTooltipText(binding.clickView, title);
    }
    
    private void updateStates(boolean needWarn, boolean needUpdate) {
        if (!isFirstCall && needWarn == lastNeedWarn && needUpdate == lastNeedUpdate) return;
        lastNeedWarn = needWarn;
        lastNeedUpdate = needUpdate;
        isFirstCall = false;
        if (binding != null) {
            initInfo();
            binding.updateTitle.setText("更新");
            binding.updateSummary.setText("请下载最新版本!");
            binding.updateBtn.setOnClickListener(v-> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + AppUtil.domainName + "/lzy/index.php?url=" + AppUtil.NetworkDiskLink + "&p=" + AppUtil.NetworkDiskPassword + "&type=down"))));
            if (needUpdate) binding.statusIcon.setImageResource(R.drawable.ic_round_update_24);
            binding.updateCard.setVisibility(needUpdate ? View.VISIBLE : View.GONE);
            binding.warningCard.setVisibility(needWarn ? View.VISIBLE : View.GONE);
            if (needUpdate) binding.warningCard.setVisibility(8);
        }
    }

    private void initInfo() {
        if (binding != null) {
            Activity myActivity = requireActivity();
            Context mContext = requireContext();
            binding.statusCardView.setCardBackgroundColor(ThemeUtils.getThemeAttrColor(mContext, com.google.android.material.R.attr.colorPrimary));
            binding.statusIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_round_check_circle_24));
            binding.statusTitle.setText("已激活");
            binding.releaseValue.setText(getReleaseTime());
            binding.statusSummary.setText(
                    String.format(
                            "版本: %s(%s)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));
            binding.androidVersionValue.setText(
                    String.valueOf(
                            Build.VERSION.PREVIEW_SDK_INT != 0
                                    ? Build.VERSION.CODENAME
                                    : Build.VERSION.RELEASE));
            binding.sdkVersionValue.setText(String.valueOf(Build.VERSION.SDK_INT));
            binding.androidIdValue.setText(
                    android.provider.Settings.Secure.getString(
                            mContext.getContentResolver(),
                            android.provider.Settings.Secure.ANDROID_ID));
            binding.brandValue.setText(Build.MANUFACTURER);
            binding.modelValue.setText(Build.MODEL);
            binding.fingerValue.setText(Build.FINGERPRINT);
            binding.appBar.setLiftable(true);
            binding.appBar.addOnOffsetChangedListener(
                    new AppBarLayout.OnOffsetChangedListener() {
                        int lastOffset = 0;

                        @Override
                        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                            if (verticalOffset < lastOffset) {
                                hideNavigation();
                            } else if (verticalOffset > lastOffset) {
                                showNavigation();
                            }
                            lastOffset = verticalOffset;
                        }
                    });
            binding.nestedScrollView.setOnScrollChangeListener(
                    (NestedScrollView.OnScrollChangeListener)
                            (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                                if (scrollY > oldScrollY) {
                                    hideNavigation();
                                } else if (scrollY < oldScrollY) {
                                    showNavigation();
                                }
                            });
            binding.viewSource.setMovementMethod(LinkMovementMethod.getInstance());
            binding.viewSource.setText(HtmlCompat.fromHtml(
                getString(R.string.about_view_source_code, "<b><a href=\"https://github.com/FlexHook/SingleHook\">GitHub</a></b>"),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            ));
            binding.feedback.setMovementMethod(LinkMovementMethod.getInstance());
            binding.feedback.setText(HtmlCompat.fromHtml(
                getString(R.string.join_telegram_channel, "<b><a href=\"https://t.me/SingleHook\">Telegram</a></b>"),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            ));
        }
    }

    private String getReleaseTime() {
        String str_time = "未知";
        try {
            long timestamp = BuildConfig.BUILD_TIME;
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            String time = sdf.format(date);
            if (!TextUtils.isEmpty(time)) {
                str_time = time;
            }
        } catch (Throwable e) {
        }
        return str_time;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        isActivityResumed = true;
        Optional.ofNullable(pending.getAndSet(null)).ifPresent(main::post);
        Optional.ofNullable(pending2.getAndSet(null)).ifPresent(main::post);
    }

    @Override
    public void onPause() {
        super.onPause();
        isActivityResumed = false;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
