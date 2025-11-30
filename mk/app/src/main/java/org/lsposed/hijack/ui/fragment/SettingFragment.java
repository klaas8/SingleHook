package org.lsposed.hijack.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import org.lsposed.hijack.databinding.FragmentSettingBinding;
import org.lsposed.hijack.databinding.ItemSwitchBinding;
import org.lsposed.hijack.R;
import org.lsposed.hijack.util.AppUtil;

public class SettingFragment extends MainBase {

    private FragmentSettingBinding binding;
    
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingBinding.inflate(inflater, container, false);
        
        TooltipCompat.setTooltipText(binding.name, getString(R.string.title_setting));
        
        String qq = "1252760547";
        String imageUrl = "https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=100&nopng=1";
        TooltipCompat.setTooltipText(binding.icon, qq);
        Glide.with(requireActivity())
                .load(imageUrl)
                .transform(new RoundedCorners(100))
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.no_apk)
                .error(R.drawable.no_apk)
                .fallback(R.drawable.no_apk)
                .into(binding.icon);
        
        binding.category1.title.setText("设置");
        
        binding.hideIcon.text.setText("隐藏图标");
        setSwitch(binding.hideIcon, "hideIcon", isHideIcon()).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setShowIcon(!isChecked);
                }
            });

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
        
        binding.officialWebsite.text.setText("更新地址");
        binding.officialWebsite.label.setText("无法检测更新，可以在这里下载");
        binding.officialWebsite.root.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppUtil.NetworkDiskLink)));
            show("密码: "+ AppUtil.NetworkDiskPassword);
        });
        
        binding.mtAutoCheckin.text.setText("MT论坛");
        binding.mtAutoCheckin.label.setText("已将自动签到移植到GitHub部署，完全免费");
        binding.mtAutoCheckin.root.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/FlexHook/MT")));
        });
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
        binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    binding.switch2.toggle();
                }
            });
        return binding.switch2;
    }
        
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}