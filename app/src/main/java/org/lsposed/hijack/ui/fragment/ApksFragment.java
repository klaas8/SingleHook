package org.lsposed.hijack.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import org.lsposed.hijack.R;
import org.lsposed.hijack.databinding.BottomDialogAppInfoBinding;
import org.lsposed.hijack.databinding.DialogEditPackageBinding;
import org.lsposed.hijack.databinding.FragmentApksBinding;
import org.lsposed.hijack.databinding.ItemAppBinding;
import org.lsposed.hijack.util.ApksAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.imageview.ShapeableImageView;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.lsposed.hijack.util.AppUtil;

public class ApksFragment extends MainBase {

    private AlertDialog alertDialog;
    private FragmentApksBinding binding;
    private RecyclerViewAdapter adapter;
    private Set<String> packageLists = new HashSet<>();
    private AtomicBoolean isAnalysis = new AtomicBoolean(), isRefresh = new AtomicBoolean();
    private final AtomicReference<Runnable> pending = new AtomicReference<>();
    private boolean isActivityResumed = false;
    private BottomSheetDialog builderDialog;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentApksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        main.postDelayed(
                () -> {
                    loadData();
                },
                50);
        registerReceiver();
    }

    // 初始化View
    private void initView() {
        LinearLayoutManager layoutManager = new CustomLayoutManager(requireContext());
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setItemViewCacheSize(30);
        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.addOnScrollListener(
                new RecyclerView.OnScrollListener() {
                    private int totalScrolled = 0;
                    @Override
                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        totalScrolled += dy;
                        if (dy > 0) {
                            hideNavigation();
                        } else if (dy < 0 || totalScrolled <= 0) {
                            showNavigation();
                        }
                    }
                });
    }

    // 加载数据
    public void loadData() {
        if (!isRefresh.getAndSet(true)) {
            executor.execute(
                    () -> {
                        List<Apk> data = getAdapterData();
                        main.post(
                                () -> {
                                    adapter = new RecyclerViewAdapter(data);
                                    adapter.setHasStableIds(true);
                                    binding.recyclerView.setAdapter(adapter);
                                    if (data.size() > 14)
                                        new FastScrollerBuilder(binding.recyclerView)
                                                .useMd2Style()
                                                .build()
                                                .setPadding(0, 0, 0, getH());
                                    binding.recyclerView.setPadding(0, 0, 0, getH());
                                });
                        isRefresh.set(false);
                    });
        }
    }

    // 监听软件 更新/安装/删除，并刷新页面
    private BroadcastReceiver packageChangeReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (packageLists.contains(packageName)) {
                        if (Intent.ACTION_PACKAGE_ADDED.equals(action)
                                || Intent.ACTION_PACKAGE_REMOVED.equals(action)
                                || Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
                            if (isActivityResumed) {
                                loadData();
                            } else {
                                pending.set(() -> loadData());
                            }
                        }
                    }
                }
            };

    // 注册软件监听
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        requireActivity().registerReceiver(packageChangeReceiver, filter);
    }

    // 设置额外加载
    public class CustomLayoutManager extends LinearLayoutManager {
        public CustomLayoutManager(Context context) {
            super(context);
        }

        @Override
        protected void calculateExtraLayoutSpace(RecyclerView.State state, int[] extraLayoutSpace) {
            Arrays.fill(extraLayoutSpace, 5000);
        }
    }

    // 适配器
    private class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private final List<Apk> data;
        private final Drawable defaultIcon;
        RecyclerViewAdapter(List<Apk> data) {
            this.data = data;
            this.defaultIcon = requireContext().getDrawable(R.drawable.no_apk);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemAppBinding binding = ItemAppBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {}

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).packageName.hashCode();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View root;
            ShapeableImageView IconView;
            TextView AppNameView, AppVersionView, packageNameView;

            ViewHolder(@NonNull ItemAppBinding binding, int position) {
                super(binding.getRoot());
                if (position + 1 == getItemCount()) {
                    binding.getRoot().setVisibility(4);
                    return;
                }
                IconView = binding.appIcon;
                AppNameView = binding.appName;
                AppVersionView = binding.appVersion;
                packageNameView = binding.packageName;
                root = binding.getRoot();
                bind(data.get(position));
            }

            void bind(Apk info) {
                Drawable icon = info.getType() != 2 ? info.getApkIcon() : defaultIcon;
                if (icon == null) icon = defaultIcon;
                IconView.setImageDrawable(icon);
                AppNameView.setText(info.getApkName());
                packageNameView.setText(info.getPackageName());
                AppVersionView.setText(
                        info.getType() != 2
                                ? String.format(
                                        "%s（%s）", info.getVersionName(), info.getVersionCode())
                                : info.getVersionName());
                if (info.getType() == 0) AppVersionView.setTextColor(0xFFFF0000);
                info.setApkIcon(icon);
                root.setOnClickListener(
                        v -> {
                            show(info);
                        });
            }
        }
    }
    
    public void show(Apk info) {
        if (builderDialog != null) builderDialog.dismiss();
        builderDialog = new BottomSheetDialog(requireContext());
        BottomDialogAppInfoBinding root = BottomDialogAppInfoBinding.inflate(getLayoutInflater());
        root.icon.setImageDrawable(info.apkIcon);
        root.appName.setText(info.getApkName());
        root.appName.setSelected(true);
        root.packageName.title.setText("包名");
        root.packageName.value.setText(info.getPackageName());
        if (info.getType() != 2) {
            root.versionName.title.setText("版本名");
            root.versionName.value.setText(info.getVersionName());
            root.versionCode.title.setText("版本号");
            root.versionCode.value.setText(info.getVersionCode() + "");
        } else {
            root.versionName.getRoot().setVisibility(8);
            root.versionCode.getRoot().setVisibility(8);
        }
        root.adapterVersion.title.setText("适配版本");
        root.adapterVersion.value.setText(
                info.adapterVersionCode == ApksAdapter.isAutoAdapter
                        ? "全版本适配"
                        : info.getType() == 0
                                ? info.getVersionCode() + " >> " + info.getAdapterVersionCode()
                                : info.getAdapterVersionCode() + "");
        root.extraAdapter.title.setText("额外包");
        root.extraAdapter.value.setText(getString("extra_" + info.getPackageName(), ""));
        root.extraAdapter.getRoot().setVisibility(TextUtils.isEmpty(getString("extra_" + info.getPackageName(), "")) ? 8 : 0);
        if (info.getType() == 0) root.adapterVersion.value.setTextColor(0xFFFF0000);
        root.description.title.setText("Hook详情");
        root.description.value.setText(info.getDescription());
        root.description.value.setSingleLine(false);
        root.description.value.setMaxLines(Integer.MAX_VALUE);
        root.description.value.setEllipsize(null);
        root.not.setText("打开");
        root.ok.setText("下载");
        root.not.setOnClickListener(
                v -> {
                    if (info.getUrl().contains("lanzou")) makeText("密码：6666");
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(info.getUrl())));
                    builderDialog.dismiss();
                });
        root.ok.setOnClickListener(
                v -> {
                    if (info.getUrl().contains("lanzou")) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + AppUtil.domainName + "/lzy/?url=" + info.getUrl() + "&p=6666&type=down")));
                    } else {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(info.getUrl())));
                    }
                    builderDialog.dismiss();
                });
        root.setting.setOnClickListener(v -> PopUp(info, root));
        builderDialog.setContentView(root.getRoot());
        builderDialog.show();
    }
    
    private void PopUp(Apk info, BottomDialogAppInfoBinding root) {
        if (alertDialog != null) alertDialog.dismiss();
        DialogEditPackageBinding binding = DialogEditPackageBinding.inflate(getLayoutInflater());
        binding.tilPackage.setErrorEnabled(true);
        binding.etPackage.setText(getString("extra_" + info.getPackageName(), ""));
        binding.etPackage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (binding.tilPackage.isErrorEnabled()) {
                    binding.tilPackage.setError(null);
                }
            }
        });
        binding.ok.setOnClickListener(v -> {
            String text = binding.etPackage.getText().toString();
            if (TextUtils.isEmpty(text) && !TextUtils.isEmpty(getString("extra_" + info.getPackageName(), ""))) {
                remove("extra_" + info.getPackageName());
            } else if (TextUtils.isEmpty(text)) {
                binding.tilPackage.setError("请输入包名");
                return;
            } else if (!isValidPackageName(text)) {
                binding.tilPackage.setError("包名格式不正确，应为 com.example.app 形式");
                return;
            } else if (text.equals(info.getPackageName())) {
                binding.tilPackage.setError("包名与当前适配的一致");
                return;
            }
            alertDialog.dismiss();
            if (!TextUtils.isEmpty(text)) putStringE("extra_" + info.getPackageName(), text);
            if (root.extraAdapter.getRoot() != null) {
                root.extraAdapter.value.setText(getString("extra_" + info.getPackageName(), ""));
                root.extraAdapter.getRoot().setVisibility(TextUtils.isEmpty(getString("extra_" + info.getPackageName(), "")) ? 8 : 0);
            }
            show("保存成功！");
        });
        alertDialog = new AlertDialog.Builder(requireActivity(), R.style.CustomDialog).setView(binding.getRoot()).setCancelable(true).create();
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.WHITE);
        drawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, requireActivity().getResources().getDisplayMetrics()));
        Window window = alertDialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setBackgroundDrawable(drawable);
        window.getAttributes().windowAnimations = R.style.MenuDialoggWindowAnim;
        alertDialog.show();
    }
    
    private boolean isValidPackageName(String packageName) {
        return packageName.matches("[a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]*)+");
    }

    // 适配器项目数据
    public class Apk {
        public int versionCode;
        public int adapterVersionCode;
        public int type;
        public String apkName;
        public String packageName;
        public String url;
        public String description;
        public String versionName;
        public Drawable apkIcon;

        public int getVersionCode() {
            return this.versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }

        public int getType() {
            return this.type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public String getApkName() {
            return this.apkName;
        }

        public void setApkName(String apkName) {
            this.apkName = apkName;
        }

        public String getPackageName() {
            return this.packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getUrl() {
            return this.url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getVersionName() {
            return this.versionName;
        }

        public void setVersionName(String versionName) {
            this.versionName = versionName;
        }

        public Drawable getApkIcon() {
            return this.apkIcon;
        }

        public void setApkIcon(Drawable apkIcon) {
            this.apkIcon = apkIcon;
        }

        public int getAdapterVersionCode() {
            return this.adapterVersionCode;
        }

        public void setAdapterVersionCode(int adapterVersionCode) {
            this.adapterVersionCode = adapterVersionCode;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    // 获取all适配器数据
    private List<Apk> getAdapterData() {
        List<Apk> upper = new ArrayList<>();
        List<Apk> center = new ArrayList<>();
        List<Apk> below = new ArrayList<>();
        List<String> packageNames = new ArrayList<>();
        Context context = requireContext();
        PackageManager packageManager = context.getPackageManager();
        for (ApksAdapter.AppInfo appInfo : ApksAdapter.getAppInfos()) {
            String packageName = appInfo.packageName;
            if (packageNames.contains(packageName)) continue;
            packageLists.add(packageName);
            packageNames.add(packageName);
            int versionCode = appInfo.versionCode;
            String displayName = appInfo.apkName;
            String versionName = "未安装";
            Drawable apkIcon = null;
            int adaptationStatus;
            try {
                PackageInfo info = packageManager.getPackageInfo(packageName, 0);
                ApplicationInfo appInfoInstalled =
                        packageManager.getApplicationInfo(packageName, 0);
                apkIcon = packageManager.getApplicationIcon(appInfoInstalled);
                displayName = info.applicationInfo.loadLabel(packageManager).toString();
                versionName = info.versionName;
                versionCode = info.versionCode;
                boolean isAdapted =
                        (appInfo.versionCode == versionCode
                                || appInfo.versionCode == ApksAdapter.isAutoAdapter);
                adaptationStatus = isAdapted ? 1 : 0;
            } catch (PackageManager.NameNotFoundException e) {
                adaptationStatus = 2;
            }
            Apk apk = new Apk();
            apk.setVersionCode(versionCode);
            apk.setApkName(displayName);
            apk.setPackageName(packageName);
            apk.setUrl(appInfo.url);
            apk.setType(adaptationStatus);
            apk.setApkIcon(adaptationStatus != 2 ? apkIcon : null);
            apk.setVersionName(versionName);
            apk.setAdapterVersionCode(appInfo.versionCode);
            apk.setDescription(appInfo.description);
            switch (adaptationStatus) {
                case 0:
                    upper.add(apk);
                    break;
                case 1:
                    center.add(apk);
                    break;
                case 2:
                    below.add(apk);
                    break;
            }
        }
        List<Apk> result = new ArrayList<>();
        result.addAll(upper);
        result.addAll(center);
        result.addAll(below);
        Apk apk = new Apk();
        apk.setPackageName(requireContext().getPackageName());
        result.add(apk);
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();
        isActivityResumed = true;
        Runnable latest = pending.getAndSet(null);
        if (latest != null) {
            main.post(latest);
        }
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
        if (packageChangeReceiver != null) {
            try {
                requireActivity().unregisterReceiver(packageChangeReceiver);
            } catch (Throwable e) {
            }
        }
    }
}
