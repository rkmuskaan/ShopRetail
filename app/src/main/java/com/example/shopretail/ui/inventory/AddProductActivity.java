package com.example.shopretail.ui.inventory;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.shopretail.data.entity.Product;
import com.example.shopretail.databinding.ActivityAddProductBinding;
import com.example.shopretail.viewmodel.ProductViewModel;

public class AddProductActivity extends AppCompatActivity {

    private ActivityAddProductBinding binding;
    private ProductViewModel productViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddProductBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        binding.buttonSave.setOnClickListener(v -> saveProduct());
    }

    private void saveProduct() {
        String name = binding.editTextName.getText().toString().trim();
        String sku = binding.editTextSKU.getText().toString().trim();
        String category = binding.editTextCategory.getText().toString().trim();
        String stockStr = binding.editTextStock.getText().toString().trim();
        String minStockStr = binding.editTextMinStock.getText().toString().trim();
        String purchasePriceStr = binding.editTextPurchasePrice.getText().toString().trim();
        String retailPriceStr = binding.editTextRetailPrice.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(sku) || TextUtils.isEmpty(stockStr)) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int stock = Integer.parseInt(stockStr);
            int minStock = TextUtils.isEmpty(minStockStr) ? 0 : Integer.parseInt(minStockStr);
            double purchasePrice = TextUtils.isEmpty(purchasePriceStr) ? 0.0 : Double.parseDouble(purchasePriceStr);
            double retailPrice = TextUtils.isEmpty(retailPriceStr) ? 0.0 : Double.parseDouble(retailPriceStr);

            Product product = new Product(name, sku, stock, minStock, purchasePrice, retailPrice, category);
            productViewModel.insert(product);
            Toast.makeText(this, "Product saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
