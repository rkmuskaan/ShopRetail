package com.example.shopretail.ui.inventory;

import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.shopretail.R;
import com.example.shopretail.ThemeUtils;
import com.example.shopretail.databinding.ActivityInventoryBinding;
import com.example.shopretail.ui.adapter.ProductAdapter;
import com.example.shopretail.ui.analytics.AutomationDashboardActivity;
import com.example.shopretail.ui.sales.DashboardActivity;
import com.example.shopretail.ui.sales.LogSaleActivity;
import com.example.shopretail.viewmodel.ProductViewModel;

public class InventoryActivity extends AppCompatActivity {

    private ActivityInventoryBinding binding;
    private ProductViewModel productViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInventoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        ThemeUtils.applyBackground(this, "bg_target_inventory");

        ProductAdapter adapter = new ProductAdapter();
        binding.recyclerViewInventory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewInventory.setAdapter(adapter);

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        productViewModel.getAllProducts().observe(this, adapter::submitList);

        binding.fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, AddProductActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.inventory_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_log_sale) {
            startActivity(new Intent(this, LogSaleActivity.class));
            return true;
        } else if (id == R.id.action_dashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
            return true;
        } else if (id == R.id.action_automation) {
            startActivity(new Intent(this, AutomationDashboardActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
