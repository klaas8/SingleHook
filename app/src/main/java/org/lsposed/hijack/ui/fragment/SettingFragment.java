package org.lsposed.hijack.ui.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.TooltipCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.tabs.TabLayout;

import org.lsposed.hijack.BuildConfig;
import org.lsposed.hijack.R;
import org.lsposed.hijack.databinding.DialogDonateBinding;
import org.lsposed.hijack.databinding.FragmentSettingBinding;
import org.lsposed.hijack.databinding.ItemSwitchBinding;
import org.lsposed.hijack.ui.activity.AboutActivity;
import org.lsposed.hijack.util.AppUtil;
import org.lsposed.hijack.util.ImageSaveHelper;
import org.lsposed.hijack.util.yiyan;

public class SettingFragment extends MainBase {

    private Bitmap wx, _wx, zfb, _zfb;
    private AlertDialog alertDialog;
    private FragmentSettingBinding binding;
    
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        
        TooltipCompat.setTooltipText(binding.name, getString(R.string.title_setting));
        
        binding.describe.setText(yiyan.getYiYan());
        
        TooltipCompat.setTooltipText(binding.icon, AppUtil.qq);
        Glide.with(requireActivity())
                .load(AppUtil.image.qqUrl)
                .transform(new RoundedCorners(100))
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.no_apk)
                .error(R.drawable.no_apk)
                .fallback(R.drawable.no_apk)
                .into(binding.icon);
        
        binding.category1.title.setText("设置");
        
        binding.hideIcon.text.setText("隐藏图标");
        setSwitch(binding.hideIcon, "hideIcon", isHideIcon()).setOnCheckedChangeListener((buttonView, isChecked) -> setShowIcon(!isChecked));

        binding.dumpDex.text.setText("Dump Dex");
        binding.dumpDex.label.setText("运行时脱壳");
        binding.dumpDex.label.setVisibility(0);
        setSwitch(binding.dumpDex, "dumpDex", false);
        
        binding.detectUpdates.text.setText("检测更新");
        binding.detectUpdates.label.setText("在Home检测更新");
        binding.detectUpdates.label.setVisibility(0);
        setSwitch(binding.detectUpdates, "detectUpdates", true).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    AppUtil.event.callUpdate();
                    putBoolean("detectUpdates", isChecked);
                }
            });
        
        binding.needInterceptAds.text.setText("拦截广告");
        binding.needInterceptAds.label.setText("Xposed拦截广告总开关");
        binding.needInterceptAds.label.setVisibility(0);
        setSwitch(binding.needInterceptAds, "needInterceptAds", true);
        
        binding.category2.title.setText("其他");
        
        binding.about.text.setText("关于");
        binding.about.label.setText(BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")");
        binding.about.getRoot().setOnClickListener(v -> startActivity(new Intent(requireContext(), AboutActivity.class)));
        
        binding.officialWebsite.text.setText("更新地址");
        binding.officialWebsite.label.setText("无法检测更新，可以在这里下载");
        binding.officialWebsite.getRoot().setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppUtil.NetworkDiskLink)));
            show("密码: "+ AppUtil.NetworkDiskPassword);
        });
        
        binding.mtAutoCheckin.text.setText("MT论坛");
        binding.mtAutoCheckin.label.setText("已将自动签到移植到GitHub部署，完全免费");
        binding.mtAutoCheckin.getRoot().setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/klaas8/MT"))));
        
        binding.donate.text.setText("请我吃辣条");
        binding.donate.label.setText("模块免费且开源，不强制打赏");
        binding.donate.getRoot().setOnClickListener(v -> PopUp());
        
        binding.helpEarnRewards.text.setText("赚钱必备");
        binding.helpEarnRewards.label.setText("利用空闲时间，每天轻轻松松赚一杯奶茶钱");
        binding.helpEarnRewards.getRoot().setOnClickListener(v -> helpEarnRewards());
        
        init();
        return binding.getRoot();
    }
    
    private Switch setSwitch(ItemSwitchBinding binding, String tag, boolean isChecked) {
        binding.switch2.setChecked(getBoolean(tag, isChecked));
        binding.switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    putBoolean(tag, isChecked);
                }
            });
        binding.getRoot().setOnClickListener(view -> {
            binding.switch2.toggle();
        });
        return binding.switch2;
    }
    
    private void PopUp() {
        if (alertDialog != null) alertDialog.dismiss();
        DialogDonateBinding mBinding = DialogDonateBinding.inflate(getLayoutInflater());
        TabLayout tabLayout = mBinding.tabLayout;
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("微信"));
        tabLayout.addTab(tabLayout.newTab().setText("支付宝"));
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setSelectedTabIndicatorHeight(0);
        tabLayout.setSelectedTabIndicatorColor(Color.TRANSPARENT);
        tabLayout.setTabRippleColor(null);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                putIntE("donate_tab_position", position);
                setQRcode(mBinding, position);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Tab取消选中时的处理
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Tab重新选中时的处理
            }
        });
        int p = getInt("donate_tab_position", 0);
        if (p >= tabLayout.getTabCount()) p = 0;
        setQRcode(mBinding, p);
        tabLayout.getTabAt(p).select();
        mBinding.gd.setOnClickListener(v -> helpEarnRewards());
        mBinding.close.setOnClickListener(v -> alertDialog.dismiss());
        mBinding.open.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AlipayData))));
        mBinding.text.setOnClickListener(v -> {
            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
            if (!imageSaveHelper.hasAllRequiredPermissions()) {
                show("请先授予必要权限");
                return;
            }
            imageSaveHelper.saveImageToGallery(getInt("donate_tab_position", 0) == 0 ? _wx : _zfb, fileName, new ImageSaveHelper.SaveCallback() {
                @Override
                public void onSuccess(String imagePath) {
                    show("图片保存成功: " + imagePath);
                }
                @Override
                public void onError(String errorMessage) {
                    show(errorMessage);
                }
            });
        });
        alertDialog = new AlertDialog.Builder(requireActivity(), R.style.CustomDialog).setView(mBinding.getRoot()).setCancelable(false).create();
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.TRANSPARENT);
        drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 26f, requireActivity().getResources().getDisplayMetrics()));
        Window window = alertDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawable(drawable);
        window.getAttributes().windowAnimations = R.style.MenuDialoggWindowAnim;
        alertDialog.show();
    }
    
    private void setQRcode(DialogDonateBinding mBinding, int position) {
        initQRcode();
    	switch (position) {
            case 0:
                Drawable drawable = new BitmapDrawable(wx);
                Glide.with(requireActivity())
                    .load(AppUtil.image.appreciate)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(drawable)
                    .error(drawable)
                    .fallback(drawable)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(@NonNull Drawable resource, @Nullable Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                            if (resource instanceof BitmapDrawable) {
                                Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                if (bitmap != null) {
                                    wx = bitmap;
                                    _wx = bitmap;
                                }
                            }
                            return false;
                        }
                    })
                    .into(mBinding.icon);
                mBinding.open.setVisibility(8);
                mBinding.text.setText("保存图片到相册");
                break;
            case 1:
                mBinding.icon.setImageBitmap(zfb);
                boolean hasAlipay = AppUtil.isAppInstalled(requireActivity(), "com.eg.android.AlipayGphone");
                mBinding.open.setVisibility(hasAlipay ? 0 : 8);
                mBinding.text.setText(hasAlipay ? "保存" : "保存图片到相册");
                break;
        }
    }
    
    private void init() {
    	executor.execute(() -> {
            initQRcode();
        });
    }
    
    private void initQRcode() {
        int size = 300;
    	if (wx == null || zfb == null || _wx == null || _zfb == null) {
            wx = getQRcode(WxData, size);
            zfb = getQRcode(AlipayData, size);
            _wx = getQRcodeWithText(WxData, size, "微信", 30);
            _zfb = getQRcodeWithText(AlipayData, size, "支付宝", 30);
        }
    }
    
    private void helpEarnRewards() {
    	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://utyam.ffudbangcyq8yj.cn/?code=12097404")));
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            show("权限已授予，请重新点击保存");
        } else {
            show("权限被拒绝，无法保存图片");
        }
    }
        
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}