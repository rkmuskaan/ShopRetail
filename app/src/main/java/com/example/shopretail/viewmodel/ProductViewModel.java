package com.example.shopretail.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.shopretail.data.entity.Product;
import com.example.shopretail.data.repository.RetailRepository;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {
    private final RetailRepository repository;
    private final LiveData<List<Product>> allProducts;

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new RetailRepository(application);
        allProducts = repository.getAllProducts();
    }

    public LiveData<List<Product>> getAllProducts() {
        return allProducts;
    }

    public LiveData<List<Product>> getLowStockProducts() {
        return repository.getLowStockProducts();
    }

    public void insert(Product product) {
        repository.insertProduct(product);
    }

    public void update(Product product) {
        repository.updateProduct(product);
    }
}
