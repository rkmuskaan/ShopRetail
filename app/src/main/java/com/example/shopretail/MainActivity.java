package com.example.shopretail;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.shopretail.databinding.ActivityMainBinding;
import com.example.shopretail.ui.analytics.AutomationDashboardActivity;
import com.example.shopretail.ui.inventory.InventoryActivity;
import com.example.shopretail.ui.sales.DashboardActivity;
import com.example.shopretail.ui.sales.LogSaleActivity;
import com.example.shopretail.viewmodel.SalesViewModel;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SalesViewModel salesViewModel;
    private SharedPreferences prefs;

    private final String[] presetColorHex = {"#000080", "#FF0000", "#800080", "#008000", "#FFA500", "#FFFF00", "#FFC0CB"};

    private final ActivityResultLauncher<Intent> wallpaperPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        startCrop(imageUri);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> cropLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, "Wallpaper applied to selected pages", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
        salesViewModel = new ViewModelProvider(this).get(SalesViewModel.class);

        setupDashboardPreview();
        setupNavigation();
        setupDrawerActions();
        
        float savedValue = prefs.getFloat("theme_value", 100f);
        boolean isDefault = prefs.getBoolean("is_default_theme", true);
        
        if (!isDefault) {
            applyDynamicTheme(savedValue);
        }

        binding.btnOptions.setOnClickListener(v -> 
            binding.drawerLayout.openDrawer(GravityCompat.END));
    }

    private void setupDrawerActions() {
        binding.menuAppearance.setOnClickListener(v -> {
            boolean isVisible = binding.subMenuAppearance.getVisibility() == View.VISIBLE;
            binding.subMenuAppearance.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        binding.menuSetTheme.setOnClickListener(v -> showThemeDialog());

        binding.menuBackgroundImage.setOnClickListener(v -> showBackgroundOptionsDialog());
    }

    private void showBackgroundOptionsDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_background_options, null);
        ThemeUtils.applyBackgroundToView(this, view, "bg_target_popups");
        LinearLayout colorContainer = view.findViewById(R.id.colorContainer);
        MaterialCheckBox targetInventory = view.findViewById(R.id.targetInventory);
        MaterialCheckBox targetLogSale = view.findViewById(R.id.targetLogSale);
        MaterialCheckBox targetAutomation = view.findViewById(R.id.targetAutomation);
        MaterialCheckBox targetPopups = view.findViewById(R.id.targetPopups);
        View wallpaperTargets = view.findViewById(R.id.wallpaperTargets);

        // Load current targets
        targetInventory.setChecked(prefs.getBoolean("bg_target_inventory", false));
        targetLogSale.setChecked(prefs.getBoolean("bg_target_logsale", false));
        targetAutomation.setChecked(prefs.getBoolean("bg_target_automation", false));
        targetPopups.setChecked(prefs.getBoolean("bg_target_popups", false));

        for (String colorHex : presetColorHex) {
            View colorCircle = new View(this);
            int size = (int) (40 * getResources().getDisplayMetrics().density);
            int margin = (int) (8 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, margin, margin, margin);
            colorCircle.setLayoutParams(params);

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(Color.parseColor(colorHex));
            colorCircle.setBackground(shape);

            colorCircle.setOnClickListener(v -> {
                prefs.edit().putString("accent_color", colorHex).apply();
                applyDynamicTheme(prefs.getFloat("theme_value", 100f));
                Toast.makeText(this, "Accent color updated", Toast.LENGTH_SHORT).show();
            });
            colorContainer.addView(colorCircle);
        }

        view.findViewById(R.id.btnSelectWallpaper).setOnClickListener(v -> {
            wallpaperTargets.setVisibility(View.VISIBLE);
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            wallpaperPickerLauncher.launch(intent);
        });

        new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setPositiveButton("Save", (dialog, which) -> {
                    prefs.edit()
                            .putBoolean("bg_target_inventory", targetInventory.isChecked())
                            .putBoolean("bg_target_logsale", targetLogSale.isChecked())
                            .putBoolean("bg_target_automation", targetAutomation.isChecked())
                            .putBoolean("bg_target_popups", targetPopups.isChecked())
                            .apply();
                })
                .show();
    }

    private void startCrop(Uri uri) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(uri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 9);
            cropIntent.putExtra("aspectY", 16);
            cropIntent.putExtra("outputX", 1080);
            cropIntent.putExtra("outputY", 1920);
            cropIntent.putExtra("return-data", true);
            cropLauncher.launch(cropIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Crop not supported, using full image", Toast.LENGTH_SHORT).show();
            prefs.edit().putString("wallpaper_uri", uri.toString()).apply();
        }
    }

    private void showThemeDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_set_theme, null);
        ThemeUtils.applyBackgroundToView(this, view, "bg_target_popups");
        Slider themeSlider = view.findViewById(R.id.themeSlider);
        MaterialCheckBox cbDefault = view.findViewById(R.id.cbDefaultTheme);

        float savedValue = prefs.getFloat("theme_value", 100f);
        boolean isDefault = prefs.getBoolean("is_default_theme", true);

        themeSlider.setValue(savedValue);
        cbDefault.setChecked(isDefault);
        themeSlider.setEnabled(!isDefault);

        cbDefault.setOnCheckedChangeListener((buttonView, isChecked) -> {
            themeSlider.setEnabled(!isChecked);
            prefs.edit().putBoolean("is_default_theme", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            } else {
                applyDynamicTheme(themeSlider.getValue());
            }
        });

        themeSlider.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser && !cbDefault.isChecked()) {
                applyDynamicTheme(value);
            }
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            dialog.getWindow().setDimAmount(0.7f);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
                dialog.getWindow().getAttributes().setBlurBehindRadius(30);
            }
        }

        dialog.show();
    }

    private void applyDynamicTheme(float value) {
        prefs.edit().putFloat("theme_value", value).apply();

        int bgColor, pillColor, textColor;
        String accentHex = prefs.getString("accent_color", "#F5F5F5");
        int baseAccentColor = Color.parseColor(accentHex);

        if (value <= 50) {
            float fraction = value / 50f;
            bgColor = interpolateColor(Color.parseColor("#121212"), Color.parseColor("#EEEEEE"), fraction);
            pillColor = interpolateColor(Color.parseColor("#2C2C2C"), baseAccentColor, fraction);
            textColor = interpolateColor(Color.WHITE, Color.BLACK, fraction);
        } else {
            float fraction = (value - 50f) / 50f;
            bgColor = interpolateColor(Color.parseColor("#EEEEEE"), Color.WHITE, fraction);
            pillColor = interpolateColor(baseAccentColor, Color.parseColor("#F5F5F5"), fraction);
            textColor = Color.BLACK;
        }

        updateUiColors(bgColor, pillColor, textColor);
    }

    private void updateUiColors(int bgColor, int pillColor, int textColor) {
        binding.drawerLayout.setBackgroundColor(bgColor);
        binding.slideMenu.setBackgroundColor(bgColor);
        
        if (getWindow() != null) {
            getWindow().setStatusBarColor(bgColor);
            View decor = getWindow().getDecorView();
            double luminance = (0.299 * Color.red(bgColor) + 0.587 * Color.green(bgColor) + 0.114 * Color.blue(bgColor)) / 255.0;
            if (luminance > 0.5) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            } else {
                decor.setSystemUiVisibility(0);
            }
        }

        binding.dashboardPreview.setBackgroundColor(bgColor);
        binding.centerMoneyBox.setCardBackgroundColor(interpolateColor(bgColor, Color.GRAY, 0.05f));
        binding.headerPill.setTextColor(textColor);
        
        tintBackground(binding.btnOptions, pillColor);
        tintBackground(binding.navInventory, pillColor);
        tintBackground(binding.navLogSale, pillColor);
        tintBackground(binding.navAnalytics, pillColor);
        tintBackground(binding.navDashboard, pillColor);

        updateChildColors(binding.drawerLayout, textColor);
    }

    private void tintBackground(View view, int color) {
        Drawable bg = view.getBackground();
        if (bg != null) {
            bg.mutate().setTint(color);
        }
    }

    private void updateChildColors(ViewGroup viewGroup, int textColor) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(textColor);
            } else if (child instanceof ImageView) {
                ((ImageView) child).setColorFilter(textColor);
            } else if (child instanceof ViewGroup) {
                updateChildColors((ViewGroup) child, textColor);
            }
        }
    }

    private int interpolateColor(int color1, int color2, float fraction) {
        float[] hsv1 = new float[3];
        float[] hsv2 = new float[3];
        Color.colorToHSV(color1, hsv1);
        Color.colorToHSV(color2, hsv2);

        for (int i = 0; i < 3; i++) {
            hsv1[i] = hsv1[i] + (hsv2[i] - hsv1[i]) * fraction;
        }

        int alpha = (int) (Color.alpha(color1) + (Color.alpha(color2) - Color.alpha(color1)) * fraction);
        return Color.HSVToColor(alpha, hsv1);
    }

    private void setupDashboardPreview() {
        salesViewModel.getTodaySalesCount().observe(this, count -> {
            binding.mainSalesCount.setText(String.valueOf(count != null ? count : 0));
            
            // Calculate Rate
            int c = count != null ? count : 0;
            String rate = "Low";
            if (c > 20) rate = "High";
            else if (c > 5) rate = "Mid";
            binding.mainSalesRate.setText(rate);
        });

        salesViewModel.getTodayAvgSalesValue().observe(this, avg -> {
            binding.mainAvgSalesValue.setText(String.format(Locale.getDefault(), "$%.2f", avg != null ? avg : 0.0));
        });

        salesViewModel.getTodayProfit().observe(this, todayProfit -> {
            double tp = todayProfit != null ? todayProfit : 0.0;
            salesViewModel.getYesterdayProfit().observe(this, yesterdayProfit -> {
                double yp = yesterdayProfit != null ? yesterdayProfit : 0.0;
                if (tp > yp) {
                    binding.mainProfitGrowth.setText("Positive");
                    binding.mainProfitGrowth.setTextColor(Color.GREEN);
                } else if (tp < yp) {
                    binding.mainProfitGrowth.setText("Negative");
                    binding.mainProfitGrowth.setTextColor(Color.RED);
                } else {
                    binding.mainProfitGrowth.setText("Neutral");
                    binding.mainProfitGrowth.setTextColor(Color.GRAY);
                }
            });
        });
    }

    private void setupNavigation() {
        binding.navInventory.setOnClickListener(v -> 
            startActivity(new Intent(this, InventoryActivity.class)));

        binding.navLogSale.setOnClickListener(v -> 
            startActivity(new Intent(this, LogSaleActivity.class)));

        binding.navAnalytics.setOnClickListener(v -> 
            startActivity(new Intent(this, AutomationDashboardActivity.class)));

        binding.navDashboard.setOnClickListener(v -> 
            startActivity(new Intent(this, DashboardActivity.class)));
    }
}
