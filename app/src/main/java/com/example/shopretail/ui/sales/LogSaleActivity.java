package com.example.shopretail.ui.sales;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.shopretail.ThemeUtils;
import com.example.shopretail.data.entity.Product;
import com.example.shopretail.data.entity.Sale;
import com.example.shopretail.databinding.ActivityLogSaleBinding;
import com.example.shopretail.viewmodel.ProductViewModel;
import com.example.shopretail.viewmodel.SalesViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LogSaleActivity extends AppCompatActivity {

    private ActivityLogSaleBinding binding;
    private ProductViewModel productViewModel;
    private SalesViewModel salesViewModel;
    private List<Product> productList = new ArrayList<>();
    private Product selectedProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLogSaleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ThemeUtils.applyBackground(this, "bg_target_logsale");

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        salesViewModel = new ViewModelProvider(this).get(SalesViewModel.class);

        setupProductSelection();
        setupQuantityListener();

        binding.buttonLogSale.setOnClickListener(v -> processSale());
    }

    private void setupProductSelection() {
        productViewModel.getAllProducts().observe(this, products -> {
            productList = products;
            List<String> productNames = new ArrayList<>();
            for (Product p : products) {
                productNames.add(p.getName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, productNames);
            binding.autoCompleteProduct.setAdapter(adapter);
        });

        binding.autoCompleteProduct.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            for (Product p : productList) {
                if (p.getName().equals(selectedName)) {
                    selectedProduct = p;
                    updateStockInfo();
                    calculateTotal();
                    break;
                }
            }
        });
    }

    private void updateStockInfo() {
        if (selectedProduct != null) {
            binding.textViewStockAvailable.setText(String.format(Locale.getDefault(), "Stock available: %d", selectedProduct.getCurrentStock()));
        }
    }

    private void setupQuantityListener() {
        binding.editTextQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                calculateTotal();
            }
        });
    }

    private void calculateTotal() {
        String qtyStr = binding.editTextQuantity.getText().toString();
        if (selectedProduct != null && !TextUtils.isEmpty(qtyStr)) {
            try {
                int qty = Integer.parseInt(qtyStr);
                double total = qty * selectedProduct.getRetailPrice();
                binding.textViewTotalAmount.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
            } catch (NumberFormatException e) {
                binding.textViewTotalAmount.setText("Total: $0.00");
            }
        } else {
            binding.textViewTotalAmount.setText("Total: $0.00");
        }
    }

    private void processSale() {
        if (selectedProduct == null) {
            Toast.makeText(this, "Please select a product", Toast.LENGTH_SHORT).show();
            return;
        }

        String qtyStr = binding.editTextQuantity.getText().toString();
        if (TextUtils.isEmpty(qtyStr)) {
            Toast.makeText(this, "Please enter quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        int qty = Integer.parseInt(qtyStr);
        if (qty <= 0) {
            Toast.makeText(this, "Quantity must be greater than zero", Toast.LENGTH_SHORT).show();
            return;
        }

        if (qty > selectedProduct.getCurrentStock()) {
            Toast.makeText(this, "Insufficient stock", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalAmount = qty * selectedProduct.getRetailPrice();
        Sale sale = new Sale(selectedProduct.getId(), qty, totalAmount, System.currentTimeMillis());

        // Deduct stock
        selectedProduct.setCurrentStock(selectedProduct.getCurrentStock() - qty);
        productViewModel.update(selectedProduct);
        salesViewModel.insertSale(sale);

        Toast.makeText(this, "Sale logged successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
