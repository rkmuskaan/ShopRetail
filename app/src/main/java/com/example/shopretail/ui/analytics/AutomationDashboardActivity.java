package com.example.shopretail.ui.analytics;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.shopretail.ThemeUtils;
import com.example.shopretail.databinding.ActivityAutomationDashboardBinding;
import com.example.shopretail.ui.adapter.PurchaseOrderAdapter;
import com.example.shopretail.viewmodel.AnalyticsViewModel;

import java.util.Locale;

public class AutomationDashboardActivity extends AppCompatActivity {

    private ActivityAutomationDashboardBinding binding;
    private AnalyticsViewModel analyticsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAutomationDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ThemeUtils.applyBackground(this, "bg_target_automation");

        analyticsViewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);

        setupPurchaseOrders();
        observeForecast();
    }

    private void setupPurchaseOrders() {
        PurchaseOrderAdapter adapter = new PurchaseOrderAdapter();
        binding.recyclerViewPurchaseOrders.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewPurchaseOrders.setAdapter(adapter);

        analyticsViewModel.getPredictions().observe(this, adapter::submitList);
    }

    private void observeForecast() {
        analyticsViewModel.getForecast().observe(this, forecast -> {
            if (forecast != null) {
                binding.textViewForecastWeek.setText(String.format(Locale.getDefault(), "$%.2f", forecast.nextWeekIncome));
                binding.textViewForecastMonth.setText(String.format(Locale.getDefault(), "$%.2f", forecast.nextMonthIncome));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
