package com.example.shopretail.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.shopretail.data.entity.Product;

import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    void insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("SELECT * FROM products ORDER BY name ASC")
    LiveData<List<Product>> getAllProducts();

    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<Product> getProductById(int id);

    @Query("SELECT * FROM products WHERE currentStock <= minRequiredStock")
    LiveData<List<Product>> getLowStockProducts();

    @Query("SELECT category, SUM(currentStock) as totalStock FROM products GROUP BY category")
    LiveData<List<CategoryStock>> getStockByCategory();

    class CategoryStock {
        public String category;
        public int totalStock;
    }
}
